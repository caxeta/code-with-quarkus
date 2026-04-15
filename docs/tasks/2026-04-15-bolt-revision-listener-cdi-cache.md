# Objective
Eliminate the severe performance penalty of looking up the CDI bean and creating contexts inside an Envers `RevisionListener`.

# Changes Made
- Edited `src/main/java/org/acme/CustomRevisionListener.java` to cache the CDI proxy for `SecurityIdentity` directly using a `volatile` field.
- Refactored `newRevision()` to assign `securityIdentity = CDI.current().select(SecurityIdentity.class).get()` if uninitialized.
- Removed unused imports and variables (`beanManager`, `securityIdentityBean`, `CreationalContext`).
- Added a learning entry to `.jules/bolt.md`.

# Tests
- `./mvnw clean test` passes successfully.

# QA / Homologation Guide
- Monitor application performance (specifically CPU usage and garbage collection) during heavy database write operations to observe reduced overhead from the Envers `RevisionListener`.
