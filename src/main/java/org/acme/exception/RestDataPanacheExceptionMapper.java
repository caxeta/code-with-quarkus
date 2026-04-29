package org.acme.exception;

import io.quarkus.rest.data.panache.RestDataPanacheException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class RestDataPanacheExceptionMapper implements ExceptionMapper<RestDataPanacheException> {

    private static final Logger LOG = Logger.getLogger(RestDataPanacheExceptionMapper.class);

    @Override
    public Response toResponse(RestDataPanacheException exception) {
        // SECURITY: Do not leak stack traces or exception messages which might contain query structure
        LOG.warn("Caught RestDataPanacheException: " + exception.getMessage());
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Invalid request parameters\"}")
                .type("application/json")
                .build();
    }
}
