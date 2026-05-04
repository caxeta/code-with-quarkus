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

## 2026-04-23 - Missing Authentication on Auto-Generated Endpoints
**Vulnerability:** The application used `quarkus-hibernate-orm-rest-data-panache` to automatically generate REST CRUD endpoints for `MyEntity`. However, these endpoints were fully exposed without any authentication, allowing anonymous users to create, read, update, or delete entities.
**Learning:** Frameworks that auto-generate endpoints prioritize rapid development and often expose them publicly by default. This creates a severe security risk if the developer assumes the endpoints are secure or internal-only.
**Prevention:** Always explicitly apply role-based access control annotations (e.g., `@Authenticated`, `@RolesAllowed`) to the corresponding `PanacheEntityResource` interfaces to ensure generated endpoints are secured.

## 2026-04-24 - Integer Overflow and DoS Risk in Pagination Parameters
**Vulnerability:** A `ContainerRequestFilter` used `Integer.parseInt` to validate the `page` query parameter and checked `if (page < 0)`. However, it lacked an upper bound. This allowed users to pass values up to `2147483647` or values that cause `NumberFormatException` (which was caught and returned 400). A large valid integer like `2147483647` would cause Quarkus/Panache to throw an unhandled internal error (e.g., `IllegalArgumentException: Page index must be >= 0 : -2147483648` due to integer overflow in `next()` calculations inside Panache) converting the response to a 500 error instead of a 400 Bad Request. Also, a large `page` query causes deep pagination, leading to heavy database processing and Denial of Service.
**Learning:** Checking for negative values on pagination parameters isn't enough. Not capping the upper limit on `page` can lead to integer overflows inside the framework's pagination logic and deep pagination attacks, resulting in DoS and 500 errors.
**Prevention:** Always enforce a reasonable upper bound for both `page` and `size` parameters (e.g., `page > 10000` and `size > 100`) to prevent deep pagination DoS and integer overflow issues.
## 2026-04-26 - Missing Secure-by-Default Configuration for Endpoints
**Vulnerability:** The application did not enforce `quarkus.security.jaxrs.deny-unannotated-endpoints=true`, meaning that any new JAX-RS endpoint added without explicit security annotations would be implicitly public and fully accessible by default.
**Learning:** Frameworks like Quarkus allow developers to enforce a "secure by default" posture. By requiring explicit annotations, the framework prevents accidental exposure of sensitive endpoints.
**Prevention:** Always set `quarkus.security.jaxrs.deny-unannotated-endpoints=true` in `application.properties` and explicitly annotate public endpoints with `@PermitAll` to ensure no endpoint is accidentally left unprotected.

## 2026-04-27 - DoS Vulnerability via Late-Executing Rate Limiting Filter
**Vulnerability:** A rate limiting filter (`RateLimitFilter`) was implemented but lacked explicit `@Priority` configuration. By default, JAX-RS `ContainerRequestFilter` implementations are executed late in the filter chain. This means expensive operations like user authentication (which involves database lookups and cryptographic hashing) were executed *before* the rate limiter evaluated if the user had exceeded their quota. An attacker could exploit this by flooding an endpoint, forcing the server to authenticate every malicious request, leading to CPU exhaustion and a Denial of Service (DoS), completely bypassing the protective intent of the rate limiter.
**Learning:** Security controls like rate limiters and payload size checks must execute as early as possible in the request lifecycle, before any expensive operations (especially authentication or parsing) are performed.
**Prevention:** Always use `@Priority(Priorities.AUTHENTICATION - 100)` (or an appropriately early value) on rate limiting request filters to ensure they execute before authentication and other costly processing layers.
## 2024-04-28 - DoS via Late-Executing Pagination Filter
**Vulnerability:** The `MaxPageSizeFilter` implemented to bound pagination size lacked an explicit `@Priority` configuration. As a result, it executed late in the JAX-RS filter chain, meaning expensive operations like authentication occurred *before* the invalid parameters were rejected. This allowed attackers to flood the system with malformed pagination requests and cause a DoS via authentication resource exhaustion.
**Learning:** Security filters intended to block abusive or malformed requests must execute as early as possible in the request lifecycle to minimize unnecessary processing and save compute resources.
**Prevention:** Always use `@Priority(Priorities.AUTHENTICATION - 100)` (or an appropriate early priority) on request filters that validate limits or prevent resource exhaustion to ensure they run prior to expensive operations.

