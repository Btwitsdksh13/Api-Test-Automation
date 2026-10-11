package com.daksh.api.base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.BeforeClass;

import static org.hamcrest.Matchers.lessThan;

/**
 * Shared setup. The base URL can be overridden per environment:
 *   mvn test -DbaseUrl=https://staging.example.com
 */
public abstract class BaseTest {

    protected static final String BASE_URL =
            System.getProperty("baseUrl", "https://fakestoreapi.com");

    protected RequestSpecification requestSpec;
    protected ResponseSpecification okSpec;

    @BeforeClass(alwaysRun = true)
    public void setUpSpecs() {
        RestAssured.useRelaxedHTTPSValidation();
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.URI)
                .build();

        // Every successful call must also be reasonably fast.
        okSpec = new ResponseSpecBuilder()
                .expectResponseTime(lessThan(5000L))
                .build();
    }
}
