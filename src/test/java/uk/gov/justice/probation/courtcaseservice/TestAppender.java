package uk.gov.justice.probation.courtcaseservice;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;
import java.util.List;

public class TestAppender extends AppenderBase<LoggingEvent> {
    public static List<LoggingEvent> events = new ArrayList<>();

    @Override
    protected void append(LoggingEvent e) {
        events.add(e);
    }
}
