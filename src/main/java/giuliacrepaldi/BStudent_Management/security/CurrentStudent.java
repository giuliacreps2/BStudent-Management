package giuliacrepaldi.BStudent_Management.security;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.exceptions.ForbiddenException;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import org.springframework.stereotype.Component;

// L'id dello studente arriva sempre dall'utente autenticato, mai dal client (services.student, TODO comuni)
@Component
public class CurrentStudent {

    private final StudentsRepository studentsRepository;

    public CurrentStudent(StudentsRepository studentsRepository) {
        this.studentsRepository = studentsRepository;
    }

    public long idOf(User currentUser) {
        return studentsRepository.findByUserUserId(currentUser.getUserId())
                .orElseThrow(() -> new ForbiddenException("Questa pagina è riservata agli studenti"))
                .getStudentId();
    }
}
