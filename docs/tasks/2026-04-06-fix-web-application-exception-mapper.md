# Task: Fix WebApplicationException mapping to preserve HTTP semantics

## Objective
The `GlobalExceptionMapper` was intercepting standard HTTP exceptions (like 401, 403, 405) because they extend `RuntimeException` via `WebApplicationException`. This converted proper HTTP error responses into generic 500 Internal Server Errors and stripped out critical framework-injected headers (like the `Allow` header for 405 responses), breaking HTTP semantics.

## Changes Made
- Created `src/main/java/org/acme/exception/WebApplicationExceptionMapper.java` which implements `ExceptionMapper<WebApplicationException>`.
- Configured the mapper to return `Response.fromResponse(exception.getResponse())` to preserve the original response status and headers.
- Added logic to construct a generic JSON error payload using the standard HTTP reason phrase.
- Created `src/test/java/org/acme/WebApplicationExceptionMapperTest.java` to verify that 405 Method Not Allowed responses are handled correctly.
- Added a new learning to `.jules/sentinel.md` outlining the `WebApplicationException` vulnerability and prevention.

## Tests
- Ran `./mvnw clean test` to ensure the project test suite and the new `WebApplicationExceptionMapperTest` passed.

## QA / Homologation Guide
1. Try calling the `/hello` endpoint with an unsupported HTTP method (e.g., POST).
2. Verify that the response returns a `405 Method Not Allowed` status code.
3. Verify that the response payload contains `{"error": "Method Not Allowed"}`.
