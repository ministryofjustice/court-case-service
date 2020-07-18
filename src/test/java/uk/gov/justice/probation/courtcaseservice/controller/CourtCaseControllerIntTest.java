package uk.gov.justice.probation.courtcaseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.probation.courtcaseservice.TestConfig;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAMultiPrimePrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.TestConfig.WIREMOCK_PORT;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "classpath:before-test.sql", config = @SqlConfig(transactionMode = ISOLATED))
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class CourtCaseControllerIntTest {
    public static final String KEY_ID = "mock-key";
    private String token;

    @LocalServerPort
    private int port;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    CourtCaseRepository courtCaseRepository;

    private static final String COURT_CODE = "SHF";
    private static final String CASE_NO = "1600028913";
    private static final String PROBATION_STATUS = "Previously known";
    private static final String NOT_FOUND_COURT_CODE = "LPL";
    private static final String DEFENDANT_NAME = "JTEST";
    private final LocalDateTime now = LocalDateTime.now();

    @ClassRule
    public static final WireMockClassRule wireMockRule = new WireMockClassRule(wireMockConfig()
            .port(WIREMOCK_PORT)
            .usingFilesUnderClasspath("mocks"));

    @Rule
    public WireMockClassRule instanceRule = wireMockRule;

    @Before
    public void setup() throws InvalidKeySpecException, NoSuchAlgorithmException, ParseException, JOSEException {
        TestConfig.configureRestAssuredForIntTest(port);
        token = createToken();
    }

    private String createToken() throws InvalidKeySpecException, NoSuchAlgorithmException, JOSEException, ParseException {
        RSAMultiPrimePrivateCrtKeySpec privateCrtKeySpec;
        RSAKey jwk;
        try {

            JWKSet localKeys = JWKSet.load(new ClassPathResource("mocks/test_keypair_jwks.json").getFile());
            jwk = (RSAKey) localKeys.getKeyByKeyId(KEY_ID);

            BigInteger publicKeyModulus = jwk.getModulus().decodeToBigInteger();
            BigInteger publicExponent = jwk.getPublicExponent().decodeToBigInteger();
            BigInteger privateExponent = jwk.getPrivateExponent().decodeToBigInteger();
            BigInteger primeP = jwk.getFirstPrimeFactor().decodeToBigInteger();
            BigInteger primeQ = jwk.getSecondPrimeFactor().decodeToBigInteger();
            BigInteger primeExponentP = jwk.getFirstFactorCRTExponent().decodeToBigInteger();
            BigInteger primeExponentQ = jwk.getSecondFactorCRTExponent().decodeToBigInteger();
            BigInteger crtCoefficient = jwk.getSecondFactorCRTExponent().decodeToBigInteger();

            privateCrtKeySpec = new RSAMultiPrimePrivateCrtKeySpec(
                    publicKeyModulus,
                    publicExponent,
                    privateExponent,
                    primeP,
                    primeQ,
                    primeExponentP,
                    primeExponentQ,
                    crtCoefficient,
                    null
            );

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        KeySpec keySpec = privateCrtKeySpec;

        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(jwk.getModulus().decodeToBigInteger(), jwk.getPublicExponent().decodeToBigInteger());
        RSAPublicKey rsaPublicJWK = (RSAPublicKey) kf.generatePublic(rsaPublicKeySpec);
        PrivateKey rsaJWK = kf.generatePrivate(keySpec);

// Create RSA-signer with the private key
        JWSSigner signer = new RSASSASigner(rsaJWK);

// Prepare JWT with claims set
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("TEST.USER")
                .issuer("http://localhost:8090/auth/issuer")
                .claim("authorities", Collections.singletonList("ROLE_PREPARE_A_CASE"))
                .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(KEY_ID).build(),
                claimsSet);

// Compute the RSA signature
        signedJWT.sign(signer);

// To serialize to compact form, produces something like
// eyJhbGciOiJSUzI1NiJ9.SW4gUlNBIHdlIHRydXN0IQ.IRMQENi4nJyp4er2L
// mZq3ivwoAjqa1uUkSBKFIX7ATndFF5ivnt-m8uApHO4kfIFOrW7w2Ezmlg3Qd
// maXlS9DhN0nUk_hGI3amEjkKd0BWYCB8vfUbUv0XGjQip78AI4z1PrFRNidm7
// -jPDm5Iq0SZnjKjCNS5Q15fokXZc8u0A
        String s = signedJWT.serialize();

// On the consumer side, parse the JWS and verify its RSA signature
        signedJWT = SignedJWT.parse(s);

        JWSVerifier verifier = new RSASSAVerifier(rsaPublicJWK);
        assertThat(signedJWT.verify(verifier)).isTrue();
        return signedJWT.serialize();
    }

    @Test
    public void cases_shouldGetCaseListWhenCasesExist() {

        given()
                .auth()
                .oauth2(token)
                .when()
                .get("/court/{courtCode}/cases?date={date}", COURT_CODE, LocalDate.of(2019, 12, 14).format(DateTimeFormatter.ISO_DATE))
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases[0].courtCode", equalTo(COURT_CODE))
                .body("cases[0].caseId", equalTo("5555555"))
                .body("cases[0].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 9, 0).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[0].offences", hasSize(2))
                .body("cases[0].offences[0].sequenceNumber", equalTo(1))
                .body("cases[0].offences[1].sequenceNumber", equalTo(2))
                .body("cases[1].lastUpdated", containsString(now.format(DateTimeFormatter.ISO_DATE)))
                .body("cases[1].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 0, 0).format(DateTimeFormatter.ISO_DATE_TIME)))
                .body("cases[2].sessionStartTime", equalTo(LocalDateTime.of(2019, 12, 14, 23, 59, 59).format(DateTimeFormatter.ISO_DATE_TIME)));
    }

    @Test
    public void GET_cases_shouldGetEmptyCaseListWhenNoCasesMatch() {
        when()
                .get("/court/{courtCode}/cases?date={date}", COURT_CODE, "2020-02-02")
                .then()
                .assertThat()
                .statusCode(200)
                .body("cases", empty());

    }

    @Test
    public void GET_cases_shouldReturn400BadRequestWhenNoDateProvided() {
        when()
                .get("/court/{courtCode}/cases", COURT_CODE)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Required LocalDate parameter 'date' is not present"));
    }

    @Test
    public void GET_cases_shouldReturn404NotFoundWhenCourtDoesNotExist() {
        ErrorResponse result = when()
                .get("/court/{courtCode}/cases?date={date}", NOT_FOUND_COURT_CODE, "2020-02-02")
                .then()
                .assertThat()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getDeveloperMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
        assertThat(result.getUserMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
        assertThat(result.getStatus()).isEqualTo(404);
    }


    @Test
    public void shouldGetCaseWhenCourtExists() {
        given()
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, CASE_NO)
                .then()
                .assertThat().statusCode(200);
    }

    @Test
    public void shouldGetCaseWhenExists() {
        String startTime = LocalDateTime.of(2019, Month.DECEMBER, 14, 9, 0, 0)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        given()
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, CASE_NO)
                .then()
                .statusCode(200)
                .body("caseNo", equalTo(CASE_NO))
                .body("offences", hasSize(2))
                .body("offences[0].offenceTitle", equalTo("Theft from a shop"))
                .body("offences[0].offenceSummary", equalTo("On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person."))
                .body("offences[0].act", equalTo("Contrary to section 1(1) and 7 of the Theft Act 1968."))
                .body("offences[1].offenceTitle", equalTo("Theft from a different shop"))
                .body("probationStatus", equalTo(PROBATION_STATUS))
                .body("previouslyKnownTerminationDate", equalTo(LocalDate.of(2010, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("suspendedSentenceOrder", equalTo(true))
                .body("breach", equalTo(true))
                .body("crn", equalTo("X320741"))
                .body("cro", equalTo("311462/13E"))
                .body("pnc", equalTo("A/1234560BA"))
                .body("listNo", equalTo("3rd"))
                .body("courtCode", equalTo(COURT_CODE))
                .body("sessionStartTime", equalTo(startTime))
                .body("lastUpdated", equalTo(startTime))
                .body("defendantName", equalTo(DEFENDANT_NAME))
                .body("defendantAddress.line1", equalTo("27"))
                .body("defendantAddress.line2", equalTo("Elm Place"))
                .body("defendantAddress.postcode", equalTo("ad21 5dr"))
                .body("defendantAddress.line3", equalTo("Bangor"))
                .body("defendantAddress.line4", equalTo(null))
                .body("defendantAddress.line5", equalTo(null))
                .body("defendantDob", equalTo(LocalDate.of(1958, Month.OCTOBER, 10).format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .body("defendantSex", equalTo("M"))
                .body("nationality1", equalTo("British"))
                .body("nationality2", equalTo("Polish"))
                .body("removed", equalTo(false))
                .body("createdToday", equalTo(true))
        ;
    }


    @Test
    public void shouldReturnNotFoundForNonexistentCase() {

        String NOT_FOUND_CASE_NO = "11111111111";

        ErrorResponse result = given()
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", COURT_CODE, NOT_FOUND_CASE_NO)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getDeveloperMessage()).contains("Case " + NOT_FOUND_CASE_NO + " not found");
        assertThat(result.getUserMessage()).contains("Case " + NOT_FOUND_CASE_NO + " not found");
        assertThat(result.getStatus()).isEqualTo(404);
    }

    @Test
    public void shouldReturnNotFoundForNonexistentCourt() {
        ErrorResponse result = given()
                .when()
                .header("Accept", "application/json")
                .get("/court/{courtCode}/case/{caseNo}", NOT_FOUND_COURT_CODE, CASE_NO)
                .then()
                .statusCode(404)
                .extract()
                .body()
                .as(ErrorResponse.class);

        assertThat(result.getDeveloperMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
        assertThat(result.getUserMessage()).contains("Court " + NOT_FOUND_COURT_CODE + " not found");
        assertThat(result.getStatus()).isEqualTo(404);
    }

}
