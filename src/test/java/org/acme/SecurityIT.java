package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class SecurityIT {

    @Test
    public void testNotFound() {
        given()
          .when().get("/does-not-exist")
          .then()
             .statusCode(404);
    }
}
