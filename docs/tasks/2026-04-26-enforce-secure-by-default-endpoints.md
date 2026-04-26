# Objective
Enforce a "secure by default" posture for the application's REST endpoints to prevent accidental data exposure.

# Changes Made
*   Added `quarkus.security.jaxrs.deny-unannotated-endpoints=true` to `src/main/resources/application.properties` to ensure any unannotated JAX-RS endpoint is denied by default.
*   Added the `@PermitAll` annotation to the `GreetingResource` so it remains publicly accessible.
*   Replaced `@Authenticated` with `@RolesAllowed("admin")` in `MyEntityResource` to apply the principle of least privilege to the sensitive entity endpoint.

# Tests
*   Ran the full `./mvnw clean test` suite, verifying all existing and modified endpoints behave as expected under the new security constraints. All tests passed.

# QA / Homologation Guide
*   Verify that unauthenticated requests to `/hello` succeed (return 200).
*   Verify that requests to `/my-entity` require authentication and the `admin` role.
*   Verify that if any new endpoint is created without a security annotation (like `@PermitAll`, `@Authenticated`, or `@RolesAllowed`), the application will deny access and return a `401 Unauthorized` or `403 Forbidden`.
