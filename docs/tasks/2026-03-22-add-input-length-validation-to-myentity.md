# Add input length validation to MyEntity

## Objective
Implement "Add input length validation to MyEntity".
Added `@Size` annotation to `MyEntity.java` and included the `quarkus-hibernate-validator` dependency. This validates payload length constraints at the application boundary, returning a 400 Bad Request instead of causing a 500 Internal Server Error at the database level.

Co-authored-by: caxeta <8550578+caxeta@users.noreply.github.com>

## Changes Made
- `pom.xml`
- `src/main/java/org/acme/MyEntity.java`
- `src/test/java/org/acme/MyEntityResourceTest.java`

## Tests
- Run `./mvnw clean test` to ensure all tests pass.
- Verify the specific changes in the affected files.

## QA / Homologation Guide
- Check the application logs for any errors.
- Manually test the affected endpoints or UI elements to ensure they behave as expected.
