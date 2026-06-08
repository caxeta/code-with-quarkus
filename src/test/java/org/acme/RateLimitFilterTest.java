package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class RateLimitFilterTest {

    @Test
    void testRateLimitEnforcement() {
        String testIp = "192.168.1.100";
        String endpoint = "/hello";

        // 1. Send MAX_REQUESTS (100)
        for (int i = 0; i < 100; i++) {
            given()
                .header("X-Forwarded-For", testIp)
                .when().get(endpoint)
                .then()
                .statusCode(200);
        }

        // 2. Send the 101st request, which should be blocked
        given()
            .header("X-Forwarded-For", testIp)
            .when().get(endpoint)
            .then()
            .statusCode(429);
    }
}
