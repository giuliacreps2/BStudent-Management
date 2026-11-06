package giuliacrepaldi.BStudent_Management.payloads.exercises;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// Body di PUT .../skills: sostituisce tutti i pesi. Lista vuota = 100% sulla skill predefinita della materia
public record SkillWeightsDTO(@NotNull @Size(max = 10) List<@Valid @NotNull SkillWeightDTO> weights) {
}
