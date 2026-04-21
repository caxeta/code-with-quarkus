package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class NegativePageTest {
    @Test
    public void testNegativePage() {
        given()
          .when().get("/my-entity?page=-1")
          .then()
             .statusCode(400);
    }
}
