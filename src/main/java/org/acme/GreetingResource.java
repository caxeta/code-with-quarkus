package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.annotations.cache.Cache;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Cache(maxAge = 3600) // ⚡ Bolt: Add HTTP Cache-Control header to cache static responses
    public String hello() {
        return "Hello RESTEasy";
    }
}
