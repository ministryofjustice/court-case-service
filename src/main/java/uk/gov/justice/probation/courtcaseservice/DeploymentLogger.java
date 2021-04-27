package uk.gov.justice.probation.courtcaseservice;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

@Component
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentLogger {

    @Autowired
    private BuildProperties buildProperties;

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent readyEvent) throws UnknownHostException {
        log.info(String.format("Starting CourtCaseServiceApplication %s using Java %s on %s", buildProperties.getVersion(), System.getProperty("java.version"), Optional.ofNullable(InetAddress.getLocalHost()).map(InetAddress::getHostName).orElse("unknown machine")));
    }
}
