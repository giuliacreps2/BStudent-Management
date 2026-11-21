package giuliacrepaldi.BStudent_Management.payloads.notebook;

import giuliacrepaldi.BStudent_Management.payloads.student.ResourceCardDTO;

import java.util.List;

// Quaderno (blueprint §14): colore della copertina, una linguetta per materia con le sue lezioni (le più recenti
// prima) e i segnalibri delle Risorse, a parte dagli appunti (§7.4). La linguetta "Articoli" arriva con il Blog
public record NotebookOverviewDTO(String coverColor, List<Tab> subjects, List<ResourceCardDTO> bookmarks) {

    public record Tab(String subjectSlug, String subjectName, List<NotebookItemDTO> items) {
    }
}
