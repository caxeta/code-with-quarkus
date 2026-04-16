# 2026-04-16 Cache CDI Proxy in Envers RevisionListener

## Objective
The objective of this task is to improve the database transaction performance by completely eliminating redundant CDI context creations and lookups in the Envers `RevisionListener`. Instead of dynamically resolving and creating the `SecurityIdentity` bean on every revision, we can cache the CDI proxy itself, which is a thread-safe singleton.

## Changes Made
- Modified `src/main/java/org/acme/CustomRevisionListener.java` to cache a single instance of `SecurityIdentity` directly instead of caching `BeanManager` and `Bean` references.
- Replaced the repetitive `beanManager.createCreationalContext()` and `beanManager.getReference()` invocations with a direct check against the cached `SecurityIdentity`.

## Tests
- Confirmed that the application tests continue to run and pass correctly after the change (`./mvnw clean test`).
- The test suite verified that `SecurityIdentity` is correctly proxied and can determine the current principal during revision creation without failure.

## QA / Homologation Guide
- Execute load testing on endpoints that perform multiple updates/inserts (e.g., POST to Panache resources) and confirm the overhead per transaction is reduced.
- Verify that the correct username still appears in the Envers audit logs (`REVINFO` table) based on the user making the request.
