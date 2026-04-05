## 2024-05-24 - Missing Security Headers in Quarkus App
**Vulnerability:** The Quarkus application was missing standard security headers (X-Content-Type-Options, X-XSS-Protection, X-Frame-Options, Strict-Transport-Security), which left it vulnerable to various attacks like MIME sniffing, clickjacking, and XSS.
**Learning:** In a Quarkus application, security headers aren't added by default. You need to explicitly configure them, usually in `src/main/resources/application.properties`.
**Prevention:** Always add standard security headers via `quarkus.http.header...` configuration when bootstrapping a new Quarkus web application to ensure baseline security.

## 2024-05-24 - Missing CSP Header and Entity Length Limits
**Vulnerability:** The application lacked a Content-Security-Policy (CSP) header, increasing the risk of XSS attacks. Additionally, the JPA entity `MyEntity` lacked explicit length limits on its String fields, potentially allowing excessively large payloads to be sent and stored, which is a minor DoS and storage exhaustion risk.
**Learning:** In Quarkus, while some security headers can be configured, a strong CSP must be explicitly defined in `application.properties`. For Panache entities, relying on default String lengths (usually 255) in the database without explicit code-level constraints can mask potential payload size issues.
**Prevention:** Always configure a restrictive `Content-Security-Policy` header by default. Explicitly define `@Column(length = ...)` on entity String fields to enforce length constraints at the schema level, and consider adding validation annotations (`@Size` or `@Length`) for earlier rejection of oversized payloads.

## 2026-03-26 - Potential Stored XSS via Panache REST Data endpoints
**Vulnerability:** The Panache entity automatically exposed a REST CRUD endpoint without input character sanitization on its string fields. This allowed potentially malicious payloads containing `<script>` tags or HTML entities to be stored and potentially executed when viewed by users.
**Learning:** `quarkus-hibernate-orm-rest-data-panache` makes building APIs easy, but it bypasses manual controller-layer validation since the endpoints are automatically generated. Relying only on front-end validation or generic size constraints is insufficient.
**Prevention:** Always apply specific Jakarta Validation constraints, like `@Pattern(regexp = "^[^<>]*$")`, directly to Panache Entity fields to ensure defense-in-depth and prevent Stored XSS before data is persisted via auto-generated endpoints.
## 2026-03-27 - Information Exposure in RESTEasy via JSONB Deserialization
**Vulnerability:** Sending malformed JSON payloads (like trailing commas) to RESTEasy endpoints caused a `ProcessingException` (wrapping a `JsonbException`) which leaked internal Java stack traces, class names, and JSONB parsing implementation details to the client in an HTML 400 Bad Request response. This is an Information Exposure (CWE-200) risk.
**Learning:** RESTEasy's default error handling for payload parsing failures is verbose and leaks server internals, which violates secure error handling principles.
**Prevention:** Always register custom `ExceptionMapper` implementations for framework-level exceptions like `ProcessingException` and `JsonbException` to intercept them and return generic, sanitized error messages (e.g., "Malformed payload") instead of leaking the underlying exceptions.

## 2026-03-28 - Secure Validation Exception Handling
**Vulnerability:** Information leakage via default `ConstraintViolationException` handling.
**Learning:** In Quarkus RESTEasy, default validation constraint violations throw a `ConstraintViolationException` which inadvertently leaks internal Java parameter paths (e.g. `add.entity.field`), the constraint type, and echoes the exact unsanitized user input in the HTTP response body.
**Prevention:** Register a custom `ExceptionMapper<ConstraintViolationException>` to catch these validation failures and return a sanitized, generic 400 Bad Request error to prevent leaking schema details or echoing malicious payloads.

## 2026-03-31 - Information Exposure in RESTEasy via Unhandled Exceptions
**Vulnerability:** The application leaked stack traces when an unhandled `java.lang.Exception` or `java.lang.RuntimeException` occurred. This is an Information Exposure (CWE-200) risk.
**Learning:** RESTEasy's default error handling for unhandled exceptions is verbose and leaks server internals, violating secure error handling principles.
**Prevention:** Always register a custom `ExceptionMapper<Exception>` to intercept generic unhandled exceptions and return generic, sanitized error messages (e.g., "Internal Server Error") instead of leaking underlying exceptions.

## 2026-04-01 - Missing Cache-Control Headers for API Responses
**Vulnerability:** API endpoints didn't define default `Cache-Control` headers. This could lead to sensitive data (such as API responses containing user information or validation errors) being stored in browser caches, proxies, or intermediate nodes, increasing the risk of unauthorized access.
**Learning:** In Quarkus RESTEasy, responses don't include cache prevention headers by default unless explicitly configured.
**Prevention:** Always implement a `ContainerResponseFilter` to add `Cache-Control: no-store, no-cache, must-revalidate, max-age=0`, `Pragma: no-cache`, and `Expires: 0` headers to all responses by default. This ensures that only explicitly annotated endpoints (e.g., using `@Cache(maxAge = ...)`) are cached.

## 2026-04-02 - Information Exposure and Incorrect Error Handling for Not Found Resources
**Vulnerability:** A `GlobalExceptionMapper` catching generic `java.lang.Exception` was inadvertently intercepting `jakarta.ws.rs.NotFoundException` (which extends `RuntimeException`). This caused invalid URLs to return a `500 Internal Server Error` instead of a `404 Not Found`, while additionally filling logs with false-positive stack traces for simple non-existent paths.
**Learning:** In Quarkus RESTEasy, global exception mappers can easily shadow standard HTTP error semantics if not properly prioritized or if specific framework exceptions are not handled separately.
**Prevention:** Always register specific `ExceptionMapper` implementations (like `ExceptionMapper<NotFoundException>`) for standard framework exceptions like `NotFoundException` to intercept them and return the appropriate HTTP status codes and sanitized responses before a catch-all mapper is invoked.

## 2026-04-05 - WebApplicationException intercepted by GlobalExceptionMapper
**Vulnerability:** A generic `GlobalExceptionMapper<Exception>` was inadvertently intercepting standard JAX-RS/RESTEasy exceptions like `WebApplicationException` (e.g., 405 Method Not Allowed), returning a 500 Internal Server Error instead, and stripping critical HTTP headers such as `Allow`.
**Learning:** In Quarkus RESTEasy, a global exception mapper for `java.lang.Exception` catches everything not explicitly mapped. When it intercepts a `WebApplicationException` that has an associated response (like 405 Method Not Allowed), it creates a completely new response, effectively stripping required protocol headers.
**Prevention:** Register a specific `ExceptionMapper<WebApplicationException>` that rebuilds the error using `Response.fromResponse(exception.getResponse())`. This approach ensures that important semantic HTTP headers like `Allow` are preserved, while allowing custom formatting (e.g. JSON error payload) to prevent internal details from leaking.
