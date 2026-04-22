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

## 2026-04-06 - WebApplicationException Mapping
**Vulnerability:** Global `ExceptionMapper<Exception>` was inadvertently intercepting standard HTTP exceptions (like `WebApplicationException` for 405 Method Not Allowed) and converting them into 500 Internal Server Errors while stripping out critical headers (like `Allow`).
**Learning:** In Quarkus RESTEasy, standard HTTP error responses built by the framework must be explicitly preserved if a generic catch-all mapper is present. Stripping headers like `Allow` breaks HTTP semantics and can hinder security tooling/scanners that rely on these standards.
**Prevention:** Always implement a specific `ExceptionMapper<WebApplicationException>` when using a generic `ExceptionMapper<Exception>`. Crucially, use `Response.fromResponse(exception.getResponse())` to reconstruct the response, ensuring that all original headers injected by the framework are preserved.
## 2026-04-05 - WebApplicationException intercepted by GlobalExceptionMapper
**Vulnerability:** A generic `GlobalExceptionMapper<Exception>` was inadvertently intercepting standard JAX-RS/RESTEasy exceptions like `WebApplicationException` (e.g., 405 Method Not Allowed), returning a 500 Internal Server Error instead, and stripping critical HTTP headers such as `Allow`.
**Learning:** In Quarkus RESTEasy, a global exception mapper for `java.lang.Exception` catches everything not explicitly mapped. When it intercepts a `WebApplicationException` that has an associated response (like 405 Method Not Allowed), it creates a completely new response, effectively stripping required protocol headers.
**Prevention:** Register a specific `ExceptionMapper<WebApplicationException>` that rebuilds the error using `Response.fromResponse(exception.getResponse())`. This approach ensures that important semantic HTTP headers like `Allow` are preserved, while allowing custom formatting (e.g. JSON error payload) to prevent internal details from leaking.
## 2026-04-07 - Information Exposure and Header Stripping via Global Exception Handling
**Vulnerability:** A `GlobalExceptionMapper` catching `java.lang.Exception` was inadvertently intercepting `jakarta.ws.rs.WebApplicationException` and subclasses (like `NotAllowedException` and `NotAuthorizedException`). This converted correct framework-generated HTTP status codes (like 405 or 401) to generic `500 Internal Server Error` responses, whilst simultaneously stripping critical response headers (like `Allow` or `WWW-Authenticate`) added by RESTEasy, which violates proper secure HTTP semantics.
**Learning:** In Quarkus RESTEasy, global catch-all exception mappers must not inadvertently mask framework exceptions that rely on specific status codes and headers for proper client-server communication and security protocols.
**Prevention:** Always register a specific `ExceptionMapper<WebApplicationException>` to intercept web application exceptions and correctly build the response from the underlying exception object using `Response.fromResponse(exception.getResponse())` to preserve status codes and essential headers.

## 2024-05-25 - Silently Dropped Security Headers due to Duplicate Property Keys
**Vulnerability:** A `Content-Security-Policy` header was misconfigured in `application.properties` because the key `quarkus.http.header."Content-Security-Policy".value` was defined twice. This caused the later definition to completely overwrite the earlier one instead of appending to it, silently dropping critical security directives.
**Learning:** In Quarkus `application.properties`, defining duplicate keys causes the latter to overwrite the former. Multiple directives for the same header (like CSP or Permissions-Policy) must be combined into a single property value string.
**Prevention:** Always consolidate multiple directives for the same HTTP header into a single line in `application.properties` to ensure all intended security policies are actively enforced.
## 2026-04-15 - Information Exposure in RESTEasy via Unhandled Throwable Exceptions
**Vulnerability:** The application handled generic `Exception` instances through `GlobalExceptionMapper`, but unhandled `java.lang.Error` instances (like `OutOfMemoryError` or custom `Error` classes thrown unexpectedly) were still leaking full server stack traces and class details to the client. This is an Information Exposure (CWE-200) risk as an unhandled `Throwable` isn't caught by an `ExceptionMapper<Exception>`.
**Learning:** In Java and Quarkus RESTEasy, `Exception` does not catch all `Throwable` objects. A `java.lang.Error` (which extends `Throwable` directly) bypasses `ExceptionMapper<Exception>` and invokes default framework error handling, which can leak stack traces.
**Prevention:** Change the generic catch-all exception mapper signature from `ExceptionMapper<Exception>` to `ExceptionMapper<Throwable>` to intercept all exceptions AND errors, ensuring complete coverage and returning sanitized "Internal Server Error" messages for any unhandled failure condition.

