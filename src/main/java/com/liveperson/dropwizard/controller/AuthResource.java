package com.liveperson.dropwizard.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.liveperson.jwt.JwtUtil;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.codec.binary.Base64;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;

/**
 * Created by rois on 6/26/16.
 */
@Path("/testDW")
public class AuthResource {

    private final Client client;
    private final String endpoint;
    private final Gson gson = new Gson();

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

    public AuthResource(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
    }

    @GET
    @Path("/authenticateAsync")
    @Produces({MediaType.APPLICATION_JSON})
    public void authenticate(@Suspended final AsyncResponse asyncResponse) throws WebApplicationException {

        WebTarget target = client.target(endpoint);

        //get response Async
        InvocationCallback<Response> callback = new InvocationCallback<Response>() {
            @Override
            public void completed(Response res) {
                if (res != null && res.getEntity() != null) {
                    String jsonRes = res.readEntity(String.class);
                    JsonElement element = gson.fromJson(jsonRes, JsonElement.class);
                    JsonObject jsonObjRes = element.getAsJsonObject();
                    JsonElement jwtElement = jsonObjRes.get("jwt");
                    Boolean isVerified =false;
                    if (jwtElement != null) {
                        isVerified = JwtUtil.isVerifiedJwt(jwtElement.getAsString());
                        asyncResponse.resume("{\"valid\" : \"" + isVerified+"\"}");
                    } else {
                        asyncResponse.resume("{\"valid\" : \"false\"}");
                        System.err.println("couldn't find jwt element in ext jwt response. res = " + jsonRes);
                    }
                }
            }


            @Override
            public void failed(Throwable t) {
                t.printStackTrace();
            }
        };

        target.request(MediaType.APPLICATION_JSON_TYPE).async()
                .get(callback);

    }

    @GET
    @Path("/parsedJwtDW")
    public String parsedJwtDw() throws WebApplicationException {

        final String res= "{\"jwt\" : \"eyJraWQiOiIwMDAwMSIsInR5cCI6IkpXVCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiIxMDMyZjA3YTI0ZTUxYTE3ZmE2NDhhMDgyMDA2YzgxZjJjZmFmM2Y0ZTUyZmFmY2FiNDc0ZjQ3MDhkYjhmMTQ3IiwiYXVkIjoiYWNjOnFhMzY4MDQ2MjEiLCJpc3MiOiJodHRwczpcL1wvaWRwLmxpdmVwZXJzb24ubmV0IiwibHAuZXh0Ijp7InN1YiI6ImxwVGVzdHJvaW92ZXJyaWRlRXhwaXJlQ29kZUF0LTE3OTcwNjY2MTEwMDAiLCJpc3MiOiJodHRwczpcL1wvY3VzdG9tZXJXZWJTaXRlLmNvbSIsImxwX3NkZXMiOiJhMzVhM2M3NThmZmEwODRhNjkxMTg1M2RiODEyNmIxOTk1ZTA3NDExOWQ4NDkxYjk0ZjQ3YWNkOWVlM2IyNDBjOWM1MjZjNzk1ZmNjNTcxOTI5ZThmNTkzNWE2YmEwZTllZTE5YWU4NDkyMmY3MTlmZDU3NjVkYWZiNzc2NGFlZTIwMmQ5Mzg4MjQ5M2Q5NzA2MjA4OWI2MGM4ZDYzNzQ3MzZmMTg0ZTc5Zjk2ZjU4MDYxNjBhYWU5NjQ0ZWU1YzQ2OTYzMDNiMzJjMDY4NjU4YjBjN2E4YzA2YmY0YjJhNzJlNTc1ODRkZWJhOTBhNDg3MGM1NTAyM2I3MjI0NmJlOWJiOWUxMzc4ZjMyNzVjMDZhMTUwMDg5Mjg3MTkyZmNlZGE3MTA5OWI3ZTFhZjNhNjQ5YjRhYTVmMmRhMjQ3M2FhNjdiMGFmYmU5M2Q3ZmNmNGQzMWE1MzE4MTY0MDc2M2I4ZTBkNTVhMTk0NGQ2ZmExZGYzYmEzYzA2OTYyOGY2ZDAyOGU2MzU2OTIxYmE5ZmUwMmUzMmRjZTI1MmYzMyJ9LCJleHAiOjE3OTcwNjY2MTEsImlhdCI6MTQ2NTkxMjM2OX0.q1WQUhCv7bbJu6JsD95KOZOVgmBwcV2n-M-KxVQq-AUyY9bUy8zytb_E_V_ddXV3U6TgUj_PfydPVXEqt3MLhjBlmfmVyknEx_llqaCUnSymtQAcXcn1vso2KB5oBHD8YCmccbc8Cn1pNfTEay4rBpF7hhWG_tT4UqqRs64qoHU\"}";
        return res;
    }

}
