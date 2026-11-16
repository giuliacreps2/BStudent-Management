package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.gamification.AccessoryDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.AccessoryResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.StudentAccessoryDTO;

import java.util.List;

/**
 * Accessori: riscatto ed equipaggiamento (blueprint principale §11.1, §11.2; Blueprint Gamification §6.1).
 * Nella beta esiste solo il cappello da turista (50 punti di Grammatica). Gli accessori sono dello studente,
 * non del personaggio. La soglia vale se raggiunta nella skill in una materia qualsiasi (§11.4), con i punti
 * veri: il livello di partenza non conta.
 * Riscatto manuale ("Riscatta"), senza consumo di punti; possesso permanente; non indossato in automatico.
 * Un accessorio per spazio; cambio consentito fuori dal tentativo di missione.
 * L'immagine dell'accessorio sta nel frontend, per code (TODO: colonna immagine quando saranno molti).
 */
public interface IAccessoriesService {

    // Accessori disponibili, più quelli già posseduti: bloccato / riscattabile / posseduto / indossato
    List<StudentAccessoryDTO> getAccessories(long studentId);

    // Soglia raggiunta, altrimenti errore con i punti mancanti; già posseduto: nessun effetto.
    // Cariche complete al riscatto
    StudentAccessoryDTO redeem(long studentId, long accessoryId);

    // Posseduto; sostituisce quello nello stesso spazio; non durante un tentativo di missione in corso.
    // Non ripristina cariche e non azzera la ricarica
    StudentAccessoryDTO equip(long studentId, long accessoryId);

    StudentAccessoryDTO unequip(long studentId, long accessoryId);

    // Admin
    List<AccessoryResponseDTO> findAll();

    AccessoryResponseDTO findById(long accessoryId);

    AccessoryResponseDTO save(AccessoryDTO body);

    // Rendere non disponibile un accessorio non lo toglie a chi lo possiede
    AccessoryResponseDTO update(long accessoryId, AccessoryDTO body);

    // Solo se nessuno studente lo possiede
    void delete(long accessoryId);
}
