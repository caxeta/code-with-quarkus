# Add accessibility attributes to theme toggle and search input

## Objective
Implement "Add accessibility attributes to theme toggle and search input".
Added `aria-label`, `role="button"`, and `tabindex="0"` to the theme toggle span, and `aria-label` to the configuration filter search input across the HTML documentation pages (cdi.html, hibernate-orm.html, panache.html, resteasy.html) to improve keyboard and screen reader accessibility.

Co-authored-by: caxeta <8550578+caxeta@users.noreply.github.com>

## Changes Made
- `.Jules/palette.md`
- `cdi.html`
- `hibernate-orm.html`
- `panache.html`
- `resteasy.html`

## Tests
- Run `./mvnw clean test` to ensure all tests pass.
- Verify the specific changes in the affected files.

## QA / Homologation Guide
- Check the application logs for any errors.
- Manually test the affected endpoints or UI elements to ensure they behave as expected.
