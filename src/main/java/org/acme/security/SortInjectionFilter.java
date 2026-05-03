package org.acme.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import java.util.List;
import java.util.regex.Pattern;

@Provider
@Priority(Priorities.AUTHENTICATION - 50)
public class SortInjectionFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(SortInjectionFilter.class);

    // SECURITY: Only allow alphanumeric characters, underscores, and commas (for multiple fields), optionally preceded by '+' or '-'
    private static final Pattern VALID_SORT_PATTERN = Pattern.compile("^[a-zA-Z0-9_+\\-.,]+$");

    @Override
    public void filter(ContainerRequestContext requestContext) {
        List<String> sortParams = requestContext.getUriInfo().getQueryParameters().get("sort");
        if (sortParams != null) {
            for (String sortParam : sortParams) {
                if (sortParam != null && !sortParam.isEmpty()) {
                    if (!VALID_SORT_PATTERN.matcher(sortParam).matches()) {
                        // SECURITY: Log potential HQL injection attempts for auditing. Prevent Log Injection by stripping newlines.
                        LOG.warn("Invalid sort parameter blocked: " + sortParam.replaceAll("[\r\n]", ""));

                        // SECURITY: Prevent DoS or information disclosure via HQL injection/invalid sort paths
                        requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"error\": \"Invalid sort parameter\"}")
                                .type("application/json")
                                .build());
                        return; // Ensure we stop processing after the first invalid parameter
                    }
                }
            }
        }
    }
}
