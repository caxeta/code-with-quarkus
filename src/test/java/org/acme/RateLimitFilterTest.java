package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class RateLimitFilterTest {

    @Test
    public void testRateLimiting() {
        // Use a unique IP for this test to avoid interfering with other tests
        // since the RateLimitFilter uses the client IP and is a singleton across tests.
        String uniqueIp = "192.168.1.100";

        // The limit is 100 requests per minute.
        // We will send 100 successful requests.
        for (int i = 0; i < 100; i++) {
            given()
              .header("X-Forwarded-For", uniqueIp)
              .when().get("/hello")
              .then()
                 .statusCode(200);
        }

        // The 101st request should be rate limited and return 429.
        given()
          .header("X-Forwarded-For", uniqueIp)
          .when().get("/hello")
          .then()
             .statusCode(429)
             .body("error", is("Too Many Requests"));
    }
}
