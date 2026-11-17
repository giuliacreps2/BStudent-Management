package giuliacrepaldi.BStudent_Management.payloads.student;

import java.util.List;

// Pagina principale delle Risorse di una materia (blueprint §7.4): "più usate" e "in evidenza" scelte
// dall'autrice, e tutte le Risorse visibili (il frontend le divide per categoria).
// hasOrientationTest = mostrare "Percorso consigliato" verso il test di orientamento
public record ResourcesHubDTO(List<SubjectCardDTO> subjects, SubjectCardDTO activeSubject, List<ResourceCardDTO> mostUsed,
                              List<ResourceCardDTO> featured, List<ResourceCardDTO> resources) {
}
