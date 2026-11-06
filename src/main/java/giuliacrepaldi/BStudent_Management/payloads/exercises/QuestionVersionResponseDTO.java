package giuliacrepaldi.BStudent_Management.payloads.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.Question;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionAnswer;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionRubricCriterion;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionVersion;
import giuliacrepaldi.BStudent_Management.enums.attempts.AssessmentMethod;
import giuliacrepaldi.BStudent_Management.enums.exercises.KindQuestion;
import giuliacrepaldi.BStudent_Management.enums.exercises.RubricCategory;

import java.time.OffsetDateTime;
import java.util.List;

// Vista editoriale dell'Admin: include soluzioni e rubriche, mai da esporre allo studente
public record QuestionVersionResponseDTO(
        Long questionVersionId,
        Long questionBankEntryId,
        int versionNumber,
        boolean published,
        OffsetDateTime publishedAt,
        KindQuestion kind,
        AssessmentMethod assessmentMode,
        String prompt,
        String sourceText,
        String generalExplanation,
        List<Answer> answers,
        List<RubricCriterion> rubricCriteria) {

    public record Answer(Long questionAnswerId, int sortOrder, String text, boolean correct, String feedback) {
    }

    public record RubricCriterion(Long questionRubricCriterionId, int sortOrder, RubricCategory category,
                                  String description) {
    }

    public static QuestionVersionResponseDTO of(QuestionVersion version, Question question,
                                                List<QuestionAnswer> answers,
                                                List<QuestionRubricCriterion> criteria) {
        return new QuestionVersionResponseDTO(
                version.getQuestionVersionId(),
                version.getQuestionBankEntry().getQuestionBankEntryId(),
                version.getQuestionVersionNumber(),
                version.isPublished(),
                version.getPublishedAt(),
                question.getKindQuestion(),
                question.getAssessmentMode(),
                question.getPrompt(),
                question.getSourceText(),
                question.getGeneralExplanation(),
                answers.stream()
                        .map(a -> new Answer(a.getQuestionAnswerId(), a.getAnswerSortOrder(), a.getAnswerText(),
                                a.isCorrect(), a.getFeedback()))
                        .toList(),
                criteria.stream()
                        .map(c -> new RubricCriterion(c.getQuestionRubricCriterionId(), c.getCriterionSortOrder(),
                                c.getCategory(), c.getDescription()))
                        .toList());
    }
}
