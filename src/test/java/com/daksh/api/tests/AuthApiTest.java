package com.daksh.api.tests;

import com.daksh.api.base.BaseTest;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthApiTest extends BaseTest {

    // Public demo credentials documented by DummyJSON.
    private static final String USERNAME = "emilys";
    private static final String PASSWORD = "emilyspass";

    private String login() {
        return given().spec(requestSpec)
                .body(Map.of("username", USERNAME, "password", PASSWORD))
                .post("/auth/login")
                .then().statusCode(200)
                .extract().path("accessToken");
    }

    @Test(groups = "smoke", description = "Valid credentials return an access token")
    public void loginWithValidCredentialsReturnsToken() {
        given().spec(requestSpec)
                .body(Map.of("username", USERNAME, "password", PASSWORD))
        .when().post("/auth/login")
        .then().spec(okSpec)
                .statusCode(200)
                .body("accessToken", not(emptyOrNullString()))
                .body("username", equalTo(USERNAME));
    }

    @Test(groups = "negative", description = "Wrong password is rejected with 400 and no token")
    public void loginWithWrongPasswordIsRejected() {
        given().spec(requestSpec)
                .body(Map.of("username", USERNAME, "password", "wrong-password"))
        .when().post("/auth/login")
        .then().statusCode(400)
                .body("message", equalTo("Invalid credentials"))
                .body("accessToken", nullValue());
    }

    @Test(groups = "regression", description = "The token authorises access to the profile endpoint")
    public void tokenAllowsAccessToProfile() {
        given().spec(requestSpec).header("Authorization", "Bearer " + login())
        .when().get("/auth/me")
        .then().statusCode(200)
                .body("username", equalTo(USERNAME));
    }

    @Test(groups = "negative", description = "Profile endpoint without a token returns 401")
    public void profileWithoutTokenIsUnauthorized() {
        given().spec(requestSpec)
        .when().get("/auth/me")
        .then().statusCode(401)
                .body("message", not(emptyOrNullString()));
    }
}
