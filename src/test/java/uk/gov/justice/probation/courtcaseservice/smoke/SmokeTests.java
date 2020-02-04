package uk.gov.justice.probation.courtcaseservice.smoke;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.justice.probation.courtcaseservice.test.SmokeTest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

@Category(SmokeTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class SmokeTests {


    private static final String COURT_CODE = "FOO";
    private static final String PUT_BODY =
            "{" +
                    "\"name\": \"Sheffield Magistrates Court\"," +
                    "\"courtCode\": \"" + COURT_CODE + "\"" +
                    "}";

    @Value("${smoke-test.target-host:http://localhost:8080}")
    private String host;

    @Before
    public void setUp() {
        RestAssured.baseURI = host;
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
    public void givenCourtCodeInPathAndBodyConflict_whenCourtCreated_thenReturnBadRequest() {
        given().body(PUT_BODY)
                .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .put("/court/BAD/")
                .then()
                .assertThat()
                .statusCode(400);
    }
}
