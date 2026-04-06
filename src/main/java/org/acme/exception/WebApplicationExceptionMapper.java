package org.acme.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOG = Logger.getLogger(WebApplicationExceptionMapper.class);

    @Override
    public Response toResponse(WebApplicationException exception) {
        // SECURITY: Prevent GlobalExceptionMapper from intercepting standard HTTP exceptions
        // (like 401, 403, 405) and turning them into 500 Internal Server Errors.
        // This preserves proper HTTP semantics for security tooling and prevents log flooding (DoS).
        Response response = exception.getResponse();

        String errorMessage = "An error occurred";
        if (response.getStatusInfo() != null && response.getStatusInfo().getReasonPhrase() != null) {
            errorMessage = response.getStatusInfo().getReasonPhrase();
        }

        return Response.fromResponse(response)
                .entity("{\"error\": \"" + errorMessage + "\"}")
                .type("application/json")
                .build();
    }
}
