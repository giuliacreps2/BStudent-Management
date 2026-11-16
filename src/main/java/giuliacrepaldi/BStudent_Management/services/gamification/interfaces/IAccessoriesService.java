package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

/**
 * Accessori: riscatto ed equipaggiamento (blueprint principale §11.1, §11.2; Blueprint Gamification §6.1).
 * Nella beta esiste solo il cappello da turista (50 punti di Grammatica). Gli accessori sono dello studente,
 * non del personaggio. La soglia vale se raggiunta nella skill in una materia qualsiasi (§11.4).
 * Riscatto manuale ("Riscatta"), senza consumo di punti; possesso permanente; non indossato in automatico.
 * Un accessorio per spazio; cambio consentito fuori dal tentativo di missione.
 *
 * <h2>TODO — metodi da implementare (Fase D)</h2>
 * <pre>
 * List&lt;StudentAccessoryDTO&gt; getAccessories(long studentId);
 *     // stato di ogni accessorio disponibile: bloccato (con soglia e punti attuali nella materia migliore),
 *     // riscattabile, posseduto, indossato; descrizione dell'aiuto ("Ti ricorda l'argomento · 5 aiuti ·
 *     // si ricarica in 12 ore")
 * // Calcolo puro, coperto dai test:
 * static AccessoryStatus statusOf(BigDecimal bestPointsInSkill, BigDecimal requiredPoints, boolean owned, boolean equipped);
 * StudentAccessoryDTO redeem(long studentId, long accessoryId);
 *     // soglia raggiunta, altrimenti BadRequestException con i punti mancanti; già posseduto: nessun
 *     // effetto (stesso risultato); cariche complete al riscatto
 * StudentAccessoryDTO equip(long studentId, long accessoryId);
 *     // posseduto; sostituisce quello nello stesso spazio; non durante un tentativo di missione aperto
 *     // (ConflictException). Non ripristina cariche e non azzera la ricarica
 * void unequip(long studentId, long accessoryId);
 *
 * // Admin
 * Accessory save(AccessoryDTO body);
 * Accessory update(long accessoryId, AccessoryDTO body);
 *     // nome, immagine (Cloudinary), spazio, skill e punti richiesti, aiuto (numero di utilizzi, ore di
 *     // ricarica), disponibile sì/no. Rendere non disponibile un accessorio non lo toglie a chi lo possiede
 * List&lt;Accessory&gt; findAll();
 * </pre>
 * DTO da creare: StudentAccessoryDTO, AccessoryDTO. Enum: AccessoryStatus (locked, redeemable, owned, equipped).
 * Controller: /student/accessories, /admin/academy/accessories.
 */
public interface IAccessoriesService {
}
