package giuliacrepaldi.BStudent_Management.repositories.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.StudentStartingLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentStartingLevelsRepository
        extends JpaRepository<StudentStartingLevel, StudentStartingLevel.StudentStartingLevelId> {

    List<StudentStartingLevel> findById_StudentId(Long studentId);
}
