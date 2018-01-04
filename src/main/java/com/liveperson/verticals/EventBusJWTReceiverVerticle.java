package com.liveperson.verticals;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import org.apache.commons.codec.binary.Base64;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;

/**
 * Created by rois on 1/19/16.
 */
public class EventBusJWTReceiverVerticle extends AbstractVerticle {

    private String name = null;
    public static String listeningAddress = "jwt.tokens.request";
    public static String sendAddress = "jwt.tokens.result";
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

    public EventBusJWTReceiverVerticle(String name) {
        this.name = name;
    }

    public void start(Future<Void> startFuture) {
        vertx.eventBus().consumer(listeningAddress, message -> {
            //System.out.println(this.name + " received message: " +   message.body());
            try {
                SignedJWT parsedJWT = SignedJWT.parse(message.body().toString());
                boolean isVerified = parsedJWT.verify(jwsVerifier);
                //vertx.eventBus().send   (sendAddress, ((Boolean)isVerified).toString());
                message.reply(isVerified);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (JOSEException e) {
                e.printStackTrace();
            }

        });
    }
}
