package org.acme.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

public class SortInjectionFilterTest {

    private SortInjectionFilter filter;
    private ContainerRequestContext requestContext;
    private UriInfo uriInfo;

    @BeforeEach
    public void setup() {
        filter = new SortInjectionFilter();
        requestContext = Mockito.mock(ContainerRequestContext.class);
        uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
    }

    private void mockSortParams(List<String> params) {
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
        if (params != null) {
            queryParameters.put("sort", params);
        }
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
    }

    @Test
    public void testValidSortParam() {
        mockSortParams(Arrays.asList("name"));
        filter.filter(requestContext);
        verify(requestContext, never()).abortWith(any());

        mockSortParams(Arrays.asList("+date"));
        filter.filter(requestContext);
        verify(requestContext, never()).abortWith(any());

        mockSortParams(Arrays.asList("-value"));
        filter.filter(requestContext);
        verify(requestContext, never()).abortWith(any());

        mockSortParams(Arrays.asList("name,date"));
        filter.filter(requestContext);
        verify(requestContext, never()).abortWith(any());

        mockSortParams(Arrays.asList("name_id"));
        filter.filter(requestContext);
        verify(requestContext, never()).abortWith(any());

        mockSortParams(Arrays.asList("abc.def"));
        filter.filter(requestContext);
        verify(requestContext, never()).abortWith(any());

        mockSortParams(Arrays.asList("name", "+date"));
        filter.filter(requestContext);
        verify(requestContext, never()).abortWith(any());

        mockSortParams(Arrays.asList("name--"));
        filter.filter(requestContext);
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    public void testInvalidSortParam() {
        mockSortParams(Arrays.asList("name;drop table"));
        filter.filter(requestContext);
        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertEquals(400, captor.getValue().getStatus());
    }

    @Test
    public void testInvalidSortParamSqlInjection() {
        mockSortParams(Arrays.asList("name'"));
        filter.filter(requestContext);
        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertEquals(400, captor.getValue().getStatus());
    }

    @Test
    public void testInvalidSortParamParentheses() {
        mockSortParams(Arrays.asList("(name)"));
        filter.filter(requestContext);
        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertEquals(400, captor.getValue().getStatus());
    }

    @Test
    public void testMultipleParamsOneInvalid() {
        mockSortParams(Arrays.asList("validName", "invalid;drop"));
        filter.filter(requestContext);
        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());
        assertEquals(400, captor.getValue().getStatus());
    }

    @Test
    public void testNullSortParamList() {
        mockSortParams(null);
        filter.filter(requestContext);
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    public void testEmptySortParam() {
        mockSortParams(Arrays.asList(""));
        filter.filter(requestContext);
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    public void testNullSortParamValue() {
        mockSortParams(Arrays.asList((String) null));
        filter.filter(requestContext);
        verify(requestContext, never()).abortWith(any());
    }
}