## 2026-04-17 - DoS via Excessive Pagination Size
**Vulnerability:** The application used `quarkus-hibernate-orm-rest-data-panache` to automatically generate REST endpoints for entities. These endpoints support pagination via the `size` query parameter, but lacked an upper bound. This allowed an attacker to request millions of records in a single API call (e.g., `?size=1000000`), leading to potential memory exhaustion, high CPU usage, and database overload (Denial of Service).
**Learning:** Framework-generated endpoints often prioritize functionality over restrictive security defaults. Features like pagination must be explicitly bounded to prevent resource exhaustion attacks.
**Prevention:** Implement a global `ContainerRequestFilter` to intercept all requests, inspect the `size` query parameter, and return a `400 Bad Request` if it exceeds a safe maximum limit (e.g., 100).

## 2026-04-18 - Missing Rate Limiting on Endpoints
**Vulnerability:** The application was missing rate limiting on its API endpoints, allowing for potential DoS and brute-force attacks via excessive requests from a single IP.
**Learning:** Endpoints generated by Quarkus or specifically defined without external API gateways could be vulnerable to abuse if limits are not explicitly set at the application level.
**Prevention:** Always implement a rate limit filter (e.g., using `ContainerRequestFilter`) to restrict the number of requests per IP over a given time window (e.g., max 100 requests per minute) and abort excessive requests with a `429 Too Many Requests` status code.

## 2026-04-19 - Missing Negative Value Checks in Pagination Limits
**Vulnerability:** A `ContainerRequestFilter` designed to cap maximum page sizes (`MaxPageSizeFilter`) only checked `if (size > MAX_SIZE)`. It failed to validate lower bounds (`size <= 0`), potentially allowing clients to request negative page sizes leading to unexpected behavior.
**Learning:** Security controls based on integer bounds must always check both the upper AND lower limits, as negative inputs are a common avenue for bypasses or application errors.
**Prevention:** Always validate both the lower and upper bounds of integer inputs, especially when used for resource allocation or database pagination.

## 2026-04-21 - Missing Negative Value Checks in Pagination Page Parameter
**Vulnerability:** A `ContainerRequestFilter` designed to cap maximum page sizes (`MaxPageSizeFilter`) validated the `size` query parameter, but failed to validate the `page` query parameter. This allowed clients to request negative page sizes (`?page=-1`), potentially leading to unexpected behavior or unhandled exceptions when querying the database.
**Learning:** Security controls related to pagination and limits must check all relevant query parameters (such as both `size` and `page`). Failure to check lower bounds (like `< 0`) allows negative inputs which can bypass checks or cause backend errors.
**Prevention:** Always validate all pagination parameters, ensuring both `size` and `page` parameters have appropriate minimum limits (e.g., `page >= 0` and `size > 0`).

## 2026-04-22 - System-wide DoS via IP-based Rate Limiting behind Proxy
**Vulnerability:** IP-based rate limiting using `request.remoteAddress().host()` was active without `quarkus.http.proxy.proxy-address-forwarding=true`. When running behind a reverse proxy, all requests appear to originate from the proxy's IP. This means that a few excessive requests from any user could trigger the rate limit, blocking access for all users simultaneously (a system-wide DoS).
**Learning:** IP-based security controls rely on knowing the true origin IP. Frameworks must be explicitly configured to parse `X-Forwarded-For` or similar headers when deployed behind proxies.
**Prevention:** Ensure `quarkus.http.proxy.proxy-address-forwarding=true` is explicitly set in `application.properties` whenever using IP-based controls (like rate limiting) in an environment that may use reverse proxies.
