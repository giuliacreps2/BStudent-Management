package giuliacrepaldi.BStudent_Management.services.attempts.interfaces;

/**
 * Svolgimento delle missioni (blueprint §9).
 * Almeno una vita per avviare, ritentare o riprendere; una sola missione in corso per studente.
 * Esercizi in ordine; timer verificato dal server, avviato alla fine della lezione precedente.
 * Salvataggio e ripresa senza perdita di vite; fallimento (sotto 60%, tempo scaduto, abbandono) con una sola vita persa per tentativo.
 * Checkpoint conservati nei ritentativi dopo errore o scadenza; l'abbandono riparte da zero.
 * TODO "Annulla invio" (§9.4): esercizio non superato ma missione non fallita; vite e punto di ripresa ancora da decidere.
 */
public interface IMissionRunsService {
}
