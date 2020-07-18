package uk.gov.justice.probation.courtcaseservice.testUtil;

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
import org.springframework.core.io.ClassPathResource;
import uk.gov.justice.probation.courtcaseservice.controller.CourtCaseControllerIntTest;

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
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenHelper {
    private static String cachedToken = null;

    public static String getToken() {
        if (cachedToken == null) {
            try {
                cachedToken = createToken();
            } catch (IOException | ParseException | NoSuchAlgorithmException | InvalidKeySpecException | JOSEException e) {
                throw new RuntimeException(e);
            }
        }
        return cachedToken;
    }

    public static String createToken() throws IOException, ParseException, NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
        JWKSet localKeys = JWKSet.load(new ClassPathResource("mocks/test_keypair_jwks.json").getFile());
        RSAKey jwk = (RSAKey) localKeys.getKeyByKeyId(CourtCaseControllerIntTest.KEY_ID);

        BigInteger publicKeyModulus = jwk.getModulus().decodeToBigInteger();
        BigInteger publicExponent = jwk.getPublicExponent().decodeToBigInteger();
        BigInteger privateExponent = jwk.getPrivateExponent().decodeToBigInteger();
        BigInteger primeP = jwk.getFirstPrimeFactor().decodeToBigInteger();
        BigInteger primeQ = jwk.getSecondPrimeFactor().decodeToBigInteger();
        BigInteger primeExponentP = jwk.getFirstFactorCRTExponent().decodeToBigInteger();
        BigInteger primeExponentQ = jwk.getSecondFactorCRTExponent().decodeToBigInteger();
        BigInteger crtCoefficient = jwk.getSecondFactorCRTExponent().decodeToBigInteger();

        KeySpec keySpec = new RSAMultiPrimePrivateCrtKeySpec(
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
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(CourtCaseControllerIntTest.KEY_ID).build(),
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
}
