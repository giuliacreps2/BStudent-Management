package giuliacrepaldi.BStudent_Management.repositories.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.StudentSkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentSkinsRepository extends JpaRepository<StudentSkin, Long> {

    Optional<StudentSkin> findByStudent_StudentIdAndSkin_SkinId(Long studentId, Long skinId);

    boolean existsBySkin_SkinId(Long skinId);
}
