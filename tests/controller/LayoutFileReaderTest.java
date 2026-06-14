package controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LayoutFileReaderTest {

    private LayoutFileReader reader;

    @BeforeEach
    void setUp() {
        reader = new LayoutFileReader();
    }

    @Test
    void testRead_SuccessfulWhenFileExists(@TempDir Path tempDir) throws IOException {
        // Arrange: Create a real temporary file with known text content
        String expectedContent = "{\"AreaType\": \"ROOM\"}";
        Path tempFile = tempDir.resolve("test_layout.json");
        Files.writeString(tempFile, expectedContent);

        // Act: Pass the absolute path string to bypass directory searching
        String result = reader.read(tempFile.toAbsolutePath().toString());

        // Assert
        assertEquals(expectedContent, result, "Reader should extract exact content matching the target file bytes");
    }

    @Test
    void testRead_ThrowsIllegalArgumentExceptionWhenFileNotFound() {
        // Arrange: Generate a filename that guaranteed not to exist on disk
        String missingFileName = "completely_fake_layout_file_9999.json";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reader.read(missingFileName);
        }, "Should throw IllegalArgumentException if file cannot be found in src/ or root directories");

        assertTrue(exception.getMessage().contains("Bestand niet gevonden"),
                "Exception error message should indicate missing file status");
    }

    @Test
    void testRead_ResolvesSrcDirectoryFallbackSuccessfully() throws IOException {
        // Arrange: Mimic a "src/" project environment tree by creating a real directory structure
        File currentDir = new File(".");
        File srcFolder = new File(currentDir, "src");
        boolean createdSrc = srcFolder.mkdirs(); // Ensure directory exists safely

        File dummySrcFile = new File(srcFolder, "temporary_test_probe.json");
        String content = "src_folder_content";

        try {
            Files.writeString(dummySrcFile.toPath(), content);

            // Act: Attempt to read it just by name, relying on the "src/" prefix logic inside the reader
            String result = reader.read("temporary_test_probe.json");

            // Assert
            assertEquals(content, result, "Reader should check and load from 'src/' subdirectory first");

        } finally {
            // Cleanup: Always erase temporary files generated on the local storage partition
            Files.deleteIfExists(dummySrcFile.toPath());
            if (createdSrc) {
                srcFolder.delete();
            }
        }
    }
}