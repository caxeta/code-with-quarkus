package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class NegativePageSizeTest {
    @Test
    public void testNegativeSize() {
        given()
          .when().get("/my-entity?size=-1")
          .then()
             .statusCode(400); // We expect 400 Bad Request, not 500
    }
}
