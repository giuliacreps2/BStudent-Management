package giuliacrepaldi.BStudent_Management.services.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.LessonPage;
import giuliacrepaldi.BStudent_Management.payloads.catalog.LessonDTO;
import giuliacrepaldi.BStudent_Management.payloads.catalog.LessonPageDTO;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.ILessonsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LessonsService implements ILessonsService {


    @Override
    public Lesson saveNewLesson(long missionId, LessonDTO body) {
        return null;
    }

    @Override
    public Lesson findById(long lessonId) {
        return null;
    }

    @Override
    public List<Lesson> findByMission(long missionId) {
        return List.of();
    }

    @Override
    public Lesson findByIdAndUpdate(long lessonId, LessonDTO body) {
        return null;
    }

    @Override
    public List<Lesson> reorderLessons(long missionId, List<Long> lessonIds) {
        return List.of();
    }

    @Override
    public Lesson publishLesson(long lessonId) {
        return null;
    }

    @Override
    public Lesson withdrawLesson(long lessonId) {
        return null;
    }

    @Override
    public void deleteLesson(long lessonId) {

    }

    @Override
    public LessonPage saveNewLessonPage(long lessonId, LessonPageDTO body) {
        return null;
    }

    @Override
    public List<LessonPage> findPagesByLesson(long lessonId) {
        return List.of();
    }

    @Override
    public LessonPage findPageByIdAndUpdate(long lessonPageId, LessonPageDTO body) {
        return null;
    }

    @Override
    public List<LessonPage> reorderLessonPages(long lessonId, List<Long> lessonPageIds) {
        return List.of();
    }

    @Override
    public void deleteLessonPage(long lessonPageId) {

    }
}
