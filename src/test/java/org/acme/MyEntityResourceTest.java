package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class MyEntityResourceTest {

    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
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
    @TestSecurity(user = "admin", roles = {"admin"})
    void testCreateEntityWithLargePayload() {
        String largeString = "a".repeat(300);
        String jsonPayload = "{\"field\": \"" + largeString + "\"}";

        given()
          .contentType("application/json")
          .body(jsonPayload)
          .when().post("/my-entity")
          .then()
             .log().all()
             .statusCode(400)
             .body(containsString("\"error\": \"Validation failed\""));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
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

    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    void testCreateEntityWithMaliciousPayload() {
        String jsonPayload = "{\"field\": \"<script>alert(1)</script>\"}";

        given()
          .contentType("application/json")
          .body(jsonPayload)
          .when().post("/my-entity")
          .then()
             .log().all()
             .statusCode(400);
    }
}
