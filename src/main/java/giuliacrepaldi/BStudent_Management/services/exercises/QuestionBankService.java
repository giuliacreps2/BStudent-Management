package giuliacrepaldi.BStudent_Management.services.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.Question;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionAnswer;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionRubricCriterion;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionVersion;
import giuliacrepaldi.BStudent_Management.enums.attempts.AssessmentMethod;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.exercises.KindQuestion;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.exercises.QuestionBankEntryDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.QuestionVersionDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.QuestionVersionDTO.AnswerDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.QuestionVersionDTO.RubricCriterionDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.QuestionVersionResponseDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionAnswersRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionBankEntriesRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionRubricCriteriaRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionVersionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.services.exercises.interfaces.IQuestionBankService;
import giuliacrepaldi.BStudent_Management.utilities.Pageables;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Service
public class QuestionBankService implements IQuestionBankService {

    private static final Set<String> SORTS = Set.of("questionBankEntryId", "internalQuestionTitle", "status");

    private final QuestionBankEntriesRepository entriesRepository;
    private final QuestionVersionsRepository versionsRepository;
    private final QuestionsRepository questionsRepository;
    private final QuestionAnswersRepository answersRepository;
    private final QuestionRubricCriteriaRepository rubricCriteriaRepository;
    private final ExerciseAttemptsRepository exerciseAttemptsRepository;
    private final MissionExercisesRepository missionExercisesRepository;
    private final PracticeExercisesRepository practiceExercisesRepository;

    public QuestionBankService(QuestionBankEntriesRepository entriesRepository,
                               QuestionVersionsRepository versionsRepository,
                               QuestionsRepository questionsRepository,
                               QuestionAnswersRepository answersRepository,
                               QuestionRubricCriteriaRepository rubricCriteriaRepository,
                               ExerciseAttemptsRepository exerciseAttemptsRepository,
                               MissionExercisesRepository missionExercisesRepository,
                               PracticeExercisesRepository practiceExercisesRepository) {
        this.entriesRepository = entriesRepository;
        this.versionsRepository = versionsRepository;
        this.questionsRepository = questionsRepository;
        this.answersRepository = answersRepository;
        this.rubricCriteriaRepository = rubricCriteriaRepository;
        this.exerciseAttemptsRepository = exerciseAttemptsRepository;
        this.missionExercisesRepository = missionExercisesRepository;
        this.practiceExercisesRepository = practiceExercisesRepository;
    }

    //ESERCIZI

