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
