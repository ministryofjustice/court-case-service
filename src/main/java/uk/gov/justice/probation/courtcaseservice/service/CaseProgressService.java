package uk.gov.justice.probation.courtcaseservice.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingNoteResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNotesRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CaseProgressMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CaseProgressService {

    private final HearingRepository hearingRepository;
    private final CaseProgressMapper caseProgressMapper;
    private final HearingNotesRepository hearingNotesRepository;

    public CaseProgressService(HearingRepository hearingRepository, CaseProgressMapper caseProgressMapper,
                               HearingNotesRepository hearingNotesRepository) {
        this.hearingRepository = hearingRepository;
        this.caseProgressMapper = caseProgressMapper;
        this.hearingNotesRepository = hearingNotesRepository;
    }

    public List<CaseProgressHearing> getCaseHearingProgress(String caseId) {
        return hearingRepository.findHearingsByCaseId(caseId)
            .map(caseProgressMapper::mapFrom)
            .map(caseProgressHearings ->
                populateHearingNotes(caseProgressHearings)
            )
            .orElse(null);
    }

    @NotNull
    private List<CaseProgressHearing> populateHearingNotes(List<CaseProgressHearing> caseProgressHearings) {

        return caseProgressHearings.stream().map(caseProgressHearing -> caseProgressHearing.withNotes(
            hearingNotesRepository.findAllByHearingIdAndDeletedFalse(caseProgressHearing.getHearingId())
                .map(hearingNoteEntities -> hearingNoteEntities.stream().map(hearingNoteEntity -> HearingNoteResponse.of(hearingNoteEntity)).collect(Collectors.toList())).orElse(null)
            )).collect(Collectors.toList());
    }
}
