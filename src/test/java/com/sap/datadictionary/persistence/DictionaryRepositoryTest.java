package com.sap.datadictionary.persistence;

import com.sap.datadictionary.registry.DataDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DictionaryRepository} file-based persistence.
 */
class DictionaryRepositoryTest {

    @TempDir
    Path tempDir;

    private DictionaryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new DictionaryRepository(tempDir.resolve("dictionary.json"));
    }

    @Test
    void existsReturnsFalseWhenFileDoesNotExist() {
        assertFalse(repository.exists());
    }

    @Test
    void saveAndLoadEmptyDictionary() {
        DataDictionary original = new DataDictionary();
        repository.save(original);

        assertTrue(repository.exists());

        DataDictionary loaded = repository.load();
        assertTrue(loaded.getDomains().isEmpty());
    }

    @Test
    void saveAndLoadFullDictionary() {
        DataDictionary original = DictionarySerializerTest.buildFullDictionary();
        repository.save(original);

        DataDictionary loaded = repository.load();

        assertEquals(3, loaded.getDomains().size());
        assertEquals(3, loaded.getDataElements().size());
        assertEquals(1, loaded.getTables().size());
        assertEquals(1, loaded.getViews().size());
        assertEquals(1, loaded.getSearchHelps().size());
        assertEquals(1, loaded.getLockObjects().size());
    }

    @Test
    void exportAndImportToArbitraryPath() {
        DataDictionary original = DictionarySerializerTest.buildFullDictionary();

        Path exportPath = tempDir.resolve("exports/backup.json");
        repository.exportTo(original, exportPath);

        assertTrue(Files.exists(exportPath));

        DataDictionary imported = repository.importFrom(exportPath);
        assertEquals(3, imported.getDomains().size());
        assertEquals(1, imported.getTables().size());
    }

    @Test
    void exportCreatesParentDirectories() {
        DataDictionary dictionary = new DataDictionary();
        Path nested = tempDir.resolve("a/b/c/dict.json");

        repository.exportTo(dictionary, nested);

        assertTrue(Files.exists(nested));
    }

    @Test
    void loadFromMissingFileThrowsException() {
        assertThrows(DictionarySerializationException.class,
                () -> repository.load());
    }

    @Test
    void importFromMissingFileThrowsException() {
        Path missing = tempDir.resolve("nonexistent.json");
        assertThrows(DictionarySerializationException.class,
                () -> repository.importFrom(missing));
    }

    @Test
    void savedJsonFileIsReadable() throws Exception {
        DataDictionary original = DictionarySerializerTest.buildFullDictionary();
        repository.save(original);

        String content = Files.readString(repository.getStoragePath());
        assertTrue(content.contains("ZCUSTOMER"));
        assertTrue(content.contains("MANDT"));
    }

    @Test
    void overwriteExistingFile() {
        DataDictionary first = new DataDictionary();
        repository.save(first);

        DataDictionary second = DictionarySerializerTest.buildFullDictionary();
        repository.save(second);

        DataDictionary loaded = repository.load();
        assertEquals(3, loaded.getDomains().size());
    }
}
