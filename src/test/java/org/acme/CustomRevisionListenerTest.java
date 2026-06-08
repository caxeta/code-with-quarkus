package org.acme;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomRevisionListenerTest {

    @Test
    void testNewRevisionFallback() {
        CustomRevisionListener listener = new CustomRevisionListener();
        CustomRevisionEntity entity = new CustomRevisionEntity();

        // This will attempt to access CDI.current() which is not initialized
        // in a standard unit test, triggering an Exception.
        listener.newRevision(entity);

        assertEquals("unknown", entity.getUsername());
    }
}
