package com.daksh.api.tests;

import com.daksh.api.base.BaseTest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CartApiTest extends BaseTest {

    @Test(groups = "smoke", description = "A cart exposes its user and product lines")
    public void getCartByIdReturnsProducts() {
        given().spec(requestSpec)
        .when().get("/carts/1")
        .then().spec(okSpec)
                .statusCode(200)
                .body("id", equalTo(1))
                .body("userId", notNullValue())
                .body("products", not(empty()))
                .body("products.productId", everyItem(notNullValue()))
                .body("products.quantity", everyItem(greaterThan(0)));
    }

    @Test(description = "Carts can be filtered by user")
    public void getCartsByUser() {
        given().spec(requestSpec)
        .when().get("/carts/user/2")
        .then().statusCode(200)
                .body("userId", everyItem(equalTo(2)));
    }
}
