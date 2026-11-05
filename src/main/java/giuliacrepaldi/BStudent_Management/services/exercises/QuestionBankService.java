package giuliacrepaldi.BStudent_Management.services.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionVersion;
import giuliacrepaldi.BStudent_Management.payloads.exercises.QuestionBankEntryDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.QuestionVersionDTO;
import giuliacrepaldi.BStudent_Management.services.exercises.interfaces.IQuestionBankService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionBankService implements IQuestionBankService {

    @Override
    public QuestionBankEntry saveNewQuestionBankEntry(QuestionBankEntryDTO body) {
        return null;
    }

    @Override
    public QuestionBankEntry findById(long questionBankEntryId) {
        return null;
    }

    @Override
    public Page<QuestionBankEntry> findAll(int page, int size, String sortBy) {
        return null;
    }

    @Override
    public QuestionBankEntry findByIdAndUpdate(long questionBankEntryId, QuestionBankEntryDTO body) {
        return null;
    }

    @Override
    public QuestionBankEntry publishQuestionBankEntry(long questionBankEntryId) {
        return null;
    }

    @Override
    public QuestionBankEntry withdrawQuestionBankEntry(long questionBankEntryId) {
        return null;
    }

    @Override
    public void deleteQuestionBankEntry(long questionBankEntryId) {

    }

    @Override
    public List<QuestionVersion> findVersions(long questionBankEntryId) {
        return List.of();
    }

    @Override
    public QuestionVersion findVersionById(long questionVersionId) {
        return null;
    }

    @Override
    public QuestionVersion createDraftVersion(long questionBankEntryId, QuestionVersionDTO body) {
        return null;
    }

    @Override
    public QuestionVersion updateDraftVersion(long questionVersionId, QuestionVersionDTO body) {
        return null;
    }

    @Override
    public QuestionVersion publishVersion(long questionVersionId) {
        return null;
    }

    @Override
    public void deleteDraftVersion(long questionVersionId) {

    }
}
