package giuliacrepaldi.BStudent_Management.services.gamification;

import giuliacrepaldi.BStudent_Management.entities.gamification.Accessory;
import giuliacrepaldi.BStudent_Management.entities.gamification.AccessoryBenefit;
import giuliacrepaldi.BStudent_Management.entities.gamification.AccessoryChargeState;
import giuliacrepaldi.BStudent_Management.entities.gamification.EquippedAccessory;
import giuliacrepaldi.BStudent_Management.entities.gamification.StudentAccessory;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.enums.attempts.MissionAttemptStatus;
import giuliacrepaldi.BStudent_Management.enums.gamification.AccessoryStatus;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.AccessoryDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.AccessoryResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.HintChargesDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.StudentAccessoryDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.AccessoriesRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.AccessoryBenefitsRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.AccessoryChargeStatesRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.EquippedAccessoriesRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.StudentAccessoriesRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillsRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IAccessoriesService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import giuliacrepaldi.BStudent_Management.utilities.GameRules;
import giuliacrepaldi.BStudent_Management.utilities.GameRules.ChargeState;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccessoriesService implements IAccessoriesService {

    // Unico tipo di aiuto nella beta: l'argomento dell'esercizio (Blueprint Gamification §6.2)
    public static final String TOPIC_HINT = "topic_hint";

    // Tentativo di missione in corso: l'equipaggiamento non si cambia (§11.2)
    public static final List<MissionAttemptStatus> OPEN_ATTEMPT =
            List.of(MissionAttemptStatus.active, MissionAttemptStatus.saved, MissionAttemptStatus.pending_review);

    private final AccessoriesRepository accessoriesRepository;
    private final AccessoryBenefitsRepository benefitsRepository;
    private final StudentAccessoriesRepository studentAccessoriesRepository;
    private final EquippedAccessoriesRepository equippedRepository;
    private final AccessoryChargeStatesRepository chargeStatesRepository;
    private final MissionAttemptsRepository missionAttemptsRepository;
    private final SkillsRepository skillsRepository;
    private final StudentsRepository studentsRepository;
    private final ISkillLevelsService skillLevelsService;
    private final EntityManager entityManager;

    public AccessoriesService(AccessoriesRepository accessoriesRepository, AccessoryBenefitsRepository benefitsRepository,
                              StudentAccessoriesRepository studentAccessoriesRepository,
                              EquippedAccessoriesRepository equippedRepository,
                              AccessoryChargeStatesRepository chargeStatesRepository,
                              MissionAttemptsRepository missionAttemptsRepository, SkillsRepository skillsRepository,
                              StudentsRepository studentsRepository, ISkillLevelsService skillLevelsService,
                              EntityManager entityManager) {
        this.accessoriesRepository = accessoriesRepository;
        this.benefitsRepository = benefitsRepository;
        this.studentAccessoriesRepository = studentAccessoriesRepository;
        this.equippedRepository = equippedRepository;
        this.chargeStatesRepository = chargeStatesRepository;
        this.missionAttemptsRepository = missionAttemptsRepository;
        this.skillsRepository = skillsRepository;
        this.studentsRepository = studentsRepository;
        this.skillLevelsService = skillLevelsService;
        this.entityManager = entityManager;
    }

    // ------------------------------------------------------------------ Studente

    // Non readOnly: la lettura applica la ricarica scaduta delle cariche
    @Override
    @Transactional
    public List<StudentAccessoryDTO> getAccessories(long studentId) {
        Set<Long> owned = ownedIds(studentId);
        Set<Long> equipped = equippedIds(studentId);
        List<StudentAccessoryDTO> result = new ArrayList<>();
        for (Accessory accessory : accessoriesRepository.findAllByOrderByAccessoryId()) {
            // Un accessorio non più disponibile resta a chi lo possiede
            if (accessory.isAvailable() || owned.contains(accessory.getAccessoryId())) {
                result.add(toStudent(studentId, accessory, owned.contains(accessory.getAccessoryId()),
                        equipped.contains(accessory.getAccessoryId())));
            }
        }
        return result;
    }

    @Override
    @Transactional
    public StudentAccessoryDTO redeem(long studentId, long accessoryId) {
        Accessory accessory = findAccessory(accessoryId);
        StudentAccessory.StudentAccessoryId id = new StudentAccessory.StudentAccessoryId(studentId, accessoryId);
        // Già posseduto: stesso risultato, senza errori (doppio clic su "Riscatta")
        if (!studentAccessoriesRepository.existsById(id)) {
            if (!accessory.isAvailable()) {
                throw new NotFoundException("Accessorio " + accessoryId + " non trovato");
            }
            Skill skill = accessory.getSkill();
            BigDecimal points = skillLevelsService.getBestPoints(studentId, skill.getSkillId());
            if (points.compareTo(accessory.getRequiredPoints()) < 0) {
                throw new BadRequestException("Ti mancano " + plain(accessory.getRequiredPoints().subtract(points))
                        + " punti di " + skill.getSkillName() + " per riscattare " + accessory.getAccessoryName());
            }
            Student student = studentsRepository.getReferenceById(studentId);
            // Nessun consumo di punti; possesso permanente; non indossato in automatico (§11.1)
            // persist e non save: con la chiave composta già valorizzata save farebbe un merge
            entityManager.persist(StudentAccessory.builder().id(id).student(student).accessory(accessory).build());
            entityManager.flush();
            AccessoryBenefit benefit = benefitsRepository.findById(accessoryId).orElse(null);
            if (benefit != null) {
                // Cariche complete al primo riscatto (§11.3)
                entityManager.persist(AccessoryChargeState.builder()
                        .id(new AccessoryChargeState.AccessoryChargeStateId(studentId, accessoryId))
                        .student(student).accessoryBenefit(benefit)
                        .remainingCharges(benefit.getCapacity()).build());
                entityManager.flush();
            }
        }
        return toStudent(studentId, accessory, true, equippedIds(studentId).contains(accessoryId));
    }

    @Override
    @Transactional
    public StudentAccessoryDTO equip(long studentId, long accessoryId) {
        Accessory accessory = findAccessory(accessoryId);
        if (!studentAccessoriesRepository.existsById(new StudentAccessory.StudentAccessoryId(studentId, accessoryId))) {
            throw new BadRequestException("Riscatta " + accessory.getAccessoryName() + " prima di indossarlo");
        }
        assertNoOpenMission(studentId);
        EquippedAccessory.EquippedAccessoryId slot =
                new EquippedAccessory.EquippedAccessoryId(studentId, accessory.getAccessorySlotCode());
        EquippedAccessory current = equippedRepository.findById(slot).orElse(null);
        if (current == null) {
            entityManager.persist(EquippedAccessory.builder()
                    .id(slot).student(studentsRepository.getReferenceById(studentId)).accessory(accessory).build());
        } else {
            // Un solo accessorio per spazio: il nuovo sostituisce quello indossato (§11.2)
            current.setAccessory(accessory);
        }
        entityManager.flush();
        return toStudent(studentId, accessory, true, true);
    }

    @Override
    @Transactional
    public StudentAccessoryDTO unequip(long studentId, long accessoryId) {
        Accessory accessory = findAccessory(accessoryId);
        EquippedAccessory current = equippedRepository.findById(
                new EquippedAccessory.EquippedAccessoryId(studentId, accessory.getAccessorySlotCode())).orElse(null);
        if (current != null && current.getAccessory().getAccessoryId().equals(accessoryId)) {
            assertNoOpenMission(studentId);
            // Togliere e rimettere non ripristina cariche e non azzera la ricarica (§11.2)
            equippedRepository.delete(current);
            entityManager.flush();
        }
        return toStudent(studentId, accessory, ownedIds(studentId).contains(accessoryId), false);
    }

    private void assertNoOpenMission(long studentId) {
        if (missionAttemptsRepository.existsByStudent_StudentIdAndStatusIn(studentId, OPEN_ATTEMPT)) {
            throw new ConflictException("Hai una missione in corso: finiscila prima di cambiare gli accessori");
        }
    }

    private Set<Long> ownedIds(long studentId) {
        return studentAccessoriesRepository.findById_StudentId(studentId).stream()
                .map(owned -> owned.getId().getAccessoryId()).collect(Collectors.toSet());
    }

    private Set<Long> equippedIds(long studentId) {
        return equippedRepository.findById_StudentId(studentId).stream()
                .map(equipped -> equipped.getAccessory().getAccessoryId()).collect(Collectors.toSet());
    }

    private StudentAccessoryDTO toStudent(long studentId, Accessory accessory, boolean owned, boolean equipped) {
        Skill skill = accessory.getSkill();
        BigDecimal points = skillLevelsService.getBestPoints(studentId, skill.getSkillId());
        AccessoryStatus status = GameRules.accessoryStatus(points, accessory.getRequiredPoints(), owned, equipped);
        AccessoryBenefit benefit = benefitsRepository.findById(accessory.getAccessoryId()).orElse(null);
        return new StudentAccessoryDTO(accessory.getAccessoryId(), accessory.getAccessoryCode(),
                accessory.getAccessoryName(), accessory.getAccessorySlotCode(), status, skill.getSkillName(),
                accessory.getRequiredPoints(), points,
                status == AccessoryStatus.locked
                        ? "Si sblocca con " + plain(accessory.getRequiredPoints()) + " punti di " + skill.getSkillName() : null,
                benefit == null ? null : describe(benefit),
                benefit == null || !owned ? null : charges(studentId, accessory, benefit, equipped));
    }

    // Cariche aggiornate di un accessorio posseduto, con la ricarica scaduta già applicata
    HintChargesDTO charges(long studentId, Accessory accessory, AccessoryBenefit benefit, boolean equipped) {
        AccessoryChargeState state = chargeStatesRepository.findById(
                new AccessoryChargeState.AccessoryChargeStateId(studentId, accessory.getAccessoryId())).orElse(null);
        if (state == null) {
            return new HintChargesDTO(true, equipped, accessory.getAccessoryName(), benefit.getCapacity(),
                    benefit.getCapacity(), null);
        }
        ChargeState updated = GameRules.chargesAfterRecharge(
                new ChargeState(state.getRemainingCharges(), state.getRechargeAt()), benefit.getCapacity(), OffsetDateTime.now());
        state.setRemainingCharges(updated.remaining());
        state.setRechargeAt(updated.rechargeAt());
        return new HintChargesDTO(true, equipped, accessory.getAccessoryName(), updated.remaining(),
                benefit.getCapacity(), updated.rechargeAt());
    }

    private static String describe(AccessoryBenefit benefit) {
        return "Ti ricorda l'argomento · " + benefit.getCapacity() + " aiuti · si ricarica in "
                + benefit.getRechargeHours() + " ore";
    }

    private static String plain(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    // ------------------------------------------------------------------ Admin

    @Override
    @Transactional(readOnly = true)
    public List<AccessoryResponseDTO> findAll() {
        return accessoriesRepository.findAllByOrderByAccessoryId().stream().map(this::toAdmin).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccessoryResponseDTO findById(long accessoryId) {
        return toAdmin(findAccessory(accessoryId));
    }

    @Override
    @Transactional
    public AccessoryResponseDTO save(AccessoryDTO body) {
        if (accessoriesRepository.existsByAccessoryCode(body.code())) {
            throw new ConflictException("Esiste già un accessorio con il codice " + body.code());
        }
        assertHintComplete(body);
        Accessory accessory = accessoriesRepository.save(Accessory.builder()
                .accessoryCode(body.code())
                .accessoryName(body.name().strip())
                .accessorySlotCode(body.slotCode())
                .skill(findSkill(body.skillId()))
                .requiredPoints(body.requiredPoints())
                .isAvailable(body.available())
                .build());
        applyHint(accessory, body);
        return toAdmin(accessory);
    }

    @Override
    @Transactional
    public AccessoryResponseDTO update(long accessoryId, AccessoryDTO body) {
        Accessory accessory = findAccessory(accessoryId);
        // Il frontend usa il code per l'immagine; lo spazio è legato agli accessori già indossati
        if (!accessory.getAccessoryCode().equals(body.code()) || !accessory.getAccessorySlotCode().equals(body.slotCode())) {
            throw new BadRequestException("Codice e spazio dell'accessorio non si cambiano dopo la creazione");
        }
        assertHintComplete(body);
        accessory.setAccessoryName(body.name().strip());
        accessory.setSkill(findSkill(body.skillId()));
        // La soglia nuova vale per chi non l'ha ancora riscattato: chi lo possiede lo conserva (§11.1)
        accessory.setRequiredPoints(body.requiredPoints());
        accessory.setAvailable(body.available());
        applyHint(accessory, body);
        return toAdmin(accessory);
    }

    @Override
    @Transactional
    public void delete(long accessoryId) {
        Accessory accessory = findAccessory(accessoryId);
        if (studentAccessoriesRepository.existsById_AccessoryId(accessoryId)) {
            throw new ConflictException("Alcuni studenti possiedono " + accessory.getAccessoryName()
                    + ": non si elimina. Puoi renderlo non disponibile");
        }
        benefitsRepository.findById(accessoryId).ifPresent(benefitsRepository::delete);
        entityManager.flush();
        accessoriesRepository.delete(accessory);
    }

    private static void assertHintComplete(AccessoryDTO body) {
        if ((body.hintCapacity() == null) != (body.hintRechargeHours() == null)) {
            throw new BadRequestException("Per l'aiuto servono sia il numero di utilizzi sia le ore di ricarica");
        }
    }

    private void applyHint(Accessory accessory, AccessoryDTO body) {
        AccessoryBenefit benefit = benefitsRepository.findById(accessory.getAccessoryId()).orElse(null);
        if (body.hintCapacity() == null) {
            if (benefit != null) {
                if (studentAccessoriesRepository.existsById_AccessoryId(accessory.getAccessoryId())) {
                    throw new ConflictException("Alcuni studenti possiedono già questo accessorio: l'aiuto non si può togliere");
                }
                benefitsRepository.delete(benefit);
            }
            return;
        }
        if (benefit == null) {
            // Un beneficio per accessorio nel modello v0.1 (§11.3)
            entityManager.persist(AccessoryBenefit.builder().accessory(accessory).kind(TOPIC_HINT)
                    .capacity(body.hintCapacity()).rechargeHours(body.hintRechargeHours()).build());
        } else {
            // Valgono dalla prossima ricarica: le cariche già presenti restano
            benefit.setCapacity(body.hintCapacity());
            benefit.setRechargeHours(body.hintRechargeHours());
        }
        entityManager.flush();
    }

    private AccessoryResponseDTO toAdmin(Accessory accessory) {
        return AccessoryResponseDTO.from(accessory, benefitsRepository.findById(accessory.getAccessoryId()).orElse(null),
                studentAccessoriesRepository.existsById_AccessoryId(accessory.getAccessoryId()));
    }

    private Accessory findAccessory(long accessoryId) {
        return accessoriesRepository.findById(accessoryId)
                .orElseThrow(() -> new NotFoundException("Accessorio " + accessoryId + " non trovato"));
    }

    private Skill findSkill(long skillId) {
        return skillsRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill " + skillId + " non trovata"));
    }
}
