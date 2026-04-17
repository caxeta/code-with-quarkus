package org.acme.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalErrorMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalErrorMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        // SECURITY: Log the error internally for debugging, but don't leak stack traces to the client
        LOG.error("Unhandled Throwable caught by GlobalErrorMapper", exception);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Internal Server Error\"}")
                .type("application/json")
                .build();
    }
}
