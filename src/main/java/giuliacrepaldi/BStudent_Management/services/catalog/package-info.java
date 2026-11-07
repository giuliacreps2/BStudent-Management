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
 * <p>
 * con "riordino"
 * <p>
 * Sono tutte e due le cose: cambi l'ordine, e di conseguenza la numerazione. Ogni missione, passo della missione e pagina ha un numero sortOrder (1, 2, 3…) che decide in che ordine la vede lo studente. Dalla v0.8 lezioni ed esercizi della missione sono passi di un'unica sequenza (mission_steps): si riordinano insieme con PUT .../missions/{id}/steps/order.
 * <p>
 * Con il riordino mandi la lista degli ID nel nuovo ordine, e il backend riassegna i numeri da 1 in su:
 * prima:  L1 = 1, L2 = 2, L3 = 3
 * PUT .../steps/order   { "ids": [3, 1, 2] }
 * dopo:   L3 = 1, L1 = 2, L2 = 3
 * Nel frontend è il trascinamento (drag & drop) di un elenco: alla fine si manda l'ordine completo.
 * <p>
 * Qui entra in gioco il vincolo DEFERRABLE. Due lezioni della stessa missione non possono avere lo stesso numero. Se aggiorni una riga alla volta, a metà dello scambio L3 è già 1 mentre L1 è ancora 1, e il DB rifiuterebbe la modifica. Con SET CONSTRAINTS ... DEFERRED il controllo avviene solo alla fine, quando i numeri sono di nuovo tutti diversi. Il backend chiede la lista completa proprio per rinumerare tutto in una volta, senza buchi e senza doppioni.
 *
 */
package giuliacrepaldi.BStudent_Management.services.catalog;