package org.acme.security;

import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Provider
@Priority(Priorities.AUTHENTICATION - 100) // SECURITY: Execute rate limiting BEFORE authentication to prevent DoS via expensive auth hashing
public class RateLimitFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(RateLimitFilter.class);

    private static final int MAX_REQUESTS = 100;

    // SECURITY: Use a size-bounded cache (Caffeine) instead of an unbounded ConcurrentHashMap to prevent memory exhaustion DoS
    private final Cache<String, AtomicInteger> counts = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    @Context
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String clientIp = request.remoteAddress().host();

        AtomicInteger count = counts.get(clientIp, k -> new AtomicInteger(0));
        int currentCount = count.incrementAndGet();

        if (currentCount > MAX_REQUESTS) {
            // SECURITY: Log abusive IPs for auditing. Prevent Log Injection by stripping newlines.
            LOG.warn("Rate limit exceeded for IP: " + clientIp.replaceAll("[\r\n]", ""));

            // SECURITY: Mitigate DoS and brute-force attacks by rate-limiting requests per IP
            requestContext.abortWith(Response.status(429)
                    .header("Retry-After", "60")
                    .entity("{\"error\": \"Too Many Requests\"}")
                    .type("application/json")
                    .build());
        }
    }
}
