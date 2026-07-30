package org.acme.security;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class RateLimitFilterTest {

    @Test
    public void testRateLimiting() {
        // Use a unique IP for test isolation
        String uniqueIp = UUID.randomUUID().toString();

        // 100 allowed requests
        for (int i = 0; i < 100; i++) {
            given()
                .header("X-Forwarded-For", uniqueIp)
                .when().get("/hello")
                .then()
                .statusCode(200);
        }

        // 101st request should be rate limited
        given()
            .header("X-Forwarded-For", uniqueIp)
            .when().get("/hello")
            .then()
            .statusCode(429)
            .body("error", is("Too Many Requests"))
            .header("Retry-After", "60");
    }
}
