package giuliacrepaldi.BStudent_Management.services.progression.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.progression.StudyEventDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.StudyTimeDTO;

/**
 * Registro di studio (blueprint §10.5, tabella study_events della v0.9).
 * Dati raccolti da subito per gli algoritmi futuri (consigli per lo studente, statistiche anonime della
 * piattaforma); l'algoritmo arriva dopo la beta. Si aggiunge e non si modifica. I tentativi degli
 * esercizi hanno già i loro orari e non passano da qui.
 * Statistiche della piattaforma solo aggregate e anonime (TODO legale nel package-info di services.orientation).
 * Controller: POST /student/study-events, GET /student/progress/study-time.
 */
public interface IStudyEventsService {

    // Lettura di una lezione o di una Risorsa (tratto di tempo attivo) oppure consiglio della Palestra aperto.
    // L'oggetto deve essere visibile allo studente; startedAt non nel futuro; activeMs > 0 e non più lungo
    // del tempo trascorso da startedAt
    void record(long studentId, StudyEventDTO body);

    // Tempo di studio degli ultimi `days` giorni (oggi compreso, ora italiana): teoria (eventi) + esercizi
    // (inizio -> consegna, limitato al tempo concesso o a 60 minuti); minuti per giorno di attività.
    // Aggiunto all'MVP il 21/11/2026 (era facoltativo)
    StudyTimeDTO getStudyTime(long studentId, int days);
}
