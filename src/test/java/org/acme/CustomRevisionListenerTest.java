package org.acme;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomRevisionListenerTest {

    @Test
    public void testFallbackException() {
        CustomRevisionListener listener = new CustomRevisionListener();
        CustomRevisionEntity entity = new CustomRevisionEntity();

        listener.newRevision(entity);

        assertEquals("unknown", entity.getUsername());
    }
}
