package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class WebApplicationExceptionMapperTest {

    @Test
    public void testMethodNotAllowed() {
        // Try to POST to an endpoint that only accepts GET
        given()
          .when().post("/hello")
          .then()
             .statusCode(405)
             .body("error", is("Method Not Allowed"));
    }
}
