package giuliacrepaldi.BStudent_Management.repositories.progression;

import giuliacrepaldi.BStudent_Management.entities.progression.StudyEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyEventsRepository extends JpaRepository<StudyEvent, Long> {

    // Lezioni del Quaderno riaperte dopo l'ingresso: le altre sono "Nuovo" (blueprint §14)
    @Query(value = """
            SELECT DISTINCT nl.lesson_id
              FROM notebook_lessons nl
              JOIN study_events e ON e.student_id = nl.student_id AND e.lesson_id = nl.lesson_id
             WHERE nl.student_id = :studentId AND e.kind = 'lesson_read' AND e.started_at >= nl.acquired_at
            """, nativeQuery = true)
    List<Long> findReopenedNotebookLessonIds(@Param("studentId") Long studentId);

    // Tempo di studio per giorno (ora italiana, AAAA-MM-GG) dal giorno :from (blueprint §10.5).
    // Teoria = tempo attivo delle letture. Esercizi = inizio -> consegna dei tentativi consegnati, al massimo il
    // tempo concesso (missioni) o 60 minuti (Palestra). Righe: giorno, ms di teoria, ms di esercizi
    @Query(value = """
            SELECT day, SUM(theory_ms), SUM(exercise_ms)
              FROM (SELECT to_char((e.started_at AT TIME ZONE 'Europe/Rome')::date, 'YYYY-MM-DD') AS day,
                           e.active_ms AS theory_ms, 0 AS exercise_ms
                      FROM study_events e
                     WHERE e.student_id = :studentId AND e.active_ms IS NOT NULL
                       AND (e.started_at AT TIME ZONE 'Europe/Rome')::date >= CAST(:from AS date)
                    UNION ALL
                    SELECT to_char((a.submitted_at AT TIME ZONE 'Europe/Rome')::date, 'YYYY-MM-DD'),
                           0,
                           LEAST(GREATEST(extract(epoch FROM a.submitted_at - a.started_at) * 1000, 0),
                                 COALESCE(a.allowed_duration_ms, 3600000))
                      FROM exercise_attempts a
                     WHERE a.student_id = :studentId AND a.submitted_at IS NOT NULL
                       AND (a.submitted_at AT TIME ZONE 'Europe/Rome')::date >= CAST(:from AS date)) t
             GROUP BY day
             ORDER BY day
            """, nativeQuery = true)
    List<Object[]> findStudyTimeByDay(@Param("studentId") Long studentId, @Param("from") String from);
}
