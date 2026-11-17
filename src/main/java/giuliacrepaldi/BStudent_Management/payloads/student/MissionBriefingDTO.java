package giuliacrepaldi.BStudent_Management.payloads.student;

import java.util.List;

// Briefing prima di "Inizia" (blueprint §5.9): la guida fissa della materia presenta la missione con 1-5
// battute e un video facoltativo. Si può saltare. lines vuoto e video assente = nessun briefing
public record MissionBriefingDTO(String guideName, String guideImageReference, List<String> lines,
                                 String videoProvider, String videoIdentifier) {
}
