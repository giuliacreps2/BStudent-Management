package giuliacrepaldi.BStudent_Management.payloads.student;

import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.enums.resources.ResourceCategory;

// Risorsa in un elenco (blueprint §7.4). mostUsedIcon solo per "Le risorse più usate".
// premiumLocked = Risorsa a pagamento e studente senza accesso
public record ResourceCardDTO(String slug, String subjectSlug, String title, String description, ResourceCategory category,
                              String coverReference, boolean mostUsed, String mostUsedIcon, boolean featured,
                              boolean premiumLocked) {

    public static ResourceCardDTO from(Resource resource, boolean premiumLocked) {
        return new ResourceCardDTO(resource.getSlug(), resource.getSubject().getSlug(), resource.getTitle(),
                resource.getDescription(), resource.getCategory(), resource.getCoverReference(), resource.isMostUsed(),
                resource.getMostUsedIcon(), resource.isFeatured(), premiumLocked);
    }
}
