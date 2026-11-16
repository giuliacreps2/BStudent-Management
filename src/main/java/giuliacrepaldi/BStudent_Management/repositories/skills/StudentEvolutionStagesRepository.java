package giuliacrepaldi.BStudent_Management.repositories.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.StudentEvolutionStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentEvolutionStagesRepository
        extends JpaRepository<StudentEvolutionStage, StudentEvolutionStage.StudentEvolutionStageId> {

    // 0 = stadio Base: lo studente non ha ancora raggiunto evoluzioni
    @Query("SELECT COALESCE(MAX(s.id.stageNumber), 0) FROM StudentEvolutionStage s WHERE s.id.studentId = :studentId")
    int findCurrentStage(@Param("studentId") Long studentId);
}
