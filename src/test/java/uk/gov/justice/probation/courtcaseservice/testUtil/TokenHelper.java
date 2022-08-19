package uk.gov.justice.probation.courtcaseservice.testUtil;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import uk.gov.justice.probation.courtcaseservice.controller.CourtCaseControllerIntTest;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAMultiPrimePrivateCrtKeySpec;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;

import static uk.gov.justice.probation.courtcaseservice.Constants.USER_UUID_CLAIM_NAME;

public class TokenHelper {
    private static String cachedToken = null;
    private static String cachedTokenUuid = null;

    public static String TEST_UUID = "fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81";

    public static String getToken(final String uuid) {
        if (cachedToken == null || !StringUtils.equals(uuid, cachedTokenUuid)) {
            try {
                cachedToken = createToken(uuid);
                cachedTokenUuid = uuid;
            } catch (IOException | ParseException | NoSuchAlgorithmException | InvalidKeySpecException | JOSEException e) {
                throw new RuntimeException(e);
            }
        }
        return cachedToken;
    }

    public static String getToken() {
        return getToken(TEST_UUID);
    }

    public static String createToken(final String uuid) throws IOException, ParseException, NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
        JWKSet localKeys = JWKSet.load(new ClassPathResource("mocks/test_keypair_jwks.json").getFile());
        RSAKey jwk = (RSAKey) localKeys.getKeyByKeyId(CourtCaseControllerIntTest.KEY_ID);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey rsaJWK = kf.generatePrivate(buildPrivateKeySpec(jwk));

        // Create RSA-signer with the private key
        JWSSigner signer = new RSASSASigner(rsaJWK);

        // Prepare JWT with claims set
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("TEST.USER")
                .issuer("http://localhost:8090/auth/issuer")
                .claim("authorities", Collections.singletonList("ROLE_PREPARE_A_CASE"))
                .claim(USER_UUID_CLAIM_NAME, uuid)
                .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(CourtCaseControllerIntTest.KEY_ID).build(),
                claimsSet);

        // Compute the RSA signature
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public static KeySpec buildPrivateKeySpec(RSAKey jwk) {
        BigInteger publicKeyModulus = jwk.getModulus().decodeToBigInteger();
        BigInteger publicExponent = jwk.getPublicExponent().decodeToBigInteger();
        BigInteger privateExponent = jwk.getPrivateExponent().decodeToBigInteger();
        BigInteger primeP = jwk.getFirstPrimeFactor().decodeToBigInteger();
        BigInteger primeQ = jwk.getSecondPrimeFactor().decodeToBigInteger();
        BigInteger primeExponentP = jwk.getFirstFactorCRTExponent().decodeToBigInteger();
        BigInteger primeExponentQ = jwk.getSecondFactorCRTExponent().decodeToBigInteger();
        BigInteger crtCoefficient = jwk.getSecondFactorCRTExponent().decodeToBigInteger();

        return new RSAMultiPrimePrivateCrtKeySpec(
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
    }
}
