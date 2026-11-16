package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.gamification.HintChargesDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.HintDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.HintReviewLinkDTO;

import java.util.List;

/**
 * Aiuto del cappello (blueprint principale §11.3; Blueprint Gamification §6.2).
 * Solo nelle missioni, con il cappello indossato. L'aiuto dice l'argomento dell'esercizio e mostra
 * l'"In breve" della Risorsa collegata a quell'argomento, anche se la Risorsa è premium.
 * Un nuovo aiuto consuma una carica; riaprirlo nello stesso tentativo è gratuito.
 * 5 cariche; ricarica completa 12 ore dopo l'esaurimento dell'ultima.
 *
 * <p>TODO dopo la beta: catalogo di suggerimenti a partire dagli errori degli studenti; aiuti premium (§6.2).
 */
public interface IHintsService {

    // Applica prima la ricarica scaduta
    HintChargesDTO getCharges(long studentId);

    // Tentativo in corso e dello studente; esercizio della missione del tentativo; cappello indossato.
    // Esercizio senza argomento: errore, senza consumare cariche. Già aperto in questo tentativo: stesso
    // aiuto, nessuna carica. Senza cariche: errore con l'ora della ricarica
    HintDTO useHint(long studentId, long missionAttemptId, long missionExerciseId);

    // A tentativo chiuso: per ogni aiuto usato, argomento e Risorsa da ripassare
    List<HintReviewLinkDTO> getReviewLinks(long studentId, long missionAttemptId);
}
