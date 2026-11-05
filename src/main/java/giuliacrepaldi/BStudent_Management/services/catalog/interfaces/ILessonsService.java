package giuliacrepaldi.BStudent_Management.services.catalog.interfaces;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.LessonPage;
import giuliacrepaldi.BStudent_Management.payloads.catalog.LessonDTO;
import giuliacrepaldi.BStudent_Management.payloads.catalog.LessonPageDTO;

import java.util.List;

/**
 * Lezioni della missione e loro pagine ordinate (blueprint §5.1).
 * Ordine positivo e unico nella missione; blocchi testo, immagine e video coerenti col tipo.
 * Riordino atomico con gli ID del contenitore, senza duplicati.
 * <p>
 * Aggiunti reorderLessons e i metodi delle pagine: creazione in coda, lettura in ordine, modifica, riordino, delete fisico. Le pagine non hanno uno stato proprio e nessun progresso le usa come riferimento.
 * - publishLesson è ammesso solo se la lezione ha un esercizio collegato.
 */
public interface ILessonsService {

    //Admin
    //CRUD -> create, read, update, delete

    Lesson saveNewLesson(long missionId, LessonDTO body); // nasce draft, in coda all'ordine

    Lesson findById(long lessonId);

    List<Lesson> findByMission(long missionId); // ordinate per sortOrder

    Lesson findByIdAndUpdate(long lessonId, LessonDTO body);

    // Tutti e soli gli ID delle lezioni della missione, nel nuovo ordine
    List<Lesson> reorderLessons(long missionId, List<Long> lessonIds);

    Lesson publishLesson(long lessonId); // draft/withdrawn -> published, solo con esercizio collegato

    Lesson withdrawLesson(long lessonId); // published -> withdrawn

    void deleteLesson(long lessonId); // solo draft mai pubblicata, altrimenti ConflictException

    //Pagine della lezione: nessuno stato proprio, seguono la lezione

    LessonPage saveNewLessonPage(long lessonId, LessonPageDTO body); // in coda all'ordine

    List<LessonPage> findPagesByLesson(long lessonId); // ordinate per sortOrder

    LessonPage findPageByIdAndUpdate(long lessonPageId, LessonPageDTO body);

    // Tutti e soli gli ID delle pagine della lezione, nel nuovo ordine
    List<LessonPage> reorderLessonPages(long lessonId, List<Long> lessonPageIds);

    void deleteLessonPage(long lessonPageId); // nessun riferimento dai progressi: cancellazione fisica
}
