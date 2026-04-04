package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class SecurityIT {

    @Test
    public void testNotFound() {
        given()
          .when().get("/does-not-exist")
          .then()
             .statusCode(404)
             .body(not(containsString("Exception")));
    }

    @Test
    public void testNotAllowed() {
        given()
          .when().post("/hello")
          .then()
             .statusCode(405)
             .body(not(containsString("Exception")));
    }
}
