package com.daksh.api.tests;

import com.daksh.api.base.BaseTest;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthApiTest extends BaseTest {

    // Public demo credentials documented by the FakeStore API.
    private static final String USERNAME = "mor_2314";
    private static final String PASSWORD = "83r5^_";

    @Test(groups = "smoke", description = "Valid credentials return a token")
    public void loginWithValidCredentialsReturnsToken() {
        given().spec(requestSpec)
                .body(Map.of("username", USERNAME, "password", PASSWORD))
        .when().post("/auth/login")
        .then().spec(okSpec)
                .statusCode(anyOf(is(200), is(201)))
                .body("token", not(emptyOrNullString()));
    }

    @Test(groups = "negative", description = "Wrong password is rejected and no token is issued")
    public void loginWithWrongPasswordIsRejected() {
        given().spec(requestSpec)
                .body(Map.of("username", USERNAME, "password", "wrong-password"))
        .when().post("/auth/login")
        .then().statusCode(anyOf(is(400), is(401)))
                // The API returns a plain-text error, so a token must not appear anywhere in the body.
                .body(not(containsString("token")))
                .body(containsStringIgnoringCase("incorrect"));
    }

    @Test(groups = "negative", description = "Missing credentials are rejected")
    public void loginWithEmptyBodyIsRejected() {
        given().spec(requestSpec).body(Map.of())
        .when().post("/auth/login")
        .then().statusCode(anyOf(is(400), is(401)));
    }
}
