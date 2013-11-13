/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.utils;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.model.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.model.iOSVariant;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static javax.ws.rs.core.Response.Status.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public final class SimplePushVariantUtils {

    private static final int SINGLE = 1;

    private SimplePushVariantUtils() {
    }

    public static SimplePushVariant create(String name, String description) {
        SimplePushVariant simplePushVariant = new SimplePushVariant();

        simplePushVariant.setName(name);
        simplePushVariant.setDescription(description);

        return simplePushVariant;
    }

    public static SimplePushVariant createAndRegister(String name, String description, PushApplication pushApplication,
                                                      AuthenticationUtils.Session session) {
        SimplePushVariant simplePushVariant = create(name, description);

        register(simplePushVariant, pushApplication, session);

        return simplePushVariant;
    }

    public static SimplePushVariant generate() {
        return generate(SINGLE).iterator().next();
    }

    public static List<SimplePushVariant> generate(int count) {
        List<SimplePushVariant> simplePushVariants = new ArrayList<SimplePushVariant>();

        for (int i = 0; i < count; i++) {
            String name = UUID.randomUUID().toString();
            String description = UUID.randomUUID().toString();

            SimplePushVariant simplePushVariant = create(name, description);

            simplePushVariants.add(simplePushVariant);
        }

        return simplePushVariants;
    }

    public static SimplePushVariant generateAndRegister(PushApplication pushApplication,
                                                        AuthenticationUtils.Session session) {
        return generateAndRegister(SINGLE, pushApplication, session).iterator().next();
    }

    public static List<SimplePushVariant> generateAndRegister(int count, PushApplication pushApplication,
                                                              AuthenticationUtils.Session session) {
        List<SimplePushVariant> simplePushVariants = generate(count);

        for (SimplePushVariant simplePushVariant : simplePushVariants) {
            register(simplePushVariant, pushApplication, session);
        }

        return simplePushVariants;
    }

    public static void register(SimplePushVariant simplePushVariant, PushApplication pushApplication,
                                AuthenticationUtils.Session session) {
        register(simplePushVariant, pushApplication, session, ContentTypes.json());
    }

    public static void register(SimplePushVariant simplePushVariant, PushApplication pushApplication,
                                AuthenticationUtils.Session session, String contentType) {

        Response response = RestAssured.given()
                .contentType(contentType)
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .body(toJSONString(simplePushVariant))
                .post("{root}rest/applications/{pushApplicationID}/simplePush", session.getRoot(),
                        pushApplication.getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, CREATED);

        setFromJsonPath(response.jsonPath(), simplePushVariant);
    }

    public static List<SimplePushVariant> listAll(PushApplication pushApplication,
                                                  AuthenticationUtils.Session session) {
        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .get("{root}rest/applications/{pushApplicationID}/simplePush", session.getRoot(),
                        pushApplication.getPushApplicationID());

        UnexpectedResponseException.verifyResponse(response, OK);

        List<SimplePushVariant> simplePushVariants = new ArrayList<SimplePushVariant>();

        JsonPath jsonPath = response.jsonPath();

        List<Map<String, ?>> items = jsonPath.getList("");

        for (int i = 0; i < items.size(); i++) {
            jsonPath.setRoot("[" + i + "]");

            SimplePushVariant simplePushVariant = fromJsonPath(jsonPath);

            simplePushVariants.add(simplePushVariant);
        }

        return simplePushVariants;
    }

    public static SimplePushVariant findById(String variantID, PushApplication pushApplication,
                                             AuthenticationUtils.Session session) {
        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .get("{root}rest/applications/{pushApplicationID}/android/{variantID}", session.getRoot(),
                        pushApplication.getPushApplicationID(), variantID);

        UnexpectedResponseException.verifyResponse(response, OK);

        return fromJsonPath(response.jsonPath());
    }

    public static void update(SimplePushVariant simplePushVariant, PushApplication pushApplication,
                              AuthenticationUtils.Session session) {
        update(simplePushVariant, pushApplication, session, ContentTypes.json());
    }

    public static void update(SimplePushVariant simplePushVariant, PushApplication pushApplication,
                              AuthenticationUtils.Session session, String contentType) {
        assertNotNull(session);

        Response response = RestAssured.given()
                .contentType(contentType)
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .body(toJSONString(simplePushVariant))
                .put("{root}rest/applications/{pushApplicationID}/simplePush/{variantID}", session.getRoot(),
                        pushApplication.getPushApplicationID(), simplePushVariant.getVariantID());

        UnexpectedResponseException.verifyResponse(response, NO_CONTENT);
    }

    public static void delete(SimplePushVariant simplePushVariant, PushApplication pushApplication,
                              AuthenticationUtils.Session session) {
        assertNotNull(session);

        Response response = RestAssured.given()
                .contentType(ContentTypes.json())
                .header(Headers.acceptJson())
                .cookies(session.getCookies())
                .delete("{root}rest/applications/{pushApplicationID}/simplePush/{variantID}", session.getRoot(),
                        pushApplication.getPushApplicationID(), simplePushVariant.getVariantID());

        UnexpectedResponseException.verifyResponse(response, NO_CONTENT);
    }

    public static JSONObject toJSONObject(SimplePushVariant simplePushVariant) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("name", simplePushVariant.getName());
        jsonObject.put("description", simplePushVariant.getDescription());

        return jsonObject;
    }

    public static String toJSONString(SimplePushVariant simplePushVariant) {
        return toJSONObject(simplePushVariant).toJSONString();
    }

    public static SimplePushVariant fromJsonPath(JsonPath jsonPath) {
        SimplePushVariant simplePushVariant = new SimplePushVariant();

        setFromJsonPath(jsonPath, simplePushVariant);

        return simplePushVariant;
    }

    public static void setFromJsonPath(JsonPath jsonPath, SimplePushVariant simplePushVariant) {
        simplePushVariant.setId(jsonPath.getString("id"));
        simplePushVariant.setVariantID(jsonPath.getString("variantID"));
        simplePushVariant.setDeveloper(jsonPath.getString("developer"));
        simplePushVariant.setDescription(jsonPath.getString("description"));
        simplePushVariant.setName(jsonPath.getString("name"));
        simplePushVariant.setSecret(jsonPath.getString("secret"));
    }

    public static SimplePushVariant createSimplePushVariant(String name, String description, String variantID,
                                                            String secret,
                                                            String developer) {
        SimplePushVariant variant = new SimplePushVariant();
        variant.setName(name);
        variant.setDescription(description);
        variant.setVariantID(variantID);
        variant.setSecret(secret);
        variant.setDeveloper(developer);
        return variant;
    }

    public static void checkEquality(SimplePushVariant expected, SimplePushVariant actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getVariantID(), actual.getVariantID());
        assertEquals(expected.getSecret(), actual.getSecret());
        assertEquals(expected.getDeveloper(), actual.getDeveloper());
    }

    @SuppressWarnings("unchecked")
    public static Response registerSimplePushVariant(String pushAppId, SimplePushVariant variant, Map<String,
            ?> cookies,
                                                     String root) {

        assertNotNull(root);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", variant.getName());
        jsonObject.put("description", variant.getDescription());

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).body(jsonObject.toString())
                .post("{root}rest/applications/{pushAppId}/simplePush", root, pushAppId);

        return response;
    }

    @SuppressWarnings("unchecked")
    public static Response updateSimplePushVariant(String pushAppId, SimplePushVariant variant, Map<String, ?> cookies,
                                                   String variantId, String root) {

        assertNotNull(root);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", variant.getName());
        jsonObject.put("description", variant.getDescription());

        Response response = RestAssured.given().contentType("application/json").header("Accept", "application/json")
                .cookies(cookies).body(jsonObject.toString())
                .put("{root}rest/applications/{pushAppId}/simplePush/{variantId}", root, pushAppId, variantId);

        return response;
    }

    public static Response listAllSimplePushVariants(String pushAppId, Map<String, ?> cookies, String root) {
        assertNotNull(root);

        Response response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(cookies)
                .get("{root}rest/applications/{pushAppId}/simplePush", root, pushAppId);

        return response;
    }

    public static Response findSimplePushVariantById(String pushAppId, String variantId, Map<String, ?> cookies,
                                                     String root) {
        assertNotNull(root);

        Response response = RestAssured.given()
                .contentType("application/json")
                .header("Accept", "application/json")
                .cookies(cookies)
                .get("{root}rest/applications/{pushAppId}/simplePush/{variantId}", root, pushAppId, variantId);

        return response;
    }

    public static Response deleteSimplePushVariant(String pushAppId, String variantId, Map<String, ?> cookies,
                                                   String root) {
        assertNotNull(root);

        Response response = RestAssured.given()
                .cookies(cookies)
                .delete("{root}rest/applications/{pushAppId}/simplePush/{variantId}", root, pushAppId, variantId);

        return response;
    }
}
