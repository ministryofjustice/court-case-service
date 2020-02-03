package uk.gov.justice.probation.courtcaseservice.fixtures;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CourtFixtures {
    private static final String PUT_BODY = "src/test/resources/fixtures/court/PUT_court_request.json";
    private static final String PUT_COURT_201_RESPONSE = "src/test/resources/fixtures/court/PUT_court_201_response.json";
    private static final String PUT_COURT_400_RESPONSE = "src/test/resources/fixtures/court/PUT_court_400_response.json";

    public final String createdResponseJson;
    public final String conflictResponseJson;
    public final String putBodyRequestJson;

    public CourtFixtures() throws IOException {
        createdResponseJson = CourtFixtures.getJson(PUT_COURT_201_RESPONSE);
        conflictResponseJson = CourtFixtures.getJson(PUT_COURT_400_RESPONSE);
        putBodyRequestJson = CourtFixtures.getJson(PUT_BODY);
    }


    public static String getJson(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return String.join("\n", Files.readAllLines(path));
    }
}
