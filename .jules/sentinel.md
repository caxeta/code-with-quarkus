## 2024-05-24 - Missing Security Headers in Quarkus App
**Vulnerability:** The Quarkus application was missing standard security headers (X-Content-Type-Options, X-XSS-Protection, X-Frame-Options, Strict-Transport-Security), which left it vulnerable to various attacks like MIME sniffing, clickjacking, and XSS.
**Learning:** In a Quarkus application, security headers aren't added by default. You need to explicitly configure them, usually in `src/main/resources/application.properties`.
**Prevention:** Always add standard security headers via `quarkus.http.header...` configuration when bootstrapping a new Quarkus web application to ensure baseline security.
