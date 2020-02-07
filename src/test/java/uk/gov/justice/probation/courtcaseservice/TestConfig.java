package uk.gov.justice.probation.courtcaseservice;

import io.restassured.RestAssured;

public class TestConfig {
    public static final String CONFIGURED_TWICE = "Attempt to configure RestAssured for both Integration and Smoke tests is not allowed";
    private static boolean configuredForInt = false;
    private static boolean configuredForSmoke = false;

    public static void configureRestAssuredForIntTest(int port) {
        /*
         Because RestAssured uses static configuration, changing config in one test can inadvertently affect
         other tests e.g. setting the base path will affect any test that runs afterwards causing unexpected failures.
         As such, all configuration should ideally be done once globally here.

         All tests are expected to set the port which should be fine because every test will correctly configure RA for
         its own needs. This does mean that tests using RA should never be run in parallel however as this will lead to
         arbitrary failures.
        */

        if (configuredForSmoke) throw new RuntimeException(CONFIGURED_TWICE);
        RestAssured.port = port;

        if (configuredForInt) return;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        configuredForInt = true;
    }

    public static void configureRestAssuredForSmokeTest(String host) {
        if (configuredForInt) throw new RuntimeException(CONFIGURED_TWICE);

        RestAssured.baseURI = host;

        if (configuredForSmoke) return;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        configuredForSmoke = true;
    }
}
