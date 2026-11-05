package giuliacrepaldi.BStudent_Management.utilities;

import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import jakarta.persistence.EntityManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Riordino atomico dei figli di un contenitore (missioni del corso, lezioni della missione, pagine della lezione)
public final class Reorders {

    private Reorders() {
    }

    // Tutti e soli gli ID del contenitore, senza ripetizioni (blueprint §5.1)
    public static void validateIds(List<Long> requestedIds, Set<Long> containerIds, String childrenLabel) {
        Set<Long> requested = new HashSet<>(requestedIds);
        if (requested.size() != requestedIds.size()) {
            throw new BadRequestException("L'ordine contiene ID ripetuti");
        }
        if (!requested.equals(containerIds)) {
            throw new BadRequestException("L'ordine deve contenere tutti e soli gli ID di " + childrenLabel);
        }
    }

    // Il vincolo (contenitore, sort_order) è DEFERRABLE: rimandato al COMMIT, gli scambi di posizione
    // non lo violano a metà. Vale solo per la transazione corrente, che deve essere già aperta.
    // constraintName è sempre una costante del service, mai un input del client
    public static void deferUniqueOrder(EntityManager entityManager, String constraintName) {
        entityManager.createNativeQuery("SET CONSTRAINTS " + constraintName + " DEFERRED").executeUpdate();
    }
}
