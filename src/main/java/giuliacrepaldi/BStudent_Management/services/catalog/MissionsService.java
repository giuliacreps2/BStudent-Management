package giuliacrepaldi.BStudent_Management.services.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.payloads.catalog.MissionDTO;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.IMissionsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MissionsService implements IMissionsService {

    @Override
    public Mission saveNewMission(long courseId, MissionDTO body) {
        return null;
    }

    @Override
    public Mission findById(long missionId) {
        return null;
    }

    @Override
    public List<Mission> findByCourse(long courseId) {
        return List.of();
    }

    @Override
    public Mission findByIdAndUpdate(long missionId, MissionDTO body) {
        return null;
    }

    @Override
    public List<Mission> reorderMissions(long courseId, List<Long> missionIds) {
        return List.of();
    }

    @Override
    public Mission publishMission(long missionId) {
        return null;
    }

    @Override
    public Mission withdrawMission(long missionId) {
        return null;
    }

    @Override
    public void deleteMission(long missionId) {

    }
}
