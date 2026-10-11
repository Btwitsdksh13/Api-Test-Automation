package com.daksh.api.tests;

import com.daksh.api.base.BaseTest;
import com.daksh.api.utils.ProductPayload;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ProductApiTest extends BaseTest {

    @Test(groups = "smoke", description = "GET /products returns a page of products with a total")
    public void getAllProductsReturnsList() {
        given().spec(requestSpec)
        .when().get("/products")
        .then().spec(okSpec)
                .statusCode(200)
                .body("products.size()", greaterThan(0))
                .body("total", greaterThan(0))
                .body("products.id", everyItem(notNullValue()));
    }

    @Test(groups = "smoke", description = "GET /products/{id} matches the product contract")
    public void getProductByIdMatchesSchema() {
        given().spec(requestSpec)
        .when().get("/products/1")
        .then().spec(okSpec)
                .statusCode(200)
                .body("id", equalTo(1))
                .body(matchesJsonSchemaInClasspath("schemas/product.json"));
    }

    @Test(description = "limit query param is respected")
    public void limitQueryParamIsRespected() {
        given().spec(requestSpec).queryParam("limit", 3)
        .when().get("/products")
        .then().statusCode(200)
                .body("products.size()", equalTo(3))
                .body("limit", equalTo(3));
    }

    @Test(description = "skip and limit page through the catalogue without overlap")
    public void paginationReturnsDifferentPages() {
        List<Integer> first = given().spec(requestSpec).queryParam("limit", 5).queryParam("skip", 0)
                .get("/products").path("products.id");
        List<Integer> second = given().spec(requestSpec).queryParam("limit", 5).queryParam("skip", 5)
                .get("/products").path("products.id");

        assertThat(first, hasSize(5));
        assertThat(second, hasSize(5));
        assertThat("pages must not overlap", second, everyItem(not(in(first))));
    }

    @Test(description = "sortBy=price&order=desc returns prices in descending order")
    public void sortByPriceDescending() {
        List<Float> prices = given().spec(requestSpec)
                .queryParam("sortBy", "price").queryParam("order", "desc").queryParam("limit", 10)
                .get("/products").path("products.price");

        for (int i = 1; i < prices.size(); i++) {
            assertThat("prices must not increase", prices.get(i), lessThanOrEqualTo(prices.get(i - 1)));
        }
    }

    @Test(description = "search returns products that match the query")
    public void searchReturnsMatches() {
        given().spec(requestSpec).queryParam("q", "phone")
        .when().get("/products/search")
        .then().statusCode(200)
                .body("products.size()", greaterThan(0))
                .body("total", greaterThan(0));
    }

    @DataProvider(name = "categories")
    public Object[][] categories() {
        return new Object[][] {{"smartphones"}, {"laptops"}, {"groceries"}, {"fragrances"}};
    }

    @Test(dataProvider = "categories", description = "Every product in a category belongs to it")
    public void productsFilterByCategory(String category) {
        given().spec(requestSpec).pathParam("category", category)
        .when().get("/products/category/{category}")
        .then().statusCode(200)
                .body("products.size()", greaterThan(0))
                .body("products.category", everyItem(equalTo(category)));
    }

    @Test(groups = "regression", description = "POST /products/add creates a product (201) and echoes the payload")
    public void createProduct() {
        given().spec(requestSpec).body(ProductPayload.valid())
        .when().post("/products/add")
        .then().spec(okSpec)
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo("Test Product"));
    }

    @Test(groups = "regression", description = "PUT /products/{id} returns the updated title")
    public void updateProduct() {
        given().spec(requestSpec).body(ProductPayload.valid()).pathParam("id", 1)
        .when().put("/products/{id}")
        .then().statusCode(200)
                .body("id", equalTo(1))
                .body("title", equalTo("Test Product"));
    }

    @Test(groups = "regression", description = "DELETE /products/{id} marks the product as deleted")
    public void deleteProduct() {
        given().spec(requestSpec).pathParam("id", 1)
        .when().delete("/products/{id}")
        .then().statusCode(200)
                .body("id", equalTo(1))
                .body("isDeleted", equalTo(true))
                .body("deletedOn", not(emptyOrNullString()));
    }

    @Test(groups = "negative", description = "Unknown product id returns 404 with a message")
    public void getNonExistentProductReturns404() {
        Response response = given().spec(requestSpec)
                .when().get("/products/99999")
                .then().statusCode(404)
                .extract().response();

        assertThat(response.path("message"), containsString("99999"));
    }
}
