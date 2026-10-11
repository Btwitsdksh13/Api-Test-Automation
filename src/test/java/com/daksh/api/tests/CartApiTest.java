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
                .body("products.id", everyItem(notNullValue()))
                .body("products.quantity", everyItem(greaterThan(0)));
    }

    @Test(description = "Cart totals are consistent with the number of product lines")
    public void cartTotalsAreConsistent() {
        given().spec(requestSpec)
        .when().get("/carts/1")
        .then().statusCode(200)
                .body("totalProducts", equalTo(4))
                .body("total", greaterThan(0f))
                .body("discountedTotal", lessThanOrEqualTo(
                        given().spec(requestSpec).get("/carts/1").path("total")));
    }

    @Test(description = "Carts can be filtered by user")
    public void getCartsByUser() {
        given().spec(requestSpec)
        .when().get("/carts/user/6")
        .then().statusCode(200)
                .body("carts.userId", everyItem(equalTo(6)));
    }
}
