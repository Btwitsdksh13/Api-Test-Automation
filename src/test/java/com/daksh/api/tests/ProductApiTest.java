package com.daksh.api.tests;

import com.daksh.api.base.BaseTest;
import com.daksh.api.utils.ProductPayload;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

public class ProductApiTest extends BaseTest {

    @Test(groups = "smoke", description = "GET /products returns a non-empty list")
    public void getAllProductsReturnsList() {
        given().spec(requestSpec)
        .when().get("/products")
        .then().spec(okSpec)
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("id", everyItem(notNullValue()));
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

    @Test(description = "Limit query param is respected")
    public void limitQueryParamIsRespected() {
        given().spec(requestSpec).queryParam("limit", 3)
        .when().get("/products")
        .then().statusCode(200)
                .body("size()", equalTo(3));
    }

    @Test(description = "Sort query param orders products by id descending")
    public void sortDescendingOrdersById() {
        given().spec(requestSpec).queryParam("sort", "desc")
        .when().get("/products")
        .then().statusCode(200)
                .body("id[0]", greaterThan(1))
                .body("id[0]", greaterThan(
                        given().spec(requestSpec).queryParam("sort", "asc")
                                .get("/products").path("id[0]")));
    }

    @DataProvider(name = "categories")
    public Object[][] categories() {
        return new Object[][] {
                {"electronics"}, {"jewelery"}, {"men's clothing"}, {"women's clothing"}
        };
    }

    @Test(dataProvider = "categories", description = "Every product in a category belongs to it")
    public void productsFilterByCategory(String category) {
        given().spec(requestSpec).pathParam("category", category)
        .when().get("/products/category/{category}")
        .then().statusCode(200)
                .body("size()", greaterThan(0))
                .body("category", everyItem(equalTo(category)));
    }

    @Test(groups = "regression", description = "POST /products creates a product and echoes the payload")
    public void createProduct() {
        given().spec(requestSpec).body(ProductPayload.valid())
        .when().post("/products")
        .then().spec(okSpec)
                .statusCode(anyOf(is(200), is(201)))
                .body("id", notNullValue())
                .body("title", equalTo("Test Product"))
                .body("price", equalTo(13.5f))
                .body("category", equalTo("electronics"));
    }

    @Test(groups = "regression", description = "PUT /products/{id} returns the updated product")
    public void updateProduct() {
        given().spec(requestSpec).body(ProductPayload.valid()).pathParam("id", 7)
        .when().put("/products/{id}")
        .then().statusCode(200)
                .body("title", equalTo("Test Product"));
    }

    @Test(groups = "regression", description = "DELETE /products/{id} returns the deleted product")
    public void deleteProduct() {
        given().spec(requestSpec).pathParam("id", 6)
        .when().delete("/products/{id}")
        .then().statusCode(200)
                .body("id", equalTo(6));
    }

    @Test(groups = "negative", description = "A product id that does not exist must not return a product")
    public void getNonExistentProductReturnsNoProduct() {
        // FakeStore returns 200 with an empty body for unknown ids; assert there is no product payload.
        given().spec(requestSpec)
        .when().get("/products/99999")
        .then().statusCode(anyOf(is(200), is(404)))
                .body(anyOf(emptyOrNullString(), equalTo("null")));
    }
}
