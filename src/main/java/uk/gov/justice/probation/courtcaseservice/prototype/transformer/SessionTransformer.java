package uk.gov.justice.probation.courtlistservice.prototype.transformer;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtlistservice.prototype.data.api.Session;
import uk.gov.justice.probation.courtlistservice.prototype.data.entity.SessionType;
import uk.gov.justice.probation.courtlistservice.prototype.data.entity.SessionsType;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SessionTransformer {
    private final BlockTransformer blockTransformer;

    public SessionTransformer(BlockTransformer blockTransformer) {
        this.blockTransformer = blockTransformer;
    }

    private Session toSession(SessionType session) {
        return Session
                .builder()
                .id(session.getSId())
                .dateOfHearing(DateTimeHelper.asDate(session.getDoh()))
                .localJusticeArea(session.getLja())
                .crimeManagementUnit(session.getCmu())
                .courtName(session.getCourt())
                .courtRoom(session.getRoom())
                .startTime(DateTimeHelper.asTime(session.getSstart()))
                .endTime(DateTimeHelper.asTime(session.getSend()))
                .blocks(blockTransformer.toBlocks(session.getBlocks()))
                .build();
    }

    public List<Session> toSessions(SessionsType sessions) {
        return sessions.getSession().stream().map(this::toSession).collect(Collectors.toList());
    }
}
