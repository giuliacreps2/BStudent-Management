package giuliacrepaldi.BStudent_Management.utilities;

import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class Pageables {

    private static final int MAX_SIZE = 100;

    private Pageables() {
    }

    // sortBy ammesso solo tra i campi indicati: un campo inesistente farebbe fallire la query con un 500
    public static Pageable of(int page, int size, String sortBy, Set<String> allowedSorts, String defaultSort) {
        if (page < 0) {
            throw new BadRequestException("page deve essere >= 0");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new BadRequestException("size deve essere tra 1 e " + MAX_SIZE);
        }
        String sort = (sortBy == null || sortBy.isBlank()) ? defaultSort : sortBy;
        if (!allowedSorts.contains(sort)) {
            throw new BadRequestException("sortBy non valido: ammessi " + allowedSorts);
        }
        return PageRequest.of(page, size, Sort.by(sort));
    }
}
