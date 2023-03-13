package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface HearingNotesRepository extends CrudRepository<HearingNoteEntity, Long>{
    Optional<List<HearingNoteEntity>> findAllByHearingIdAndDeletedFalse(String hearingId);
    Optional<HearingNoteEntity> findByHearingIdAndCreatedByUuidAndDraftIsTrue(String hearingId, String createdByUuid);
}
