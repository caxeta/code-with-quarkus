# Add default cache-control headers to API responses

## Objective
Implement "Add default cache-control headers to API responses".
Added a `CacheControlFilter` to append caching prevention headers (e.g. `Cache-Control: no-store, no-cache...`) to all responses by default, unless caching was already explicitly set (e.g., via `@Cache` annotations). This prevents browsers and intermediate proxies from inadvertently caching potentially sensitive data.

Co-authored-by: caxeta <8550578+caxeta@users.noreply.github.com>

## Changes Made
- `.jules/sentinel.md`
- `src/main/java/org/acme/CacheControlFilter.java`

## Tests
- Run `./mvnw clean test` to ensure all tests pass.
- Verify the specific changes in the affected files.

## QA / Homologation Guide
- Check the application logs for any errors.
- Manually test the affected endpoints or UI elements to ensure they behave as expected.
