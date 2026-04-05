# Add GlobalExceptionMapper to prevent information exposure on unhandled exceptions

## Objective
Implement "Add GlobalExceptionMapper to prevent information exposure on unhandled exceptions".
Co-authored-by: caxeta <8550578+caxeta@users.noreply.github.com>

## Changes Made
- `.jules/sentinel.md`
- `src/main/java/org/acme/exception/GlobalExceptionMapper.java`

## Tests
- Run `./mvnw clean test` to ensure all tests pass.
- Verify the specific changes in the affected files.

## QA / Homologation Guide
- Check the application logs for any errors.
- Manually test the affected endpoints or UI elements to ensure they behave as expected.
