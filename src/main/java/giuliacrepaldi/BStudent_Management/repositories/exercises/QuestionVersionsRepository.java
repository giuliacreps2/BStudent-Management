package giuliacrepaldi.BStudent_Management.repositories.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionVersion;
import giuliacrepaldi.BStudent_Management.payloads.exercises.QuestionBankEntryVersionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface QuestionVersionsRepository extends JpaRepository<QuestionVersion, Long> {

    List<QuestionVersion> findByQuestionBankEntry_QuestionBankEntryIdOrderByQuestionVersionNumber(Long entryId);

    @Query("""
            SELECT COUNT(v) > 0 FROM QuestionVersion v
            WHERE v.questionBankEntry.questionBankEntryId = :entryId AND v.isPublished = true
            """)
    boolean existsPublishedVersion(@Param("entryId") Long questionBankEntryId);

    // 0 se l'esercizio non ha versioni: la nuova è max + 1
    @Query("""
            SELECT COALESCE(MAX(v.questionVersionNumber), 0) FROM QuestionVersion v
            WHERE v.questionBankEntry.questionBankEntryId = :entryId
            """)
    int findMaxVersionNumber(@Param("entryId") Long questionBankEntryId);

    // Eseguita subito sul DB, prima di pubblicare la nuova: l'indice unico parziale
    // uq_question_versions_published non è DEFERRABLE. Un UPDATE JPQL non passa da @UpdateTimestamp:
    // le date si impostano qui
    @Modifying
    @Query("""
            UPDATE QuestionVersion v
            SET v.isPublished = false, v.replacedAt = :now, v.updatedAt = :now
            WHERE v.questionBankEntry.questionBankEntryId = :entryId AND v.isPublished = true
            """)
    int unpublishAll(@Param("entryId") Long questionBankEntryId, @Param("now") OffsetDateTime now);

    // Per l'elenco degli esercizi: prima versione e versione in uso, in una sola query per pagina
    @Query("""
            SELECT new giuliacrepaldi.BStudent_Management.payloads.exercises.QuestionBankEntryVersionInfo(
                v.questionBankEntry.questionBankEntryId,
                MIN(v.createdAt),
                MAX(CASE WHEN v.isPublished = true THEN v.questionVersionNumber ELSE NULL END),
                MAX(CASE WHEN v.isPublished = true THEN v.publishedAt ELSE NULL END))
            FROM QuestionVersion v
            WHERE v.questionBankEntry.questionBankEntryId IN :entryIds
            GROUP BY v.questionBankEntry.questionBankEntryId
            """)
    List<QuestionBankEntryVersionInfo> findVersionInfo(@Param("entryIds") Collection<Long> entryIds);
}
