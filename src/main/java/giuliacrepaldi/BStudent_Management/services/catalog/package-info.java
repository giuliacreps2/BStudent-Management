/**
 * <p>
 * Da rispettare fin da ora:
 * - Niente delete fisico di contenuti già pubblicati: si ritirano.
 * - Le versioni pubblicate non si modificano: per correggere si crea una nuova versione. Altrimenti lo storico dei tentativi perde il riferimento a cosa è stato svolto.
 * - Cambiare tempo, punti o pesi di un esercizio collegato non rompe nulla: il tentativo salva i suoi valori (allowed_duration_ms, configuration_snapshot), e i checkpoint sono legati all'esercizio, non alla sua posizione.
 * <p>
 * <p>
 * Regole comuni a tutte:
 * - Un contenuto nuovo nasce draft.
 * - publish porta da draft o withdrawn a published; withdraw porta da published a withdrawn.
 * - Il delete fisico è ammesso solo se il contenuto non è mai stato pubblicato, altrimenti ConflictException.
 * - I figli si leggono dal loro contenitore (findByCourse, findByMission…), in List ordinate. Page resta solo dove gli elenchi possono crescere: materie, corsi, esercizi.
 * - Ho tolto public, che nelle interfacce è ridondante.
 */
package giuliacrepaldi.BStudent_Management.services.catalog;