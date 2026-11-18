package giuliacrepaldi.BStudent_Management.services.attempts.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.attempts.AnswerDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.CurrentMissionDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.MissionPlayDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.MissionStepOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.MissionSyncDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;

import java.util.List;

/**
 * Svolgimento delle missioni (blueprint principale §9, §12.1; Blueprint Svolgimento §1, §2, §3, §6, §9).
 *
 * <ul>
 *   <li>Almeno una vita per avviare, ritentare o riprendere; l'ingresso non consuma la vita.</li>
 *   <li>Una missione in corso per materia (§9); una in attesa della docente non conta.</li>
 *   <li>Passi uno alla volta nell'ordine della sequenza; dopo un esercizio si procede solo se è superato.
 *       A che punto è lo studente lo dicono le lezioni lette nel percorso e i checkpoint (utilities.MissionPath).</li>
 *   <li>Il tempo parte quando l'esercizio compare ed è verificato dal server (utilities.PlayRules.timer).
 *       Il frontend sincronizza la risposta ogni pochi secondi; se le sincronizzazioni si interrompono per più
 *       della tolleranza (45 secondi) la missione va in pausa da sola, senza perdere vite.</li>
 *   <li>Tentativo fallito (sotto la soglia, tempo scaduto, abbandono): una sola vita persa. Gli esercizi superati
 *       e le lezioni lette restano nei ritentativi; l'abbandono chiude il percorso e si riparte da zero.</li>
 *   <li>Esercizio ritirato o nascosto dall'autrice (§6): l'esercizio aperto si chiude senza esito, il passo
 *       viene saltato, nessuna vita persa; se era l'ultimo la missione è superata con quelli rimasti.</li>
 * </ul>
 * Ogni azione su un tentativo di un altro studente risponde come se il tentativo non esistesse (404).
 */
public interface IMissionRunsService {

    // Missione visibile, attuale o già completata, accessibile. La stessa missione in pausa o in corso: la
    // riprende. Ultimo tentativo fallito: crea il tentativo successivo dello stesso percorso. In attesa della
    // docente: ConflictException. Un'altra missione della stessa materia in corso: ConflictException con il titolo.
    // Senza vite: ConflictException con l'ora della ricarica
    MissionPlayDTO start(long studentId, String courseSlug, String missionSlug);

    // "Continua la missione" nella home: i percorsi aperti, uno per materia
    List<CurrentMissionDTO> getCurrent(long studentId);

    // Prima applica le scadenze (tempo finito, collegamento interrotto); poi, se il passo attuale è un esercizio
    // non ancora aperto, lo apre e fa partire il tempo
    MissionPlayDTO getState(long studentId, long missionAttemptId);

    // "Ho finito di leggere": solo la lezione attuale; la lezione entra nel Quaderno. Ripetuta: nessun effetto
    MissionStepOutcomeDTO completeLesson(long studentId, long missionAttemptId, long lessonId);

    // Salvataggio automatico durante l'esercizio: conserva la risposta e segna che lo studente è collegato
    MissionSyncDTO sync(long studentId, long exerciseAttemptId, AnswerDTO body);

    // "Salva missione" o chiusura della pagina (body facoltativo): tempo fermo, nessuna vita persa
    MissionPlayDTO pause(long studentId, long missionAttemptId, AnswerDTO body);

    // Almeno una vita; stesso punto, stessa risposta, tempo residuo
    MissionPlayDTO resume(long studentId, long missionAttemptId);

    // Il tempo si ferma. Correzione automatica: superato -> checkpoint e passo successivo (o missione superata);
    // non superato -> tentativo fallito, una vita persa. Correzione della docente -> in attesa. Punti skill a ogni
    // esercizio valutato, anche non superato (§5). Doppio invio: stesso esito, nessun doppio effetto
    MissionStepOutcomeDTO submitExercise(long studentId, long exerciseAttemptId, AnswerDTO body);

    // "Annulla invio" (§2): solo senza esito; nessuna vita persa; risposta modificabile e tempo com'era all'invio;
    // la missione torna in pausa ("Riprendi")
    MissionPlayDTO cancelSubmission(long studentId, long exerciseAttemptId);

    // "Abbandona missione": tentativo fallito, una vita persa, percorso chiuso. Dopo un tentativo già fallito chiude
    // solo il percorso. Non permesso in attesa della docente (§1.4)
    LivesDTO abandon(long studentId, long missionAttemptId);

    // Da chiamare dalla pagina "Consegne da correggere" dopo aver salvato il giudizio della docente (§1.5):
    // superato -> checkpoint e missione superata; sotto la soglia -> tentativo fallito e una vita persa.
    // Già applicato: nessun effetto
    MissionStepOutcomeDTO onTeacherAssessed(long exerciseAttemptId);
}
