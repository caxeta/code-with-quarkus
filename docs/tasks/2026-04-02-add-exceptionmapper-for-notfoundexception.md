# Add ExceptionMapper for NotFoundException

## Objective
Implement "Add ExceptionMapper for NotFoundException".
Registers a specific ExceptionMapper for jakarta.ws.rs.NotFoundException
to prevent the GlobalExceptionMapper from intercepting it and returning
a 500 Internal Server Error instead of the standard 404 Not Found response.
Also prevents false-positive stack traces in the server logs for missing resources.

Co-authored-by: caxeta <8550578+caxeta@users.noreply.github.com>

## Changes Made
- `.jules/sentinel.md`
- `src/main/java/org/acme/exception/NotFoundExceptionMapper.java`
- `src/test/java/org/acme/SecurityIT.java`

## Tests
- Run `./mvnw clean test` to ensure all tests pass.
- Verify the specific changes in the affected files.

## QA / Homologation Guide
- Check the application logs for any errors.
- Manually test the affected endpoints or UI elements to ensure they behave as expected.
