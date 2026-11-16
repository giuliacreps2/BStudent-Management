package giuliacrepaldi.BStudent_Management.payloads.gamification;

import giuliacrepaldi.BStudent_Management.entities.gamification.StudentLifeState;

import java.time.OffsetDateTime;

// Vite dello studente. rechargeAt = quando il saldo torna alla base; null se non c'è una ricarica in corso
public record LivesDTO(int lives, int baseLives, OffsetDateTime rechargeAt) {

    public static LivesDTO from(StudentLifeState state) {
        return new LivesDTO(state.getLives(), state.getBaseLives(), state.getRechargeAt());
    }
}
