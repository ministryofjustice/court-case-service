package uk.gov.justice.probation.courtcaseservice.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;

@RestController
class FeatureFlagEndpoint {

    private final FeatureFlags featureFlags;

    @Autowired
    public FeatureFlagEndpoint(final FeatureFlags toggles) {
        this.featureFlags = toggles;
    }

    @CrossOrigin
    @RequestMapping(value = "/feature-flags")
    public Map<String, Boolean> featureToggles() {
        return featureFlags.getFlags();
    }
}

