/**
 * Pagamenti, abbonamenti e accesso premium (migrazione v0.5).
 *
 * <p>Modello già pronto: entity in {@code entities.billing}, repository in {@code repositories.billing},
 * {@link giuliacrepaldi.BStudent_Management.services.billing.interfaces.IEntitlementService}.
 * Regole fisse: paga sempre un adulto (artt. 2 e 1425 c.c.), nessun dato di carta salvato,
 * pagamenti/rimborsi/recessi conservati 10 anni, oggi regime forfettario (IVA 0).
 *
 * <h2>TODO</h2>
 * <ol>
 *   <li><b>Integrazione Stripe.</b> Dipendenza {@code stripe-java}; chiavi {@code STRIPE_SECRET_KEY}
 *       e {@code STRIPE_WEBHOOK_SECRET} in env.properties (mai nel codice). Creazione della sessione
 *       di Stripe Checkout con verifica che il pagante sia adulto, accettazione delle condizioni di
 *       vendita (policy_acceptances, {@code terms_of_sale}), consenso all'esecuzione immediata
 *       (art. 50 c. 3 / 51 c. 8) e pulsante "Abbonati con obbligo di pagamento" (art. 51 c. 2).
 *       Endpoint dei webhook con verifica della firma e idempotenza su billing_events
 *       (checkout.session.completed, invoice.paid, invoice.payment_failed,
 *       customer.subscription.updated/deleted, charge.refunded). Customer Portal per la disdetta,
 *       facile quanto l'iscrizione. Email di conferma del contratto su supporto durevole (art. 51 c. 7).</li>
 *   <li><b>Pulsante di recesso</b> (art. 54-bis, contratti dal 19/06/2026). Funzione
 *       "Recedi dal contratto qui" visibile fino a withdrawalDeadline, seconda conferma,
 *       WithdrawalRequest + ricevuta via email; importo trattenuto proporzionale ai giorni fruiti
 *       se c'è immediateStartConsentAt (art. 57 c. 3), rimborso entro 14 giorni (art. 56),
 *       fine dell'accesso premium.</li>
 *   <li><b>"Chiedi al genitore"</b> con PurchaseRequest, sullo schema di RegistrationRequest:
 *       token monouso (solo hash), scadenza, una richiesta aperta per studente, pulsante solo
 *       informativo e senza pressioni (art. 26 lett. e Codice del consumo). Il genitore apre il
 *       link, accede o crea l'account, sceglie il piano e paga; per i 14-17 anni crea anche il
 *       GuardianLink se manca.</li>
 *   <li><b>Collegare IEntitlementService</b> agli endpoint di corsi, missioni ed esercizi.
 *       Prima: decidere quali corsi/missioni sono {@code free} (default {@code premium}) e
 *       creare gli AccessGrant {@code beta}, altrimenti nessuno vede i contenuti premium.</li>
 *   <li><b>Piani e sconto beta nel DB</b> quando i prezzi sono decisi: individuale e famiglia
 *       (max_students 2), mensile e annuale, con il Price di Stripe; Discount audience {@code beta}
 *       con il Coupon di Stripe. Avvisi di fine beta (AccessGrantsRepository.findEndingBetween)
 *       e promemoria prima del rinnovo annuale (Subscription.renewalReminderSentAt).</li>
 * </ol>
 */
package giuliacrepaldi.BStudent_Management.services.billing;
