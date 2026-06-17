package app.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilenameSanitizerTest {
    @Test
    void stripsPathTraversalAndUnsafeCharacters() {
        assertEquals("evil-report.pdf", FilenameSanitizer.sanitize("../../evil-report.pdf"));
        assertEquals("avatar_.png", FilenameSanitizer.sanitize("..\\avatar?.png"));
    }

    @Test
    void fallsBackWhenFilenameBecomesEmpty() {
        assertEquals("file", FilenameSanitizer.sanitize("..."));
        assertEquals("file", FilenameSanitizer.sanitize("   "));
    }
}
