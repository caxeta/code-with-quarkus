# 🛡️ Sentinel: [MEDIUM] Fix Information Exposure on invalid JSON payloads

## Objective
Implement "🛡️ Sentinel: [MEDIUM] Fix Information Exposure on invalid JSON payloads".
Added ExceptionMapper for ProcessingException and JsonbException to
prevent RESTEasy from leaking internal stack traces and implementation
details when a client sends malformed JSON to the API endpoints.

Co-authored-by: caxeta <8550578+caxeta@users.noreply.github.com>

## Changes Made
- `.jules/sentinel.md`
- `src/main/java/org/acme/exception/JsonbExceptionMapper.java`
- `src/main/java/org/acme/exception/ProcessingExceptionMapper.java`
- `src/test/java/org/acme/MyEntityResourceTest.java`

## Tests
- Run `./mvnw clean test` to ensure all tests pass.
- Verify the specific changes in the affected files.

## QA / Homologation Guide
- Check the application logs for any errors.
- Manually test the affected endpoints or UI elements to ensure they behave as expected.
