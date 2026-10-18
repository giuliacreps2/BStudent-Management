package giuliacrepaldi.BStudent_Management.entities.gamification;

import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "equipped_accessories", indexes = {
        @Index(name = "idx_equipped_accessories_accessory", columnList = "accessory_id, slot_code")
})
// FK composte verificate dal DB:
// (student_id, accessory_id) -> student_accessories, (accessory_id, slot_code) -> accessories
public class EquippedAccessory {

    // Chiave composta: (student_id, slot_code), un accessorio per slot
    @EmbeddedId
    @Builder.Default
    private EquippedAccessoryId id = new EquippedAccessoryId();

    @ToString.Exclude
    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accessory_id", nullable = false)
    private Accessory accessory;

    @PrePersist
    @PreUpdate
    private void validateSlot() {
        if (accessory == null || id.getSlotCode() == null) {
            return;
        }
        if (!Objects.equals(accessory.getAccessorySlotCode(), id.getSlotCode())) {
            throw new IllegalStateException("The accessory must be equipped in its own slot");
        }
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class EquippedAccessoryId implements Serializable {
        private Long studentId;

        @Column(name = "slot_code", columnDefinition = "TEXT")
        private String slotCode;
    }
}
