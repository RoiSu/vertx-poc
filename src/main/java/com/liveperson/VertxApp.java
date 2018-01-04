package com.liveperson;

import com.liveperson.verticals.EventBusJWTReceiverVerticle;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.concurrent.atomic.AtomicLong;

import io.vertx.ext.web.RoutingContext;
import io.vertx.rx.java.ObservableHandler;
import io.vertx.rx.java.RxHelper;
import org.apache.commons.codec.binary.Base64;
import rx.Observable;

/**
 * Created by rois on 1/9/16.
 */
public class VertxApp {

    private static final Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(5));
    private static final HttpClient httpClient = vertx.createHttpClient();
    private static final AtomicLong atomicLong = new AtomicLong(0);
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

    public VertxApp() throws NoSuchAlgorithmException {
    }


    private static Boolean verifyParsedJwt(SignedJWT parsedJWT) throws JOSEException {
        return parsedJWT.verify(jwsVerifier);
    }



    public static void main(String[] args) {
        String host = args[0];
        Integer listeningPort = Integer.parseInt(args[1]);
        Integer targetPort = Integer.parseInt(args[2]);
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        EventBus eb = vertx.eventBus();
        DeploymentOptions options = new DeploymentOptions().setWorker(true);//.setMultiThreaded(true);
        vertx.deployVerticle(new EventBusJWTReceiverVerticle("R1JWT"), options);


        router.route("/validateJwt").handler(routingContext -> {

            //httpCall(routingContext.response(),"mockbin.org", "/delay/2000") ;
            //httpCall(routingContext.response(),"localhost", "/fastRes") ;
            validateJwtFromRequest(routingContext);

        });



        router.route("/authHttpCall").handler(routingContext -> {

            HttpClientRequest httpRequest = createHttpCall(routingContext.response(), host, "/jwtResponse", targetPort);
            httpRequest.handler(httpCallResponse  -> {
                httpCallResponse.bodyHandler(bodyBufferResult -> {
                    boolean jwtValidationResult = validateJwt(bodyBufferResult);
                    //System.out.println("isVerified = " + jwtValidationResult);
                    routingContext.response().putHeader("content-type", "application/json");
                    routingContext.response().end("{\"isVerified\" : \"" +  jwtValidationResult + "\")");
                });

            });
            httpRequest.end();
        });

        router.route("/authHttpCallEb").handler(routingContext -> {

            HttpClientRequest httpRequest = createHttpCall(routingContext.response(), host, "/jwtResponse", targetPort);
            httpRequest.handler(httpCallResponse  -> {
                httpCallResponse.bodyHandler(bodyBufferResult -> {
                    JsonObject object = bodyBufferResult.toJsonObject();
                    String jwt = object.getString("jwt");
                    eb.send(EventBusJWTReceiverVerticle.listeningAddress, jwt, replyMessage -> {
                        //System.out.println(replyMessage.result().body());
                        routingContext.response().putHeader("content-type", "application/json");
                        // Write to the response and end it
                        routingContext.response().end("{\"isVerified\" : \"" +replyMessage.result().body().toString()  + "\"}");
                    });
                });

            });
            httpRequest.end();
        });


        ObservableHandler<RoutingContext> routingContextObservable = RxHelper.observableHandler();
        ObservableHandler<JsonObject> responseJsonObjectObservable = RxHelper.observableHandler();
        router.route("/authHttpCallRx").handler(routingContext -> {
            routingContextObservable.just(routingContext);
          /*  HttpClientRequest httpRequest = createHttpCall(routingContext.response(), host, "/jwtResponse", targetPort);
            httpRequest.handler(httpCallResponse  -> {

                httpCallResponse.bodyHandler(bodyBufferResult -> {
                    JsonObject object = bodyBufferResult.toJsonObject();
                    String jwt = object.getString("jwt");
                    eb.send(EventBusJWTReceiverVerticle.listeningAddress, jwt, replyMessage -> {
                        //System.out.println(replyMessage.result().body());
                        routingContext.response().putHeader("content-type", "application/json");
                        // Write to the response and end it
                        routingContext.response().end("{\"isVerified\" : \"" +replyMessage.result().body().toString()  + "\"}");
                    });
                });

            });
            httpRequest.end();*/
        });
        routingContextObservable.doOnNext(routingContext ->
            {
                HttpClientRequest httpClientRequest = createHttpCall(routingContext.response(), host, "/jwtResponse", targetPort);
                responseJsonObjectObservable.just(routingContext.getBodyAsJson());
            });

        responseJsonObjectObservable.doOnNext(bodyjson -> {
            System.out.println(bodyjson.toString());
        });

        router.route("/fastResTest").handler(routingContext -> {
            System.out.println(System.currentTimeMillis() + " " + atomicLong.getAndIncrement());
            routingContext.response().putHeader("content-type", "application/json");
            // Write to the response and end it
            routingContext.response().end("{\"test\" : \"Hello World from vertx \"}");

        });

        router.route("/block").handler(routingContext -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        router.route("/jwtResponse").handler(routingContext -> {
            //System.out.println("responding jwt");
            routingContext.response().end("{\"jwt\" : \"eyJraWQiOiIwMDAwMSIsInR5cCI6IkpXVCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiIxMDMyZjA3YTI0ZTUxYTE3ZmE2NDhhMDgyMDA2YzgxZjJjZmFmM2Y0ZTUyZmFmY2FiNDc0ZjQ3MDhkYjhmMTQ3IiwiYXVkIjoiYWNjOnFhMzY4MDQ2MjEiLCJpc3MiOiJodHRwczpcL1wvaWRwLmxpdmVwZXJzb24ubmV0IiwibHAuZXh0Ijp7InN1YiI6ImxwVGVzdHJvaW92ZXJyaWRlRXhwaXJlQ29kZUF0LTE3OTcwNjY2MTEwMDAiLCJpc3MiOiJodHRwczpcL1wvY3VzdG9tZXJXZWJTaXRlLmNvbSIsImxwX3NkZXMiOiJhMzVhM2M3NThmZmEwODRhNjkxMTg1M2RiODEyNmIxOTk1ZTA3NDExOWQ4NDkxYjk0ZjQ3YWNkOWVlM2IyNDBjOWM1MjZjNzk1ZmNjNTcxOTI5ZThmNTkzNWE2YmEwZTllZTE5YWU4NDkyMmY3MTlmZDU3NjVkYWZiNzc2NGFlZTIwMmQ5Mzg4MjQ5M2Q5NzA2MjA4OWI2MGM4ZDYzNzQ3MzZmMTg0ZTc5Zjk2ZjU4MDYxNjBhYWU5NjQ0ZWU1YzQ2OTYzMDNiMzJjMDY4NjU4YjBjN2E4YzA2YmY0YjJhNzJlNTc1ODRkZWJhOTBhNDg3MGM1NTAyM2I3MjI0NmJlOWJiOWUxMzc4ZjMyNzVjMDZhMTUwMDg5Mjg3MTkyZmNlZGE3MTA5OWI3ZTFhZjNhNjQ5YjRhYTVmMmRhMjQ3M2FhNjdiMGFmYmU5M2Q3ZmNmNGQzMWE1MzE4MTY0MDc2M2I4ZTBkNTVhMTk0NGQ2ZmExZGYzYmEzYzA2OTYyOGY2ZDAyOGU2MzU2OTIxYmE5ZmUwMmUzMmRjZTI1MmYzMyJ9LCJleHAiOjE3OTcwNjY2MTEsImlhdCI6MTQ2NTkxMjM2OX0.q1WQUhCv7bbJu6JsD95KOZOVgmBwcV2n-M-KxVQq-AUyY9bUy8zytb_E_V_ddXV3U6TgUj_PfydPVXEqt3MLhjBlmfmVyknEx_llqaCUnSymtQAcXcn1vso2KB5oBHD8YCmccbc8Cn1pNfTEay4rBpF7hhWG_tT4UqqRs64qoHU\"}");
        });

        server.requestHandler(router::accept).listen(listeningPort);
        System.out.println("started listening on port " + listeningPort);
    }

    private static void validateJwtFromRequest(RoutingContext routingContext) {
        try {
            SignedJWT parsedJWT = SignedJWT.parse(routingContext.request().getParam("jwt"));
            //rx.Observable.just(verifyParsedJwt(parsedJWT)).forEach((routingContext.response().end("{\"isVerified\" : \"" + x  + "\"}")));
            boolean isVerified = parsedJWT.verify(jwsVerifier);
            //System.out.println(parsedJWT.getJWTClaimsSet().toString());
            routingContext.response().putHeader("content-type", "application/json");
            // Write to the response and end it
            routingContext.response().end("{\"isVerified\" : \"" +isVerified  + "\"}");
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        }
    }

    public static boolean validateJwt(Buffer routingContext) {
        try {
            JsonObject object = routingContext.toJsonObject();
            String jwt = object.getString("jwt");
            SignedJWT parsedJWT = SignedJWT.parse(jwt);
            //rx.Observable.just(verifyParsedJwt(parsedJWT)).forEach((routingContext.response().end("{\"isVerified\" : \"" + x  + "\"}")));
            boolean isVerified = parsedJWT.verify(jwsVerifier);
            //System.out.println(parsedJWT.getJWTClaimsSet().toString());
            return isVerified;
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static HttpClientRequest createHttpCall(final HttpServerResponse response, String domain, String path, int port) {
        HttpClientRequest httpClientRequest = httpClient.get(port, domain, path);/*, new Handler<HttpClientResponse>() {

            @Override
            public void handle(HttpClientResponse httpClientResponse) {

                // This handler will be called for every request
                //response.putHeader("content-type", "application/json");

                // Write to the response and end it
                response.end("{\"test\" : \"Hello World from vertx \"}");
                System.out.println(System.currentTimeMillis() + " Response received");

            }
        });*/

        return httpClientRequest;
    }
}
