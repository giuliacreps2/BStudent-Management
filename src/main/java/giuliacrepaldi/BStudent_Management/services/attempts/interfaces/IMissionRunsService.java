package giuliacrepaldi.BStudent_Management.services.attempts.interfaces;

/**
 * Svolgimento delle missioni (blueprint principale §9, §12.1; Blueprint Svolgimento §1, §2, §3, §6).
 * Almeno una vita per avviare, ritentare o riprendere; l'ingresso non consuma la vita. Una sola missione in
 * corso per studente, inclusa una salvata. Passi uno alla volta nell'ordine della sequenza; dopo un
 * esercizio si procede solo se è superato. Il tempo parte quando l'esercizio compare ed è verificato dal
 * server. Salvare o chiudere la pagina non fa perdere vite. Tentativo fallito (sotto la soglia, tempo
 * scaduto, abbandono): una sola vita persa. Gli esercizi superati restano come checkpoint nei ritentativi;
 * l'abbandono riparte da zero.
 *
 * <h2>TODO — metodi da implementare (Blocco 5)</h2>
 * <pre>
 * MissionPlayDTO start(long studentId, String courseSlug, String missionSlug);
 *     // missione visibile, accessibile (premium) e attuale o già completata (stato dall'ordine, come in
 *     // IStudentMissionsService); almeno una vita (ILivesService.canStartMission), altrimenti
 *     // ConflictException con l'ora della ricarica. La stessa missione già in corso -&gt; la riprende.
 *     // Un'altra missione in corso -&gt; ConflictException "Hai già una missione in corso: [titolo]".
 *     // Dopo un tentativo fallito crea il tentativo successivo dello stesso percorso, con i checkpoint
 * MissionPlayDTO getCurrent(long studentId);
 *     // la missione in corso o salvata, null se non c'è: per "Continua la missione" nella home
 * MissionPlayDTO getState(long studentId, long missionAttemptId);
 *     // passi con stato (fatto / attuale / da fare) e passo attuale: pagine della lezione oppure esercizio
 *     // (domanda senza soluzione, tempo residuo). Prima applica le scadenze: tempo finito -&gt; tentativo
 *     // fallito. Quando il passo attuale è un esercizio non ancora aperto, lo apre e fa partire il tempo
 * MissionPlayDTO completeLesson(long studentId, long missionAttemptId, long lessonId);
 *     // "Ho finito di leggere": la lezione entra nel Quaderno (INotebookService.acquireLesson) e si passa
 *     // al passo successivo. Solo la lezione attuale; ripetuta non ha effetti
 * void sync(long studentId, long exerciseAttemptId, AnswerDTO body);
 *     // salvataggio automatico durante l'esercizio, ogni pochi secondi: conserva la risposta e segna che
 *     // lo studente è ancora collegato. Il tempo continua a scorrere
 * MissionPlayDTO pause(long studentId, long missionAttemptId, AnswerDTO body);
 *     // "Salva missione" o chiusura della pagina: tempo fermo, stato salvato, nessuna vita persa.
 *     // Disconnessione senza avviso: se le sincronizzazioni si interrompono, il server considera la missione
 *     // salvata all'ultima ricevuta (tolleranza in MissionTimer), così non si perde tempo né una vita
 * MissionPlayDTO resume(long studentId, long missionAttemptId);
 *     // almeno una vita; stesso punto, stessa risposta, tempo residuo
 * MissionStepOutcomeDTO submitExercise(long studentId, long exerciseAttemptId, AnswerDTO body);
 *     // il tempo si ferma. Correzione automatica: superato -&gt; checkpoint e passo successivo; era l'ultimo
 *     // -&gt; missione superata (IMissionProgressService.onMissionPassed). Non superato -&gt; tentativo fallito,
 *     // una vita persa (ILivesService.onMissionAttemptFailed); si riparte da quell'esercizio con risposta
 *     // vuota e tempo completo. Correzione della docente (solo ultimo esercizio, §1): missione in attesa
 *     // di correzione. Punti skill a ogni esercizio valutato, anche se non superato (§5)
 * MissionPlayDTO cancelSubmission(long studentId, long exerciseAttemptId);
 *     // "Annulla invio" (§2): solo senza esito; nessuna vita persa; risposta modificabile e tempo com'era
 *     // al momento dell'invio; la missione torna in corso
 * void abandon(long studentId, long missionAttemptId);
 *     // "Abbandona missione": tentativo fallito, una vita persa, percorso chiuso: la prossima volta si
 *     // riparte da zero. Non permesso in attesa della docente (§1.4)
 * </pre>
 * Esercizio ritirato dal banco durante la missione (§6): nessuna vita persa, il passo viene saltato; se era
 * l'ultimo esercizio la missione risulta superata con quelli rimasti.
 * Calcoli puri da creare, con i test (utilities.MissionTimer): tempo residuo da inizio, ultima sincronizzazione
 * e tolleranza; scadenza. Aiuto del cappello: già in IHintsService, si usa durante l'esercizio.
 * DTO da creare: MissionPlayDTO (stato del tentativo, passi, passo attuale, vite, cariche del cappello),
 * LessonPlayDTO (pagine nel formato a blocchi), MissionStepOutcomeDTO (esito dell'esercizio secondo il §3,
 * che cosa è successo: vita persa, missione superata, XP, evoluzione, corso completato; passo successivo).
 * Controller: /student/missions/**.
 */
public interface IMissionRunsService {
}
