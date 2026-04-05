# 🛡️ Sentinel: [HIGH] Add CSP header and input length limits

## Objective
Implement "🛡️ Sentinel: [HIGH] Add CSP header and input length limits".
This commit adds a strict Content-Security-Policy (CSP) header
to the application properties to mitigate Cross-Site Scripting (XSS)
and data injection attacks.

It also explicitly limits the length of the `field` property in
`MyEntity.java` to prevent Denial of Service (DoS) and excessive storage
consumption from excessively large payloads.

Co-authored-by: caxeta <8550578+caxeta@users.noreply.github.com>

## Changes Made
- `.jules/sentinel.md`
- `src/main/java/org/acme/MyEntity.java`
- `src/main/resources/application.properties`

## Tests
- Run `./mvnw clean test` to ensure all tests pass.
- Verify the specific changes in the affected files.

## QA / Homologation Guide
- Check the application logs for any errors.
- Manually test the affected endpoints or UI elements to ensure they behave as expected.
