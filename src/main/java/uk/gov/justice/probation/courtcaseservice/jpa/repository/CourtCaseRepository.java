package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourtCaseRepository extends CrudRepository<CourtCaseEntity, Long> {

    @Query(value = "SELECT cc.*, null as first_created FROM court_case cc " +
        "where case_id  = :caseId " +
        "and deleted = false " +
        "order by id desc LIMIT 1",
        nativeQuery = true)
    Optional<CourtCaseEntity> findFirstByCaseIdOrderByIdDesc(String caseId);

    @Query(value = "select cc.*, grouped_cases.min_created as first_created from court_case cc " +
            "join hearing_day h on cc.id = h.court_case_id " +
            "inner join (select max(group_cc.created) as max_created,  min(group_cc.created) as min_created, group_cc.case_no, group_h.court_code from " +
            "court_case group_cc " +
            "join hearing_day group_h on group_cc.id = group_h.court_case_id " +
            "where group_cc.case_no = :caseNo " +
            "and group_h.court_code = :courtCode " +
            "group by group_cc.case_no, group_h.court_code) grouped_cases " +
            "on cc.case_no = grouped_cases.case_no " +
            "and h.court_code = grouped_cases.court_code " +
            "where cc.created = grouped_cases.max_created " +
            "and cc.deleted = false " +
            "order by cc.id desc limit 1",
            nativeQuery = true)
    Optional<CourtCaseEntity> findByCourtCodeAndCaseNo(String courtCode, String caseNo);

    @Query(value = "select cc.*, grouped_cases.min_created as first_created from court_case cc " +
                    "inner join " +
                    "   (select max(created) as max_created,  min(created) as min_created, case_id from court_case group_cc " +
                    "   where group_cc.case_id = :caseId " +
                    "   group by case_id) grouped_cases " +
                    "on cc.case_id = grouped_cases.case_id " +
                    "where cc.created = grouped_cases.max_created " +
                    "and cc.deleted = false " +
                    "order by cc.id desc  limit 1",
        nativeQuery = true)
    Optional<CourtCaseEntity> findByCaseId(String caseId);

    // TODO - need to get CourtCaseEntity values for CRN, etc, from defendant
    @Query(value = "select cc.*, grouped_cases.min_created as first_created " +
        "from court_case cc " +
        "   inner join " +
        "       (select min(group_cc.created) as min_created, max(group_cc.created) as max_created, group_cc.case_id from court_case group_cc " +
        "           inner join defendant d on d.court_case_id = group_cc.id  " +
        "           where d.defendant_id = :defendantId " +
        "           and group_cc.case_id = :caseId " +
        "           group by group_cc.case_id) grouped_cases " +
        "       on cc.case_id = grouped_cases.case_id " +
        "and cc.case_id = :caseId " +
        "and cc.created = grouped_cases.max_created " +
        "and cc.deleted = false " +
        "order by cc.id desc  limit 1",
        nativeQuery = true)
    Optional<CourtCaseEntity> findByCaseIdAndDefendantId(String caseId, String defendantId);


    @Query(value = "select cc.*, grouped_cases.min_created as first_created " +
        "from court_case cc " +
        "   inner join " +
        "       (select min(group_cc.created) as min_created, max(group_cc.created) as max_created, group_cc.case_id from court_case group_cc " +
        "           inner join hearing_day on hearing_day.court_case_id = group_cc.id  " +
        "           where ((group_cc.created >= :createdAfter and group_cc.created < :createdBefore) " +
        "             or (group_cc.created >= :createdBefore and group_cc.manual_update is true)) " +
        "           and hearing.court_code = :courtCode " +
        "           group by group_cc.case_id) grouped_cases " +
        "       on cc.case_id = grouped_cases.case_id " +
        "   inner join hearing_day h on h.court_case_id = cc.id " +
        "where h.hearing_day = :hearingDay " +
        "and cc.created = grouped_cases.max_created " +
        "and cc.deleted = false",
        nativeQuery = true)
    List<CourtCaseEntity> findByCourtCodeAndHearingDay(
        String courtCode,
        LocalDate hearingDay,
        LocalDateTime createdAfter,
        LocalDateTime createdBefore
    );

    @Query(value = "select cc.created " +
        "from court_case cc " +
        "inner join hearing_day h on h.court_case_id = cc.id " +
        "where h.court_code = :courtCode " +
        "and h.hearing_day = :hearingDay " +
        "order by created desc limit 1",
        nativeQuery = true)
    Optional<LocalDateTime> findLastModifiedByHearingDay(String courtCode, LocalDate hearingDay);

}
