package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class MyEntityResourceTest {

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

    @Test
    void testCreateEntityWithMalformedPayloadDoesNotLeakInternals() {
        String jsonPayload = "{\"field\": \"test\",}";

        given()
          .contentType("application/json")
          .body(jsonPayload)
          .when().post("/my-entity")
          .then()
             .log().all()
             .statusCode(400)
             .body(containsString("\"error\": \"Malformed payload\""));
    }
}