    @Override
    @Transactional
    public QuestionBankEntry saveNewQuestionBankEntry(QuestionBankEntryDTO body) {
        QuestionBankEntry entry = QuestionBankEntry.builder()
                .internalQuestionTitle(body.internalTitle().strip())
                .status(ContentStatus.draft)
                .build();
        return entriesRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionBankEntry findById(long questionBankEntryId) {
        return entriesRepository.findById(questionBankEntryId)
                .orElseThrow(() -> new NotFoundException(questionBankEntryId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuestionBankEntry> findAll(int page, int size, String sortBy) {
        return entriesRepository.findAll(Pageables.of(page, size, sortBy, SORTS, "internalQuestionTitle"));
    }

    @Override
    @Transactional
    public QuestionBankEntry findByIdAndUpdate(long questionBankEntryId, QuestionBankEntryDTO body) {
        QuestionBankEntry entry = findById(questionBankEntryId);
        entry.setInternalQuestionTitle(body.internalTitle().strip());
        return entry;
    }

    @Override
    @Transactional
    public QuestionBankEntry publishQuestionBankEntry(long questionBankEntryId) {
        QuestionBankEntry entry = findById(questionBankEntryId);
        if (entry.getStatus() == ContentStatus.published) {
            throw new ConflictException("L'esercizio è già pubblicato");
        }
        if (!versionsRepository.existsPublishedVersion(questionBankEntryId)) {
            throw new ConflictException("L'esercizio non ha una versione pubblicata");
        }
        entry.setStatus(ContentStatus.published);
        // published_at è la prima pubblicazione: resta anche dopo un ritiro e un ripristino
        if (entry.getPublishedAt() == null) {
            entry.setPublishedAt(OffsetDateTime.now());
        }
        entry.setWithdrawnAt(null);
        return entry;
    }

    @Override
    @Transactional
    public QuestionBankEntry withdrawQuestionBankEntry(long questionBankEntryId) {
        QuestionBankEntry entry = findById(questionBankEntryId);
        if (entry.getStatus() != ContentStatus.published) {
            throw new ConflictException("Si può ritirare solo un esercizio pubblicato");
        }
        // Una lezione o una voce di Palestra pubblicata resterebbe senza esercizio svolgibile
        if (missionExercisesRepository.existsByQuestionBankEntry_QuestionBankEntryIdAndLesson_Status(
                questionBankEntryId, ContentStatus.published)) {
            throw new ConflictException("L'esercizio è collegato a una lezione pubblicata: ritira prima la lezione");
        }
        if (practiceExercisesRepository.existsByQuestionBankEntry_QuestionBankEntryIdAndStatus(
                questionBankEntryId, ContentStatus.published)) {
            throw new ConflictException("L'esercizio è pubblicato in Palestra: ritiralo prima dalla Palestra");
        }
        entry.setStatus(ContentStatus.withdrawn);
        entry.setWithdrawnAt(OffsetDateTime.now());
        return entry;
    }

    @Override
    @Transactional
    public void deleteQuestionBankEntry(long questionBankEntryId) {
        QuestionBankEntry entry = findById(questionBankEntryId);
        if (entry.getStatus() != ContentStatus.draft || entry.getPublishedAt() != null) {
            throw new ConflictException("Un esercizio già pubblicato non si elimina: va ritirato");
        }
        if (missionExercisesRepository.existsByQuestionBankEntry_QuestionBankEntryId(questionBankEntryId)
                || practiceExercisesRepository.existsByQuestionBankEntry_QuestionBankEntryId(questionBankEntryId)) {
            throw new ConflictException("L'esercizio è collegato a una lezione o alla Palestra: scollegalo prima");
        }
        // Mai pubblicato, quindi nessuna versione è stata svolta: le bozze vanno via con l'esercizio
        versionsRepository.findByQuestionBankEntry_QuestionBankEntryIdOrderByQuestionVersionNumber(questionBankEntryId)
                .forEach(this::deleteVersionContent);
        entriesRepository.delete(entry);
    }

    //VERSIONI

    @Override
    @Transactional(readOnly = true)
    public List<QuestionVersion> findVersions(long questionBankEntryId) {
        findById(questionBankEntryId);
        return versionsRepository.findByQuestionBankEntry_QuestionBankEntryIdOrderByQuestionVersionNumber(questionBankEntryId);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionVersion findVersionById(long questionVersionId) {
        return versionsRepository.findById(questionVersionId)
                .orElseThrow(() -> new NotFoundException(questionVersionId));
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionVersionResponseDTO findVersionDetails(long questionVersionId) {
        QuestionVersion version = findVersionById(questionVersionId);
        Question question = findQuestion(questionVersionId);
        return QuestionVersionResponseDTO.of(version, question,
                answersRepository.findByQuestion_QuestionIdOrderByAnswerSortOrder(question.getQuestionId()),
                rubricCriteriaRepository.findByQuestion_QuestionIdOrderByCriterionSortOrder(question.getQuestionId()));
    }

    @Override
    @Transactional
    public QuestionVersion createDraftVersion(long questionBankEntryId, QuestionVersionDTO body) {
        QuestionBankEntry entry = findById(questionBankEntryId);
        Content content = validate(body);

        // Due creazioni contemporanee sullo stesso esercizio violano l'unicità del numero -> 409
        QuestionVersion version = versionsRepository.save(QuestionVersion.builder()
                .questionBankEntry(entry)
                .questionVersionNumber(versionsRepository.findMaxVersionNumber(questionBankEntryId) + 1)
                .isPublished(false)
                .build());
        // IDENTITY: l'INSERT parte subito al save, quindi la domanda deve essere già completa
        Question question = Question.builder().questionVersion(version).build();
        applyQuestionFields(question, content);
        questionsRepository.save(question);
        writeAnswersAndCriteria(question, content);
        return version;
    }

    @Override
    @Transactional
    public QuestionVersion updateDraftVersion(long questionVersionId, QuestionVersionDTO body) {
        QuestionVersion version = findVersionById(questionVersionId);
        requireEditable(version);
        Content content = validate(body);

        Question question = findQuestion(questionVersionId);
        // Risposte e rubriche si sostituiscono in blocco: DELETE eseguiti subito, poi i nuovi INSERT
        answersRepository.deleteByQuestionId(question.getQuestionId());
        rubricCriteriaRepository.deleteByQuestionId(question.getQuestionId());
        applyQuestionFields(question, content);
        writeAnswersAndCriteria(question, content);
        return version;
    }

    @Override
    @Transactional
    public QuestionVersion publishVersion(long questionVersionId) {
        QuestionVersion version = findVersionById(questionVersionId);
        if (version.isPublished()) {
            throw new ConflictException("La versione è già pubblicata");
        }
        // Prima si toglie il flag alla versione pubblicata (UPDATE eseguito subito), poi lo si mette a
        // questa: l'indice unico parziale non è DEFERRABLE. Da ora i nuovi tentativi usano questa versione,
        // quelli già avviati conservano la propria (exercise_attempts.question_version_id)
        versionsRepository.unpublishAll(version.getQuestionBankEntry().getQuestionBankEntryId());
        version.setPublished(true);
        version.setPublishedAt(OffsetDateTime.now());
        return version;
    }

    @Override
    @Transactional
    public void deleteDraftVersion(long questionVersionId) {
        QuestionVersion version = findVersionById(questionVersionId);
        requireEditable(version);
        deleteVersionContent(version);
    }

    // Modificabile o cancellabile solo una bozza mai pubblicata e mai svolta
    private void requireEditable(QuestionVersion version) {
        if (version.isPublished() || version.getPublishedAt() != null) {
            throw new ConflictException("Una versione già pubblicata non si modifica: crea una nuova versione");
        }
        if (exerciseAttemptsRepository.existsByQuestionVersion_QuestionVersionId(version.getQuestionVersionId())) {
            throw new ConflictException("La versione è già stata svolta: crea una nuova versione");
        }
    }

    private Question findQuestion(long questionVersionId) {
        return questionsRepository.findByQuestionVersion_QuestionVersionId(questionVersionId)
                .orElseThrow(() -> new NotFoundException("Domanda della versione " + questionVersionId + " non trovata"));
    }

    private void deleteVersionContent(QuestionVersion version) {
        questionsRepository.findByQuestionVersion_QuestionVersionId(version.getQuestionVersionId())
                .ifPresent(question -> {
                    answersRepository.deleteByQuestionId(question.getQuestionId());
                    rubricCriteriaRepository.deleteByQuestionId(question.getQuestionId());
                    questionsRepository.delete(question);
                });
        versionsRepository.delete(version);
    }

    private static void applyQuestionFields(Question question, Content content) {
        question.setKindQuestion(content.kind());
        question.setAssessmentMode(content.assessmentMode());
        question.setPrompt(content.prompt());
        question.setSourceText(content.sourceText());
        question.setGeneralExplanation(content.generalExplanation());
    }

    private void writeAnswersAndCriteria(Question question, Content content) {
        // L'ordine della lista è l'ordine di presentazione (nei nuovi tentativi le opzioni si mescolano)
        List<AnswerDTO> answers = content.answers();
        for (int i = 0; i < answers.size(); i++) {
            AnswerDTO answer = answers.get(i);
            answersRepository.save(QuestionAnswer.builder()
                    .question(question)
                    .answerSortOrder(i + 1)
                    .answerText(answer.text().strip())
                    // Fuori dalla scelta multipla ogni risposta è una soluzione accettata
                    .isCorrect(content.kind() != KindQuestion.multiple_choice || answer.correct())
                    .feedback(blankToNull(answer.feedback()))
                    .build());
        }

        List<RubricCriterionDTO> criteria = content.rubricCriteria();
        for (int i = 0; i < criteria.size(); i++) {
            RubricCriterionDTO criterion = criteria.get(i);
            rubricCriteriaRepository.save(QuestionRubricCriterion.builder()
                    .question(question)
                    .criterionSortOrder(i + 1)
                    .category(criterion.category())
                    .description(criterion.description().strip())
                    .build());
        }
    }

    // Regole per tipo (blueprint §6.2): validate qui per rispondere 400 invece di far fallire i CHECK del DB
    private static Content validate(QuestionVersionDTO body) {
        KindQuestion kind = body.kind();
        String sourceText = blankToNull(body.sourceText());
        String explanation = blankToNull(body.generalExplanation());
        List<AnswerDTO> answers = body.answers() == null ? List.of() : body.answers();
        List<RubricCriterionDTO> criteria = body.rubricCriteria() == null ? List.of() : body.rubricCriteria();
        boolean translation = kind == KindQuestion.sentence_translation || kind == KindQuestion.passage_translation;

        AssessmentMethod mode = switch (kind) {
            case multiple_choice, fill_in -> requireMode(body.assessmentMode(), AssessmentMethod.automatic, kind);
            case passage_translation -> requireMode(body.assessmentMode(), AssessmentMethod.teacher, kind);
            case sentence_translation -> {
                if (body.assessmentMode() == null) {
                    throw new BadRequestException("La traduzione di frase richiede assessmentMode: automatic o teacher");
                }
                yield body.assessmentMode();
            }
        };

        if (translation && (sourceText == null || explanation == null)) {
            throw new BadRequestException("Le traduzioni richiedono sourceText e generalExplanation");
        }
        if (!translation && sourceText != null) {
            throw new BadRequestException("sourceText è ammesso solo per le traduzioni");
        }
        if (kind != KindQuestion.passage_translation && !criteria.isEmpty()) {
            throw new BadRequestException("Le rubriche sono ammesse solo per la traduzione di versione");
        }

        switch (kind) {
            case multiple_choice -> {
                if (answers.size() < 2) {
                    throw new BadRequestException("La scelta multipla richiede almeno 2 opzioni");
                }
                if (answers.stream().filter(AnswerDTO::correct).count() != 1) {
                    throw new BadRequestException("La scelta multipla ha una sola risposta corretta");
                }
            }
            case fill_in -> {
                if (answers.isEmpty()) {
                    throw new BadRequestException("Il completamento richiede almeno una risposta accettata");
                }
            }
            case sentence_translation -> {
                // Correzione automatica: serve almeno una traduzione accettata con cui confrontare
                if (mode == AssessmentMethod.automatic && answers.isEmpty()) {
                    throw new BadRequestException("La traduzione di frase automatica richiede almeno una traduzione accettata");
                }
            }
            case passage_translation -> {
                // Soluzioni di riferimento facoltative: corregge la docente
            }
        }

        return new Content(kind, mode, body.prompt().strip(), sourceText, explanation, answers, criteria);
    }

    // Per i tipi a modalità fissa assessmentMode si può omettere; se indicato deve coincidere
    private static AssessmentMethod requireMode(AssessmentMethod requested, AssessmentMethod fixed, KindQuestion kind) {
        if (requested != null && requested != fixed) {
            throw new BadRequestException("Per " + kind + " la valutazione è sempre " + fixed);
        }
        return fixed;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    // Contenuto già validato e normalizzato
    private record Content(KindQuestion kind, AssessmentMethod assessmentMode, String prompt, String sourceText,
                           String generalExplanation, List<AnswerDTO> answers,
                           List<RubricCriterionDTO> rubricCriteria) {
    }
}
