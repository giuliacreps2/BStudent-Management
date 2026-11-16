package giuliacrepaldi.BStudent_Management.services.resources;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.resources.ResourceCategory;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.resources.ResourceDTO;
import giuliacrepaldi.BStudent_Management.repositories.resources.ResourcesRepository;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.ISubjectsService;
import giuliacrepaldi.BStudent_Management.services.practice.interfaces.ITopicsService;
import giuliacrepaldi.BStudent_Management.services.resources.interfaces.IResourcesService;
import giuliacrepaldi.BStudent_Management.utilities.ContentBlocks;
import giuliacrepaldi.BStudent_Management.utilities.Pageables;
import giuliacrepaldi.BStudent_Management.utilities.Slugs;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.util.Set;

@Service
public class ResourcesService implements IResourcesService {

    private static final Set<String> SORTS = Set.of("resourceId", "title", "category", "status", "publishedAt", "updatedAt");
    private static final String EMPTY_CONTENT = "{}";

    private final ResourcesRepository resourcesRepository;
    private final ISubjectsService subjectsService;
    private final ITopicsService topicsService;
    private final JsonMapper jsonMapper;

    public ResourcesService(ResourcesRepository resourcesRepository, ISubjectsService subjectsService,
                            ITopicsService topicsService, JsonMapper jsonMapper) {
        this.resourcesRepository = resourcesRepository;
        this.subjectsService = subjectsService;
        this.topicsService = topicsService;
        this.jsonMapper = jsonMapper;
    }

    @Override
    @Transactional
    public Resource saveNewResource(long subjectId, ResourceDTO body) {
        Subject subject = subjectsService.findById(subjectId);
        String slug = Slugs.resolve(body.slug(), body.title());
        if (resourcesRepository.existsBySubject_SubjectIdAndSlug(subjectId, slug)) {
            throw new ConflictException("La materia ha già una risorsa con l'indirizzo " + slug);
        }
        Resource resource = Resource.builder()
                .subject(subject)
                .slug(slug)
                .status(ContentStatus.draft)
                .build();
        applyContent(resource, body);
        return resourcesRepository.save(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource findById(long resourceId) {
        return resourcesRepository.findById(resourceId)
                .orElseThrow(() -> new NotFoundException(resourceId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Resource> findAll(Long subjectId, ResourceCategory category, ContentStatus status,
                                  int page, int size, String sortBy) {
        return resourcesRepository.search(subjectId, category, status,
                Pageables.of(page, size, sortBy, SORTS, "title"));
    }

    @Override
    @Transactional
    public Resource findByIdAndUpdate(long resourceId, ResourceDTO body) {
        Resource resource = findById(resourceId);
        Long subjectId = resource.getSubject().getSubjectId();
        String slug = Slugs.resolve(body.slug(), body.title());
        if (resourcesRepository.existsBySubject_SubjectIdAndSlugAndResourceIdNot(subjectId, slug, resourceId)) {
            throw new ConflictException("La materia ha già una risorsa con l'indirizzo " + slug);
        }
        resource.setSlug(slug);
        // Ammessa in ogni stato: i contenuti pubblicati mostrano gli aggiornamenti (blueprint §5.3)
        applyContent(resource, body);
        // Una risorsa pubblicata resta pubblicabile: non si può svuotare
        if (resource.getStatus() == ContentStatus.published && !hasSections(resource)) {
            throw new ConflictException("Una risorsa pubblicata deve avere almeno una tabella, un approfondimento o un esempio");
        }
        return resource;
    }

    @Override
    @Transactional
    public Resource publishResource(long resourceId) {
        Resource resource = findById(resourceId);
        if (resource.getStatus() == ContentStatus.published) {
            throw new ConflictException("La risorsa è già pubblicata");
        }
        if (!hasSections(resource)) {
            throw new ConflictException("Aggiungi almeno una tabella, un approfondimento o un esempio prima di pubblicare");
        }
        resource.setStatus(ContentStatus.published);
        // published_at è la prima pubblicazione: resta anche dopo un ritiro e un ripristino
        if (resource.getPublishedAt() == null) {
            resource.setPublishedAt(OffsetDateTime.now());
        }
        resource.setWithdrawnAt(null);
        return resource;
    }

    @Override
    @Transactional
    public Resource withdrawResource(long resourceId) {
        Resource resource = findById(resourceId);
        if (resource.getStatus() != ContentStatus.published) {
            throw new ConflictException("Si può ritirare solo una risorsa pubblicata");
        }
        // I segnalibri degli studenti restano, ma la risorsa non si vede più
        resource.setStatus(ContentStatus.withdrawn);
        resource.setWithdrawnAt(OffsetDateTime.now());
        return resource;
    }

    @Override
    @Transactional
    public void deleteResource(long resourceId) {
        Resource resource = findById(resourceId);
        if (resource.getStatus() != ContentStatus.draft || resource.getPublishedAt() != null) {
            throw new ConflictException("Una risorsa già pubblicata non si elimina: va ritirata");
        }
        resourcesRepository.delete(resource);
    }

    private void applyContent(Resource resource, ResourceDTO body) {
        resource.setCategory(body.category());
        resource.setTitle(body.title().strip());
        resource.setDescription(body.description().strip());
        resource.setSummaryText(blankToNull(body.summaryText()));
        resource.setCoverReference(blankToNull(body.coverReference()));
        resource.setAccessTier(body.accessTier());
        String icon = blankToNull(body.mostUsedIcon());
        if (body.isMostUsed() && icon == null) {
            throw new BadRequestException("Per le risorse più usate scegli anche un'icona");
        }
        resource.setMostUsed(body.isMostUsed());
        // L'icona ha senso solo con la casella (chk_resources_most_used)
        resource.setMostUsedIcon(body.isMostUsed() ? icon : null);
        resource.setFeatured(body.isFeatured());
        resource.setTopic(findTopic(body.topicId(), resource.getSubject()));

        JsonNode content = body.content();
        if (content == null || content.isNull()) {
            resource.setContent(EMPTY_CONTENT);
        } else {
            ContentBlocks.validateResourceContent(content);
            resource.setContent(jsonMapper.writeValueAsString(content));
        }
    }

    private boolean hasSections(Resource resource) {
        return ContentBlocks.hasResourceSections(jsonMapper.readTree(resource.getContent()));
    }

    // L'argomento deve essere della materia della risorsa: regola tra tabelle, non nel DB
    private Topic findTopic(Long topicId, Subject subject) {
        if (topicId == null) {
            return null;
        }
        Topic topic = topicsService.findById(topicId);
        if (!topic.getSubject().getSubjectId().equals(subject.getSubjectId())) {
            throw new BadRequestException("L'argomento appartiene a un'altra materia");
        }
        return topic;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
