package giuliacrepaldi.BStudent_Management.repositories.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.Skin;
import giuliacrepaldi.BStudent_Management.enums.skills.StatusSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkinsRepository extends JpaRepository<Skin, Long> {

    boolean existsBySkinCode(String skinCode);

    List<Skin> findAllByOrderBySkinId();

    // Prima i personaggi iniziali, poi gli altri ("Prossimamente")
    List<Skin> findByStatusSkinOrderByStarterDescSkinIdAsc(StatusSkill statusSkin);
}
