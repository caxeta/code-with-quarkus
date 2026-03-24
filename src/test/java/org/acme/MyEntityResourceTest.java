package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class MyEntityResourceTest {

    @Test
    void testCreateEntityWithXSSPayload() {
        String xssPayload = "{\"field\": \"<script>alert(1)</script>\"}";

        given()
          .contentType("application/json")
          .body(xssPayload)
          .when().post("/my-entity")
          .then()
             .log().all()
             .statusCode(400);
    }

    @Test
    void testCreateEntityWithLargePayload() {
        String largeString = "a".repeat(300);
        String jsonPayload = "{\"field\": \"" + largeString + "\"}";

        given()
          .contentType("application/json")
          .body(jsonPayload)
          .when().post("/my-entity")
          .then()
             .log().all()
             .statusCode(400);
    }
}
