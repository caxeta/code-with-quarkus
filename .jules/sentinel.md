## 2024-05-18 - [Input Validation in Panache Generated Resources]
**Vulnerability:** Missing input validation on Panache Entity fields exposed directly via REST Data Panache.
**Learning:** Quarkus `quarkus-hibernate-orm-rest-data-panache` automatically generates REST endpoints for Entities. If validation annotations (like `@Pattern`) are omitted from the Entity, the generated endpoints will blindly accept and persist malicious payloads (e.g., XSS strings), bypassing normal API-layer checks.
**Prevention:** Always apply Jakarta Validation annotations (`@Pattern`, `@Size`, etc.) directly on Panache Entity fields to ensure defense-in-depth for auto-generated endpoints.
