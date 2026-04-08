# 🛡️ Sentinel: [security improvement] Add CSP and Referrer-Policy headers

## Objective
Implement "🛡️ Sentinel: [security improvement] Add CSP and Referrer-Policy headers".
Adds `Content-Security-Policy` and `Referrer-Policy` headers to the application properties
to improve baseline security against XSS and referer leakage.

Co-authored-by: caxeta <8550578+caxeta@users.noreply.github.com>

## Changes Made
- `src/main/resources/application.properties`

## Tests
- Run `./mvnw clean test` to ensure all tests pass.
- Verify the specific changes in the affected files.

## QA / Homologation Guide
- Check the application logs for any errors.
- Manually test the affected endpoints or UI elements to ensure they behave as expected.
