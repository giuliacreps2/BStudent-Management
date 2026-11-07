package giuliacrepaldi.BStudent_Management.payloads.catalog;

import java.util.List;

// problems bloccano la pubblicazione, warnings no (es. la missione finisce con una lezione)
public record MissionReadinessDTO(boolean ready, List<String> problems, List<String> warnings) {

    public MissionReadinessDTO(List<String> problems, List<String> warnings) {
        this(problems.isEmpty(), List.copyOf(problems), List.copyOf(warnings));
    }
}
