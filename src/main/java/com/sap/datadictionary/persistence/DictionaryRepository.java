package com.sap.datadictionary.persistence;

import com.sap.datadictionary.registry.DataDictionary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * File-based repository that persists a {@link DataDictionary} as JSON.
 * <p>
 * Provides save/load operations for a default storage location as well as
 * import/export operations for arbitrary file paths.
 * </p>
 */
public class DictionaryRepository {

    private final DictionarySerializer serializer;
    private final Path storagePath;

    /**
     * Create a repository that reads/writes the dictionary at the given path.
     *
     * @param storagePath file path for the persisted dictionary (e.g. {@code data/dictionary.json})
     */
    public DictionaryRepository(Path storagePath) {
        this.serializer = new DictionarySerializer();
        this.storagePath = storagePath;
    }

    /** Save the dictionary to the configured storage path. */
    public void save(DataDictionary dictionary) {
        exportTo(dictionary, storagePath);
    }

    /**
     * Load the dictionary from the configured storage path.
     *
     * @return the deserialized {@link DataDictionary}
     * @throws DictionarySerializationException if the file does not exist or cannot be read
     */
    public DataDictionary load() {
        return importFrom(storagePath);
    }

    /** Check whether a persisted dictionary exists at the configured storage path. */
    public boolean exists() {
        return Files.exists(storagePath);
    }

    /**
     * Export the dictionary to an arbitrary file path.
     *
     * @param dictionary the dictionary to export
     * @param target     target file path
     */
    public void exportTo(DataDictionary dictionary, Path target) {
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            String json = serializer.toJson(dictionary);
            Files.writeString(target, json);
        } catch (IOException e) {
            throw new DictionarySerializationException(
                    "Failed to write dictionary to " + target, e);
        }
    }

    /**
     * Import a dictionary from an arbitrary file path.
     *
     * @param source source file path
     * @return the deserialized {@link DataDictionary}
     */
    public DataDictionary importFrom(Path source) {
        try {
            String json = Files.readString(source);
            return serializer.fromJson(json);
        } catch (IOException e) {
            throw new DictionarySerializationException(
                    "Failed to read dictionary from " + source, e);
        }
    }

    /** Return the configured storage path. */
    public Path getStoragePath() {
        return storagePath;
    }
}
