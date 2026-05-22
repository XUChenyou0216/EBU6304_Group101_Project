package com.ta.util;

import java.io.*;
import java.util.Properties;

/**
 * Data access object for reading and writing application configuration stored in
 * {@code config.properties} within the data directory.
 * <p>
 * All public methods are synchronized to ensure thread-safe access to the properties file.
 * Missing keys return {@code null}; setting a key to {@code null} or an empty string removes it.
 * </p>
 */
public class ConfigDAO {
    private final String configPath;

    /**
     * Creates a configuration accessor for the given data directory.
     *
     * @param dataDir absolute or relative path to the data directory; the properties file
     *                is expected at {@code {dataDir}/config.properties}
     */
    public ConfigDAO(String dataDir) {
        this.configPath = dataDir + "/config.properties";
    }

    /**
     * Retrieves the value associated with the given configuration key.
     *
     * @param key the property key to look up
     * @return the property value, or {@code null} if the key is absent or the file does not exist
     */
    public synchronized String get(String key) {
        return load().getProperty(key);
    }

    /**
     * Sets or removes a configuration property and persists the change to disk.
     * <p>
     * Passing {@code null} or an empty string for {@code value} removes the key from the file.
     * </p>
     *
     * @param key   the property key to set or remove
     * @param value the new property value, or {@code null}/empty to delete the key
     */
    public synchronized void set(String key, String value) {
        Properties props = load();
        if (value == null || value.isEmpty()) {
            props.remove(key);
        } else {
            props.setProperty(key, value);
        }
        save(props);
    }

    /**
     * Loads and returns a copy of all configuration properties.
     *
     * @return a {@link Properties} instance containing all key-value pairs from the file;
     *         empty if the file does not exist or cannot be read
     */
    public synchronized Properties getAll() {
        return load();
    }

    private Properties load() {
        Properties props = new Properties();
        File f = new File(configPath);
        if (!f.exists()) return props;
        try (InputStream in = new FileInputStream(f)) {
            props.load(in);
        } catch (IOException ignored) {}
        return props;
    }

    private void save(Properties props) {
        try (OutputStream out = new FileOutputStream(configPath)) {
            props.store(out, "TA Recruitment System Configuration");
        } catch (IOException ignored) {}
    }
}
