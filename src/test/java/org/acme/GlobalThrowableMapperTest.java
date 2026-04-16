package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GlobalThrowableMapperTest {

    @Path("/test-error")
    public static class ErrorResource {
        @GET
        public String throwError() {
            throw new OutOfMemoryError("Test Error");
        }

        @GET
        @Path("/exception")
        public String throwException() throws Exception {
            throw new Exception("Test Exception");
        }
    }

    @Test
    public void testErrorIsCaught() {
        given()
          .when().get("/test-error")
          .then()
             .statusCode(500)
             .body("error", is("Internal Server Error"));
    }

    @Test
    public void testExceptionIsCaught() {
        given()
          .when().get("/test-error/exception")
          .then()
             .statusCode(500)
             .body("error", is("Internal Server Error"));
    }
}
