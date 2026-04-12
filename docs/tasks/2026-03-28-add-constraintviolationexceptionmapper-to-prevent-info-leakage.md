# Add ConstraintViolationExceptionMapper to prevent info leakage

## Objective
Implement "Add ConstraintViolationExceptionMapper to prevent info leakage".
Added `ConstraintViolationExceptionMapper` to catch and sanitize `ConstraintViolationException` in Quarkus RESTEasy, preventing internal method paths and unsanitized user inputs from leaking into HTTP responses. Updated `MyEntityResourceTest` to verify the sanitized response body.

Co-authored-by: caxeta <8550578+caxeta@users.noreply.github.com>

## Changes Made
- `.jules/sentinel.md`
- `src/main/java/org/acme/exception/ConstraintViolationExceptionMapper.java`
- `src/test/java/org/acme/MyEntityResourceTest.java`

## Tests
- Run `./mvnw clean test` to ensure all tests pass.
- Verify the specific changes in the affected files.

## QA / Homologation Guide
- Check the application logs for any errors.
- Manually test the affected endpoints or UI elements to ensure they behave as expected.
