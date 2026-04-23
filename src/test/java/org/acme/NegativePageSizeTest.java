package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class NegativePageSizeTest {
    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    public void testNegativeSize() {
        given()
          .when().get("/my-entity?size=-1")
          .then()
             .statusCode(400); // We expect 400 Bad Request, not 500
    }
}
