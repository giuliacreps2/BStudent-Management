/**
 * Gamification: livelli delle skill, XP ed evoluzioni, personaggi, accessori e aiuto del cappello, vite,
 * sfida giornaliera e Serie.
 *
 * <p>Regole: {@code blueprint/BStudent_Blueprint_Gamification_v0.1.md} (decisioni del 16/11/2026), che
 * aggiorna il blueprint principale §10–§12. Le regole sono <b>calcoli sui fatti già registrati</b>
 * (accrediti delle skill, record, missioni superate): il Blocco 5 dovrà solo registrare i fatti e chiamare
 * gli eventi {@code on...} descritti nelle interfacce.
 *
 * <h2>Service</h2>
 * <ul>
 *   <li>{@code ISkillLevelsService}: livelli delle skill per materia, sblocco delle voci per difficoltà.</li>
 *   <li>{@code IStudentXpService}: XP dello studente, stadi raggiunti.</li>
 *   <li>{@code ISkinsService} (in services.skills): personaggi, scelta e cambio, Admin.</li>
 *   <li>{@code IAccessoriesService}, {@code IHintsService}: cappello da turista e suo aiuto.</li>
 *   <li>{@code ILivesService}: saldo, ricarica, vite extra.</li>
 *   <li>{@code IDailyGoalsService}: sfida giornaliera, tris per una vita, Serie, pausa.</li>
 *   <li>{@code IStudentGameStateService}: barra fissa, dashboard, pagina del personaggio.</li>
 *   <li>{@code IGamificationSettingsService}: impostazioni di gioco dell'Admin.</li>
 *   <li>{@code ILifeRecoveryService}: superata, non implementare.</li>
 * </ul>
 *
 * <h2>TODO per il branch dei corsi (questo branch non modifica quel codice)</h2>
 * <ol>
 *   <li>Senza skill indicate la missione non si pubblica, senza ripiego sulla skill principale della materia
 *       (Blueprint Gamification §8.2): {@code MissionsService.exerciseProblems}, {@code IMissionExercisesService}.</li>
 *   <li>Campo "Difficoltà" (1–6) nel form Admin della voce della Palestra; lucchetto della difficoltà nelle
 *       letture dello studente con {@code ISkillLevelsService.getLock} (§3).</li>
 *   <li>Classe di riferimento nel form Admin dell'argomento; nella Palestra dello studente le sezioni
 *       "Per la tua classe" e "Più avanti nel programma" con l'avviso (§3-bis).</li>
 *   <li>Il test di orientamento assegna il livello di partenza con
 *       {@code ISkillLevelsService.setStartingLevel} (§2.6); la regola dal risultato al livello è da fissare.</li>
 *   <li>Corso d'ingresso tutto gratuito, uno per materia: casella nel form Admin del corso e blocco delle
 *       missioni premium al suo interno (§8-bis).</li>
 *   <li>Ricompense nella pagina del corso e della missione con {@code IStudentGameStateService} (§6.4).</li>
 *   <li>Landing pubblica: testi dei personaggi allineati (§6.7).</li>
 * </ol>
 *
 * <h2>Momenti che il codice deve far scattare</h2>
 * Tabella in Blueprint Gamification §8b.5: evoluzione raggiunta, vita ottenuta, pausa, cappello
 * riscattabile, muro dell'abbonamento con "Chiedilo a un genitore". Ogni service che registra uno di questi
 * fatti restituisce che cosa è successo, così il frontend mostra il messaggio; non vanno aggiunti dopo.
 *
 * <h2>TODO fuori dalla gamification</h2>
 * <ol>
 *   <li>Percorso dell'adulto da riprogettare prima dei pagamenti: richiesta dal ragazzo con il riepilogo
 *       dei risultati, pagamento, consensi (§8b.6; Piano MVP, P.2 e P.3).</li>
 *   <li>Passo "La tua scuola" dopo la registrazione e import delle scuole d'Italia (§6.6): lavoro
 *       sull'account, voce P.3 del Piano MVP.</li>
 * </ol>
 *
 * <h2>TODO dopo la beta (roadmap)</h2>
 * <ol>
 *   <li>Evoluzioni proprie di ogni personaggio, con XP o percorsi separati. Prima di farlo, decidere come
 *       passare dal contatore unico a quelli separati: per esempio, gli XP già guadagnati vanno al
 *       personaggio in uso.</li>
 *   <li>Sblocco degli altri personaggi; personalizzazione dell'aspetto sullo stile di The Sims o Fortnite.</li>
 *   <li>Catalogo di suggerimenti del cappello a partire dagli errori degli studenti; aiuti premium.</li>
 *   <li>Difficoltà che cresce da sola oltre il livello massimo (richiede l'AI).</li>
 *   <li>Badge, sfondi, titoli e oggetti, collegati al mondo virtuale con scuole in gara, sponsor e premi.</li>
 *   <li>Classifica.</li>
 *   <li>Uso dell'AI tra i vantaggi dei piani in abbonamento; novità periodiche per chi non paga, riepilogo
 *       periodico al genitore, prova gratuita attivata dall'adulto (§8b.3).</li>
 * </ol>
 */
package giuliacrepaldi.BStudent_Management.services.gamification;
