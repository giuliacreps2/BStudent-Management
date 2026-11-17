package giuliacrepaldi.BStudent_Management.services.progression.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseOutcomeDTO;

/**
 * Record e punti della Palestra (blueprint principale §10.1).
 * Contributo = punti massimi x percentuale / 100, a due decimali, solo da 60%.
 * Un nuovo record accredita soltanto la differenza positiva rispetto al contributo acquisito.
 * Esempio: massimo 10. Primo 87% -> +8,70. Poi 89% -> +0,20. Poi 80% -> nulla. Poi 100% -> +1,10.
 */
public interface IPracticeProgressService {

    // Dopo ogni giudizio di un tentativo della Palestra (automatico o della docente):
    // 1. sotto la soglia: solo storico, nessun accredito;
    // 2. nuovo record: aggiorna practice_records e accredita la differenza nella skill della voce, nella
    //    materia dell'argomento;
    // 3. vite del tris e della sfida, Serie e pausa;
    // 4. evoluzioni.
    // Restituisce l'esito con i punti accreditati e i premi. Ripetuta per lo stesso tentativo: nessun doppio effetto
    ExerciseOutcomeDTO onAssessed(long studentId, long exerciseAttemptId);
}
