package uk.gov.justice.probation.courtcaseservice.application;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.Optional;

@Configuration
@Slf4j
@AllArgsConstructor
public class ClientTrackingInterceptor implements HandlerInterceptor {

    private final ClientDetails clientDetails;

    public boolean preHandle(HttpServletRequest req, @NonNull HttpServletResponse response, @NonNull Object handler) {

        var token = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.startsWithIgnoreCase(token, "Bearer ")) {
            try {
                var claimsSet = getClaimsFromJWT(token);

                final var username = Optional.ofNullable(claimsSet.getClaim("user_name"))
                        .map(Object::toString)
                        .orElse(null);

                final var clientId = Optional.ofNullable(claimsSet.getClaim("client_id"))
                        .map(Object::toString)
                        .orElse(null);
                clientDetails.setClientDetails(clientId, username);

            } catch (ParseException e) {
                log.warn("problem decoding jwt public key for application insights", e);
            }
        }
        return true;
    }

    private JWTClaimsSet getClaimsFromJWT(String token) throws ParseException {
        var signedJWT = SignedJWT.parse(token.replace("Bearer ", ""));
        return signedJWT.getJWTClaimsSet();
    }
}
