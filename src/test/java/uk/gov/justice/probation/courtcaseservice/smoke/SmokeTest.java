package uk.gov.justice.probation.courtcaseservice.smoke;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.probation.courtcaseservice.TestConfig;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringJUnit4ClassRunner.class)
public class SmokeTest {

    @Value("${smoke-test.target-host:http://localhost:8080}")
    private String host;

    @Before
    public void setUp() {
        TestConfig.configureRestAssuredForSmokeTest(host);
    }

    @Test
    public void whenPing_thenPong() {
        when()
                .get("/ping/")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(200)
                .body(equalTo("pong"));
    }

    @Test
    public void givenCourtDoesNotExist_whenGetCase_thenReturnError() {
        when()
                .get("/court/NOT_A_COURT/case/NOT_A_CASE")
                .then()
                .assertThat()
                .statusCode(404)
                .body("developerMessage", equalTo("Court NOT_A_COURT not found"));
    }

    @Test
    public void givenCaseDoesNotExist_whenGetCase_thenReturnError() {
        when()
                .get("/court/SHF/case/NOT_A_CASE")
                .then()
                .assertThat()
                .statusCode(404)
                .body("developerMessage", equalTo("Case NOT_A_CASE not found"));
    }

    @Test
    public void whenGetCasesForToday_thenReturnSomething() {
        when()
                .get(String.format("/court/SHF/cases?date=%s", LocalDate.now().format(DateTimeFormatter.ISO_DATE)))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", notNullValue());
    }
}
