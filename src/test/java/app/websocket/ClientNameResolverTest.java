package app.websocket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientNameResolverTest {
    @Test
    void decodesNameFromQueryString() {
        assertEquals("Budi Santoso", ClientNameResolver.resolve("/chat?name=Budi+Santoso"));
        assertEquals("Siti Aminah", ClientNameResolver.resolve("/chat?name=Siti%20Aminah"));
    }

    @Test
    void fallsBackToAnonymousForInvalidInputs() {
        assertEquals("anonymous", ClientNameResolver.resolve(null));
        assertEquals("anonymous", ClientNameResolver.resolve("/chat"));
        assertEquals("anonymous", ClientNameResolver.resolve("/chat?name="));
    }
}
