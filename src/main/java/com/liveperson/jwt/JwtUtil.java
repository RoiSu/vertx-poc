package com.liveperson.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.codec.binary.Base64;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;

/**
 * Created by rois on 6/27/16.
 */
public class JwtUtil {


    private static KeyFactory kf;
    private static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCsJGVwx7NrGbXxL26Wb9CN/1kkHMc99xfQ19BQN+5PrwVYdKN0SgTkUD+rCNx4KA2A3IABAB7h5PMh2ZqmieGExfxuDarLk9klIzqjGuLv/asssPHh6prgI65ta1JAbFMy+KI6G582QWYjXXzExgOuIGXT2v8\\\"eCQUJV0/yQixkdQIDAQAB";
    private  static JWSVerifier jwsVerifier;

    static{
        try {
            kf = KeyFactory.getInstance("RSA");
            jwsVerifier = new RSASSAVerifier((RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(publicKey.getBytes()))));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }



    public static boolean isVerifiedJwt(String jwt){
        SignedJWT parsedJWT = null;
        try {
            parsedJWT = SignedJWT.parse(jwt);

            return parsedJWT.verify(jwsVerifier);
        } catch (JOSEException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}
