package giuliacrepaldi.BStudent_Management.services.gamification;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import giuliacrepaldi.BStudent_Management.entities.gamification.GamificationSetting;
import giuliacrepaldi.BStudent_Management.entities.gamification.LifeAward;
import giuliacrepaldi.BStudent_Management.entities.gamification.MissionLifeLoss;
import giuliacrepaldi.BStudent_Management.entities.gamification.StudentLifeState;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.enums.gamification.LifeAwardReason;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.LifeAwardsRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.MissionLifeLossesRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.StudentLifeStatesRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IGamificationSettingsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ILivesService;
import giuliacrepaldi.BStudent_Management.utilities.GameRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
public class LivesService implements ILivesService {

    private final StudentLifeStatesRepository lifeStatesRepository;
    private final LifeAwardsRepository lifeAwardsRepository;
    private final MissionLifeLossesRepository missionLifeLossesRepository;
    private final MissionAttemptsRepository missionAttemptsRepository;
    private final StudentsRepository studentsRepository;
    private final IGamificationSettingsService settingsService;

    public LivesService(StudentLifeStatesRepository lifeStatesRepository, LifeAwardsRepository lifeAwardsRepository,
                        MissionLifeLossesRepository missionLifeLossesRepository,
                        MissionAttemptsRepository missionAttemptsRepository, StudentsRepository studentsRepository,
                        IGamificationSettingsService settingsService) {
        this.lifeStatesRepository = lifeStatesRepository;
        this.lifeAwardsRepository = lifeAwardsRepository;
        this.missionLifeLossesRepository = missionLifeLossesRepository;
        this.missionAttemptsRepository = missionAttemptsRepository;
        this.studentsRepository = studentsRepository;
        this.settingsService = settingsService;
    }

    // Non readOnly: la lettura applica la ricarica scaduta e crea lo stato la prima volta
    @Override
    @Transactional
    public LivesDTO getLives(long studentId) {
        return LivesDTO.from(currentState(studentId));
    }

    @Override
    @Transactional
    public boolean canStartMission(long studentId) {
        return currentState(studentId).getLives() > 0;
    }

    @Override
    @Transactional
    public LivesDTO awardLife(long studentId, LifeAwardReason reason, LocalDate awardDay, String eventId) {
        StudentLifeState state = currentState(studentId);
        // Stesso evento, o seconda sfida nello stesso giorno: nessuna vita in più.
        // I vincoli unici del DB restano l'ultima difesa in caso di richieste contemporanee
        if (lifeAwardsRepository.existsByEventId(eventId)
                || (reason == LifeAwardReason.daily_challenge
                    && lifeAwardsRepository.existsByStudentIdAndReasonAndAwardDay(studentId, reason, awardDay))) {
            return LivesDTO.from(state);
        }
        lifeAwardsRepository.save(LifeAward.builder()
                .studentId(studentId)
                .reason(reason)
                .awardDay(awardDay)
                .eventId(eventId)
                .awardedAt(OffsetDateTime.now())
                .build());
        // Può superare la base e non riavvia la ricarica (§7.1)
        state.setLives(state.getLives() + 1);
        return LivesDTO.from(state);
    }

    @Override
    @Transactional
    public LivesDTO raiseBaseLives(long studentId, int newBase) {
        StudentLifeState state = currentState(studentId);
        if (state.getBaseLives() < newBase) {
            state.setBaseLives(newBase);
            // Saldo portato almeno alla nuova base; la ricarica in corso resta com'è (§10.4)
            state.setLives(GameRules.livesAfterBaseRaise(state.getLives(), newBase));
        }
        return LivesDTO.from(state);
    }

    @Override
    @Transactional
    public LivesDTO onMissionAttemptFailed(long studentId, long missionAttemptId) {
        StudentLifeState state = currentState(studentId);
        // Una vita per tentativo fallito: la chiave di mission_life_losses è il tentativo
        if (missionLifeLossesRepository.existsById(missionAttemptId)) {
            return LivesDTO.from(state);
        }
        MissionAttempt attempt = missionAttemptsRepository.findById(missionAttemptId)
                .orElseThrow(() -> new NotFoundException("Tentativo di missione " + missionAttemptId + " non trovato"));
        if (!attempt.getStudent().getStudentId().equals(studentId)) {
            throw new BadRequestException("Il tentativo non è di questo studente");
        }
        missionLifeLossesRepository.save(MissionLifeLoss.builder()
                .missionAttempt(attempt)
                .studentLifeState(state)
                .build());
        state.setLives(Math.max(0, state.getLives() - 1));
        // Dieci ore dalla PRIMA vita persa: le perdite successive non riavviano il timer (§12.1)
        if (state.getRechargeAt() == null) {
            state.setRechargeAt(OffsetDateTime.now().plusHours(settingsService.get().getLifeRechargeHours()));
        }
        return LivesDTO.from(state);
    }

    // Stato aggiornato: creato alla prima lettura, con la ricarica scaduta già applicata
    private StudentLifeState currentState(long studentId) {
        StudentLifeState state = lifeStatesRepository.findById(studentId).orElseGet(() -> create(studentId));
        if (state.getRechargeAt() != null && !OffsetDateTime.now().isBefore(state.getRechargeAt())) {
            state.setLives(GameRules.livesAfterRecharge(state.getLives(), state.getBaseLives()));
            state.setRechargeAt(null);
            state.setLivesRecoveredInCycle(0);
        }
        return state;
    }

    private StudentLifeState create(long studentId) {
        Student student = studentsRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Studente " + studentId + " non trovato"));
        GamificationSetting settings = settingsService.get();
        return lifeStatesRepository.save(StudentLifeState.builder()
                .student(student)
                .lives(settings.getBaseLives())
                .baseLives(settings.getBaseLives())
                .build());
    }
}