## 2026-04-29 - Information Exposure via RestDataPanacheException
**Vulnerability:** The application was using `quarkus-hibernate-orm-rest-data-panache` to automatically generate REST endpoints. When users provided invalid query parameters (such as an invalid `sort` field like `?sort=invalidField`), the framework threw a `RestDataPanacheException` (caused by `SemanticException: Could not interpret path expression`). Since there was no specific exception mapper for `RestDataPanacheException`, it fell back to the generic `Throwable` mapper (or default framework handling), which could potentially leak stack traces or, at best, return a generic "Internal Server Error" (500) instead of correctly classifying the client error as a 400 Bad Request.
**Learning:** Framework-generated endpoints that handle query parsing (like pagination, sorting, or filtering) will throw framework-specific exceptions when given malformed input. These exceptions must be explicitly mapped to prevent 500 errors or information disclosure.
**Prevention:** Implement a custom `ExceptionMapper<RestDataPanacheException>` to gracefully handle these mapping/parsing failures, log the error without leaking it to the client, and return a sanitized `400 Bad Request` response instead of a 500 error.

## 2026-04-29 - HTTP Parameter Pollution Bypass in Request Filters
**Vulnerability:** A `ContainerRequestFilter` used to validate query parameters (`SortInjectionFilter`) only checked the *first* occurrence of the parameter using `requestContext.getUriInfo().getQueryParameters().getFirst("sort")`. This meant an attacker could bypass the filter via HTTP Parameter Pollution (HPP) by supplying a valid parameter followed by a malicious one (e.g., `?sort=validField&sort=invalidField`). The underlying application logic might then process the unvalidated subsequent values.
**Learning:** When validating query parameters for security, it is unsafe to only check the first value. Frameworks and libraries may process subsequent values, allowing attackers to sneak malicious payloads past initial filters.
**Prevention:** Always retrieve and validate the full list of parameter values using `.get("paramName")` rather than `.getFirst("paramName")`, and iterate over all values to ensure none of them contain malicious payloads.

## 2024-05-24 - HTTP Parameter Pollution in MaxPageSizeFilter
**Vulnerability:** The `MaxPageSizeFilter` evaluated parameters using `.getFirst("size")` and `.getFirst("page")`. This left it vulnerable to HTTP Parameter Pollution bypasses where an attacker could provide multiple `size` or `page` parameters, tricking the security filter into validating the first (safe) one while the underlying application might consume a subsequent (malicious) one, potentially causing DoS.
**Learning:** Security filters validating query string parameters must validate all occurrences of the parameters, not just the first one.
**Prevention:** Use `.get("paramName")` to retrieve the list of all parameters with that name, and iterate over all occurrences to apply validation.
## 2026-05-01 - Missing Audit Logging on Rate Limiter\n**Vulnerability:** The `RateLimitFilter` correctly restricted requests and prevented DoS attacks, but failed to log when an IP address was blocked. This lack of visibility prevented the security operations team from detecting and responding to abusive IPs or potential brute-force attempts in real-time.\n**Learning:** Implementing a security control (like rate limiting) without logging its enforcement actions creates a 'silent failure' scenario. While the application is protected, administrators remain unaware of the attacks.\n**Prevention:** Always include explicit audit logging (e.g., using `org.jboss.logging.Logger`) when a security control blocks or restricts a request, ensuring the offending IP and action are recorded for incident response.
## 2026-05-02 - Log Injection via Unsanitized Audit Logging
**Vulnerability:** A security filter (`SortInjectionFilter`) was updated to log potentially malicious sort parameters that were blocked. However, it logged the unsanitized user input (`sortParam`) directly via string concatenation (`LOG.warn("Invalid sort parameter blocked: " + sortParam)`). Because the regex validation failed on this parameter, it could contain any character, including newline characters (`\r` or `\n`). This allows an attacker to inject fake log entries (Log Injection, CWE-117) if the underlying logging system doesn't automatically escape newlines.
**Learning:** Even when logging malicious input for security auditing, the input must still be sanitized. Unsanitized input in log messages can lead to log injection, allowing attackers to forge log entries or corrupt log analysis tools.
**Prevention:** Always sanitize user input before logging it. To prevent Log Injection, explicitly strip newline characters (e.g., using `input.replaceAll("[\r\n]", "")`) before passing the string to the logger.

## 2026-05-04 - Fix Log Injection in IP Logging
**Vulnerability:** Log Injection (CWE-117) via `request.remoteAddress().host()`. In Quarkus, with `quarkus.http.proxy.proxy-address-forwarding=true` enabled, `request.remoteAddress().host()` resolves based on the potentially user-controlled `X-Forwarded-For` header.
**Learning:** If this header is not sanitized, malicious users could inject newlines into the IP address, writing fake log entries or spoofing system events.
**Prevention:** Explicitly strip newline characters (e.g., using `replaceAll("[\r\n]", "")`) before passing the string to the logger.
