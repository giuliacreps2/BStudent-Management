/**
 * Accessori, aiuti del cappello, vite e attività per ottenere vite (blueprint §11, §12).
 *
 * <h2>TODO</h2>
 * <ol>
 *   <li><b>Ricompense nella pagina del corso</b> (deciso il 9/11/2026, blueprint §5.8). Il mockup
 *       ({@code CourseDetailData.rewards} in bstudent-academy/types/myCourses.ts) mostra skin, badge,
 *       sfondo, titolo e una ricompensa "misteriosa"; nel DB esistono solo skin ed evoluzioni
 *       ({@code entities.skills}) e accessori ({@code entities.gamification}). Da decidere in questo blocco:
 *       quali tipi di ricompensa esistono davvero, come si legano al corso, quando si sbloccano e se
 *       servono nuove tabelle. Fino ad allora la lettura del corso restituisce una lista vuota e il
 *       frontend nasconde la sezione.</li>
 *   <li><b>Personaggi della landing pubblica</b> ({@code CourseLandingCharactersData}, con bonus e
 *       "Sblocca con 500 XP"): oggi testo statico nel frontend. Quando skin e XP saranno definiti,
 *       allineare nomi, bonus e condizioni di sblocco ai dati veri, o leggerli dal backend.</li>
 *   <li><b>XP, livelli delle skill ed evoluzione della skin</b> (principi confermati il 14/11/2026,
 *       proposta nel blueprint §10.4-bis): XP = punti skin; i punti skill fanno salire il livello della
 *       skill, che sblocca esercizi più difficili e contribuisce all'evoluzione. Da fissare: curva delle
 *       soglie di {@code skill_levels}, livello di difficoltà delle voci Palestra (sbloccate fino a
 *       livello skill + 1), soglia XP e livello medio minimo per ogni stadio di {@code skin_evolutions}.
 *       <br>Deciso il 14/11/2026: nell'MVP gli XP sono <b>dello studente</b>, con un contatore unico.
 *       Tutti i personaggi posseduti mostrano lo stesso stadio e cambiare personaggio non fa perdere nulla.
 *       <b>Dopo la beta (roadmap):</b> evoluzioni proprie di ogni personaggio, come nei giochi di
 *       combattimento, con XP o percorsi separati. Costa molte illustrazioni, per questo è rimandato.
 *       Prima di farlo, decidere come passare dal contatore unico a quelli separati: per esempio, gli
 *       XP già guadagnati vanno al personaggio in uso.</li>
 *   <li><b>Bonus della skin e premio della sfida giornaliera</b> (blueprint §7.3): il mockup mostra bonus
 *       come "+5% Grammatica" legati alla skin, non previsti dal blueprint; la sfida giornaliera
 *       ("supera 3 esercizi oggi") per ora non dà premi. Decidere se e come esistono.</li>
 *   <li><b>Livello e XP dello studente nella pagina del corso</b> ({@code CourseDetailData.studentLevel}):
 *       non restituiti nel Blocco 4, da definire insieme alla progressione (blueprint §10).</li>
 * </ol>
 */
package giuliacrepaldi.BStudent_Management.services.gamification;
