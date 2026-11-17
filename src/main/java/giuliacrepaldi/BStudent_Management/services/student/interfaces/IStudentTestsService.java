package giuliacrepaldi.BStudent_Management.services.student.interfaces;

import giuliacrepaldi.BStudent_Management.enums.orientation.OrientationTestKind;
import giuliacrepaldi.BStudent_Management.payloads.student.StudentTestDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.StudentTestResultDTO;

import java.util.Map;

/**
 * Test dello studente (blueprint §8): orientamento della Palestra e livello del corso. Sono due test diversi,
 * facoltativi, uno per materia e per tipo. Il risultato si calcola qui; risposte e risultato si salvano come
 * fotografia a ogni svolgimento (§8.3). La gestione Admin sta in IOrientationService.
 *
 * <p>Il test di livello assegna anche il livello di partenza delle skill della materia
 * (Blueprint Gamification §2.6): Principiante 1, Intermedio 2, Avanzato 3; può solo salire.
 * TODO da valutare: un livello di partenza anche dal test di orientamento. Oggi non lo assegna: le sue
 * risposte consigliano esercizi, non hanno un "giusto o sbagliato" da cui ricavare un livello.
 */
public interface IStudentTestsService {

    // Solo un test senza problemi (almeno 3 domande, ognuna con almeno 2 opzioni); altrimenti null:
    // il frontend non mostra il link al test
    StudentTestDTO getTest(String subjectSlug, OrientationTestKind kind);

    // Tutte le domande, un'opzione per domanda, opzioni del test: altrimenti errore.
    // Livello: il più basso scelto almeno due volte, altrimenti il più basso (§8.2).
    // Orientamento: al massimo 5 voci visibili; prima le più consigliate, poi priorità, poi ID (§8.1)
    StudentTestResultDTO submit(long studentId, String subjectSlug, OrientationTestKind kind, Map<Long, Long> answers);

    // Ultimo svolgimento dello studente; null se non l'ha mai fatto
    StudentTestResultDTO getLastResult(long studentId, String subjectSlug, OrientationTestKind kind);
}
