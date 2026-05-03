## 2026-05-03 - Prevent Log Injection in Request Filters and Exception Mappers
**Vulnerability:** Log Injection (CWE-117) via unsanitized client IP addresses (from `X-Forwarded-For`) and exception messages.
**Learning:** Even internal exception messages can contain unsanitized user inputs, and IP addresses should not be implicitly trusted when proxy address forwarding is enabled.
**Prevention:** Explicitly strip newline characters (`[\r\n]`) from dynamic inputs before passing them to the logger.
