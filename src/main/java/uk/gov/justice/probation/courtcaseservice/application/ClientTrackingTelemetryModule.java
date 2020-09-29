package uk.gov.justice.probation.courtcaseservice.application;

import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.extensibility.TelemetryModule;
import com.microsoft.applicationinsights.web.extensibility.modules.WebTelemetryModule;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Optional;

@Configuration
@Slf4j
public class ClientTrackingTelemetryModule implements TelemetryModule, WebTelemetryModule {

    @Override
    public void onBeginRequest(ServletRequest req, ServletResponse res) {

        var token = ((HttpServletRequest) req).getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.startsWithIgnoreCase(token, "Bearer ")) {
            try {
                var claimsSet = getClaimsFromJWT(token);
                var properties = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getProperties();

                Optional.ofNullable(claimsSet.getClaim("user_name"))
                        .map(Object::toString)
                        .ifPresent(user -> properties.put("username", user));

                Optional.ofNullable(claimsSet.getClaim("client_id"))
                        .map(Object::toString)
                        .ifPresent(clientId -> properties.put("clientId", clientId));

            } catch (ParseException e) {
                ClientTrackingTelemetryModule.log.warn("problem decoding jwt public key for application insights", e);
            }
        }
    }

    @Override
    public void onEndRequest(ServletRequest req, ServletResponse res) {
    }

    @Override
    public void initialize(TelemetryConfiguration telemetryConfiguration) {

    }
    private JWTClaimsSet getClaimsFromJWT(String token) throws ParseException {
        var signedJWT = SignedJWT.parse(token.replace("Bearer ", ""));
        return signedJWT.getJWTClaimsSet();
    }
}
