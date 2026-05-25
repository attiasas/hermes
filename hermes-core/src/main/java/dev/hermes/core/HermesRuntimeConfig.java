package dev.hermes.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Resolves Hermes launch settings from JVM system properties, classpath {@code hermes-runtime.properties},
 * or (HTML/TeaVM) the same file as a libGDX internal asset under {@code assets/hermes-runtime.properties}.
 */
public final class HermesRuntimeConfig {

    private static volatile Properties packaged;
    private static volatile boolean packagedLoaded;

    private HermesRuntimeConfig() {
    }

    public static String get(String key, String defaultValue) {
        String fromJvm = System.getProperty(key);
        if (fromJvm != null && !fromJvm.isBlank()) {
            return fromJvm;
        }
        ensurePackaged();
        return packaged.getProperty(key, defaultValue);
    }

    /** Clears cached packaged properties so the next {@link #get} reloads (call after Gdx init on HTML). */
    public static void reload() {
        synchronized (HermesRuntimeConfig.class) {
            packagedLoaded = false;
            packaged = null;
        }
    }

    private static void ensurePackaged() {
        if (packagedLoaded) {
            return;
        }
        synchronized (HermesRuntimeConfig.class) {
            if (packagedLoaded) {
                return;
            }
            Properties properties = new Properties();
            loadClasspath(properties);
            if (properties.isEmpty()) {
                loadInternalAsset(properties);
            }
            packaged = properties;
            packagedLoaded = true;
        }
    }

    private static void loadClasspath(Properties properties) {
        try (InputStream in =
                HermesRuntimeConfig.class.getClassLoader().getResourceAsStream("hermes-runtime.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load hermes-runtime.properties from classpath", e);
        }
    }

    private static void loadInternalAsset(Properties properties) {
        if (Gdx.files == null) {
            return;
        }
        FileHandle handle = HermesAssetPaths.internal("hermes-runtime.properties");
        if (!handle.exists()) {
            return;
        }
        try (InputStream in = handle.read()) {
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load hermes-runtime.properties from internal assets", e);
        }
    }
}
