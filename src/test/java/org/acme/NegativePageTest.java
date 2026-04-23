package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class NegativePageTest {
    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    public void testNegativePage() {
        given()
          .when().get("/my-entity?page=-1")
          .then()
             .statusCode(400); // We expect 400 Bad Request, not 500
    }
}
