package giuliacrepaldi.BStudent_Management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Abilita i job @Scheduled (es. pulizia delle richieste di registrazione scadute)
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
