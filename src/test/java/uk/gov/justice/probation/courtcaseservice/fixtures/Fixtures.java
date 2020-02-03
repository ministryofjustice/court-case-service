package uk.gov.justice.probation.courtcaseservice.fixtures;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Fixtures {


    public static String getJson(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return String.join("\n", Files.readAllLines(path));
    }
}
