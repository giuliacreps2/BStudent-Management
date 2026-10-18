package giuliacrepaldi.BStudent_Management.repositories.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.StudentSkinEvolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentSkinEvolutionsRepository extends JpaRepository<StudentSkinEvolution, StudentSkinEvolution.StudentSkinEvolutionId> {
}
