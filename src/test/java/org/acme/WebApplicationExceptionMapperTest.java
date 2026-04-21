package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

@QuarkusTest
public class WebApplicationExceptionMapperTest {

    @Test
    public void testMethodNotAllowed() {
        given()
          .when().post("/hello")
          .then()
             .statusCode(405)
             .body(not(containsString("RESTEASY")))
             .log().all();
    }
}
