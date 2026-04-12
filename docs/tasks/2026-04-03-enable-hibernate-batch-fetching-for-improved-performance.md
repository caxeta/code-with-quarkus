# ⚡ Bolt: enable Hibernate batch fetching for improved performance

## Objective
Implement "⚡ Bolt: enable Hibernate batch fetching for improved performance".
Enabled `quarkus.hibernate-orm.fetch.batch-size` in `application.properties`
to mitigate the N+1 select problem by allowing Hibernate to fetch associated
entities and collections in batches.

Co-authored-by: caxeta <8550578+caxeta@users.noreply.github.com>

## Changes Made
- `.jules/bolt.md`
- `src/main/resources/application.properties`

## Tests
- Run `./mvnw clean test` to ensure all tests pass.
- Verify the specific changes in the affected files.

## QA / Homologation Guide
- Check the application logs for any errors.
- Manually test the affected endpoints or UI elements to ensure they behave as expected.
