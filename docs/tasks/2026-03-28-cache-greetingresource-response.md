# ⚡ Bolt: Cache GreetingResource response

## Objective
Implement "⚡ Bolt: Cache GreetingResource response".
Add `@Cache(maxAge = 3600)` to the `GreetingResource.hello()` endpoint to add HTTP `Cache-Control` headers for the static response. This prevents repeated client requests and reduces unnecessary processing on the server.

Co-authored-by: caxeta <8550578+caxeta@users.noreply.github.com>

## Changes Made
- `src/main/java/org/acme/GreetingResource.java`

## Tests
- Run `./mvnw clean test` to ensure all tests pass.
- Verify the specific changes in the affected files.

## QA / Homologation Guide
- Check the application logs for any errors.
- Manually test the affected endpoints or UI elements to ensure they behave as expected.
