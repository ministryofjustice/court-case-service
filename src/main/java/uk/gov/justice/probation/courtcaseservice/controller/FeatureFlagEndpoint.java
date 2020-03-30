package uk.gov.justice.probation.courtcaseservice.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.swagger.annotations.Api;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;

@Api(tags = "Feature Flags")
@RestController
class FeatureFlagEndpoint {

    private final FeatureFlags featureFlags;

    @Autowired
    public FeatureFlagEndpoint(final FeatureFlags toggles) {
        this.featureFlags = toggles;
    }

    @CrossOrigin
    @GetMapping(value = "/feature-flags", produces = APPLICATION_JSON_VALUE)
    public Map<String, Boolean> featureToggles() {
        return featureFlags.getFlags();
    }
}

