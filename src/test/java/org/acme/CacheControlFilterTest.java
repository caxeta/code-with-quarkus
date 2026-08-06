package org.acme;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CacheControlFilterTest {

    @Test
    public void testFilterAddsCacheControlHeaders() {
        CacheControlFilter filter = new CacheControlFilter();

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        ContainerResponseContext mockResponseContext = new DummyContainerResponseContext(headers);

        filter.filter(null, mockResponseContext);

        assertEquals("no-store, no-cache, must-revalidate, max-age=0", headers.getFirst("Cache-Control"));
        assertEquals("no-cache", headers.getFirst("Pragma"));
        assertEquals("0", headers.getFirst("Expires"));
    }

    @Test
    public void testFilterDoesNotAddCacheControlHeadersIfAlreadyPresent() {
        CacheControlFilter filter = new CacheControlFilter();

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add("Cache-Control", "public, max-age=3600");
        ContainerResponseContext mockResponseContext = new DummyContainerResponseContext(headers);

        filter.filter(null, mockResponseContext);

        assertEquals("public, max-age=3600", headers.getFirst("Cache-Control"));
        assertNull(headers.getFirst("Pragma"));
        assertNull(headers.getFirst("Expires"));
    }

    private static class DummyContainerResponseContext implements ContainerResponseContext {
        private final MultivaluedMap<String, Object> headers;

        public DummyContainerResponseContext(MultivaluedMap<String, Object> headers) {
            this.headers = headers;
        }

        @Override
        public MultivaluedMap<String, Object> getHeaders() {
            return headers;
        }

        @Override
        public String getHeaderString(String name) {
            Object header = headers.getFirst(name);
            return header == null ? null : header.toString();
        }

        // Dummy implementations for other methods
        @Override public int getStatus() { return 0; }
        @Override public void setStatus(int code) {}
        @Override public jakarta.ws.rs.core.Response.StatusType getStatusInfo() { return null; }
        @Override public void setStatusInfo(jakarta.ws.rs.core.Response.StatusType statusInfo) {}
        @Override public MultivaluedMap<String, String> getStringHeaders() { return null; }
        @Override public java.util.Set<String> getAllowedMethods() { return null; }
        @Override public java.util.Date getDate() { return null; }
        @Override public java.util.Locale getLanguage() { return null; }
        @Override public int getLength() { return 0; }
        @Override public jakarta.ws.rs.core.MediaType getMediaType() { return null; }
        @Override public java.util.Map<String, jakarta.ws.rs.core.NewCookie> getCookies() { return null; }
        @Override public jakarta.ws.rs.core.EntityTag getEntityTag() { return null; }
        @Override public java.util.Date getLastModified() { return null; }
        @Override public java.net.URI getLocation() { return null; }
        @Override public java.util.Set<jakarta.ws.rs.core.Link> getLinks() { return null; }
        @Override public boolean hasLink(String relation) { return false; }
        @Override public jakarta.ws.rs.core.Link getLink(String relation) { return null; }
        @Override public jakarta.ws.rs.core.Link.Builder getLinkBuilder(String relation) { return null; }
        @Override public boolean hasEntity() { return false; }
        @Override public Object getEntity() { return null; }
        @Override public Class<?> getEntityClass() { return null; }
        @Override public java.lang.reflect.Type getEntityType() { return null; }
        @Override public void setEntity(Object entity) {}
        @Override public void setEntity(Object entity, java.lang.annotation.Annotation[] annotations, jakarta.ws.rs.core.MediaType mediaType) {}
        @Override public java.lang.annotation.Annotation[] getEntityAnnotations() { return null; }
        @Override public java.io.OutputStream getEntityStream() { return null; }
        @Override public void setEntityStream(java.io.OutputStream outputStream) {}
    }
}
