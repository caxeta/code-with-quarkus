package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class NotFoundTest {

    @Test
    public void testNotFound() {
        given()
          .when().get("/nonexistent-endpoint-12345")
          .then()
             .statusCode(404);
    }
}
