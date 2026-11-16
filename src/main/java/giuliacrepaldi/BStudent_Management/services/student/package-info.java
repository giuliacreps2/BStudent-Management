/**
 * Letture dello studente (Blocco 4, Fase 3 di blueprint/BStudent_TODO_Blocco4.md).
 *
 * <p>I service di questo package restituiscono esattamente i dati dei tipi del frontend
 * ({@code bstudent-academy/types/*.ts}, con i TODO concordati scritti in quei file). Non modificano
 * contenuti: le scritture dello studente stanno nei loro domini (Quaderno, test, registro di studio).
 *
 * <h2>TODO comuni</h2>
 * <ol>
 *   <li><b>Una sola regola di visibilità</b> ({@code StudentVisibility}, da creare): un contenuto si vede se è
 *       pubblicato e lo sono anche tutti i suoi contenitori, materia compresa (subjects.status = published).
 *       Esercizio di missione: mission_exercises.status = published ed esercizio del banco non ritirato
 *       (stessa logica di {@code MissionsService.visibleSteps}). Voce della Palestra: pubblicata e
 *       esercizio del banco non ritirato. Nessun service la riscrive per conto suo.</li>
 *   <li><b>Lucchetto premium</b> distinto dalla visibilità: il contenuto si vede, ma si apre solo se
 *       {@code IEntitlementService.canAccess} lo permette (blueprint §5.6).</li>
 *   <li><b>Anteprima Admin</b>: con {@code ?preview=true} e ruolo ADMIN si vedono anche le bozze
 *       (TODO in {@code ICoursesService}).</li>
 *   <li><b>Sicurezza</b>: endpoint sotto /student/** protetti in {@code SecurityConfig} (ruolo studente);
 *       l'id dello studente arriva sempre dal token, mai dal client.</li>
 *   <li><b>Indirizzi</b>: le pagine usano gli slug (materia, corso, missione, risorsa), mai l'ordine.</li>
 *   <li><b>Dati dei blocchi futuri</b> (progressi, XP, livelli, stato delle missioni superate): fino al Blocco 5
 *       e alla gamification valori calcolati sul "nessun progresso" (prima missione attuale, nessun XP),
 *       mai valori inventati.</li>
 * </ol>
 */
package giuliacrepaldi.BStudent_Management.services.student;
