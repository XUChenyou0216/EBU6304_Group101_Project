package com.ta.util;

import java.io.*;
import java.util.Properties;

public class ConfigDAO {
    private final String configPath;

    public ConfigDAO(String dataDir) {
        this.configPath = dataDir + "/config.properties";
    }

    public synchronized String get(String key) {
        return load().getProperty(key);
    }

    public synchronized void set(String key, String value) {
        Properties props = load();
        if (value == null || value.isEmpty()) {
            props.remove(key);
        } else {
            props.setProperty(key, value);
        }
        save(props);
    }

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
