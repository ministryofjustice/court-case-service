package uk.gov.justice.probation.courtcaseservice.application;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "feature")
public class FeatureFlags {

    private final Map<String, Boolean> flags;

    public FeatureFlags() {
        this.flags = new HashMap<>();
    }

    public Map<String, Boolean> getFlags() {
        return flags;
    }

    public boolean sentenceData() {
        return flags.getOrDefault("fetch-sentence-data", true);
    }

    public boolean deleteHearing() { return flags.getOrDefault("delete-hearing", false); }

    public void setFlagValue(final String flagName, final boolean value) {
        flags.put(flagName, value);
    }

    public void setFlags(final Map<String, Boolean> flags) {
        this.flags.putAll(flags);
    }
}
