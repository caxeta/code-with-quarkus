# Task: Fix Log Injection in RateLimitFilter

## Objective
Fix a Log Injection (CWE-117) vulnerability in `RateLimitFilter` where unsanitized IP addresses, which can be spoofed via `X-Forwarded-For` when `quarkus.http.proxy.proxy-address-forwarding=true` is enabled, are directly logged.

## Changes Made
- Modified `src/main/java/org/acme/security/RateLimitFilter.java` to explicitly strip newline characters (`[\r\n]`) from `clientIp` before passing it to `LOG.warn()`.

## Tests
- Confirmed that `RateLimitFilter` continues to compile and execute properly by running `./mvnw clean test`.

## QA / Homologation Guide
1. Run the application behind a reverse proxy (or ensure proxy address forwarding is enabled).
2. Send requests to the application with an `X-Forwarded-For` header containing newline characters followed by spoofed log entry text (e.g., `X-Forwarded-For: 127.0.0.1\n[FAKE LOG ENTRY]`).
3. Trigger the rate limit.
4. Verify in the logs that the newline characters have been stripped and the fake log entry is not on a new line, but concatenated with the rest of the warning.
