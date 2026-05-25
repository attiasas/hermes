package dev.hermes.core;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Classpath-only {@link Files} for unit tests (no gdx-backend-headless).
 */
final class ClasspathFiles implements Files {

    @Override
    public FileHandle getFileHandle(String path, FileType type) {
        if (type != FileType.Internal) {
            throw new GdxRuntimeException("ClasspathFiles only supports Internal: " + path);
        }
        return new ClasspathFileHandle(path);
    }

    @Override
    public FileHandle classpath(String path) {
        return getFileHandle(path, FileType.Classpath);
    }

    @Override
    public FileHandle internal(String path) {
        return getFileHandle(path, FileType.Internal);
    }

    @Override
    public FileHandle external(String path) {
        return getFileHandle(path, FileType.External);
    }

    @Override
    public FileHandle absolute(String path) {
        return getFileHandle(path, FileType.Absolute);
    }

    @Override
    public FileHandle local(String path) {
        return getFileHandle(path, FileType.Local);
    }

    @Override
    public String getExternalStoragePath() {
        throw unsupported("getExternalStoragePath");
    }

    @Override
    public boolean isExternalStorageAvailable() {
        return false;
    }

    @Override
    public String getLocalStoragePath() {
        throw unsupported("getLocalStoragePath");
    }

    @Override
    public boolean isLocalStorageAvailable() {
        return false;
    }

    private static GdxRuntimeException unsupported(String method) {
        return new GdxRuntimeException("ClasspathFiles does not support " + method);
    }

    private static final class ClasspathFileHandle extends FileHandle {

        private final String path;

        ClasspathFileHandle(String path) {
            super((File) null);
            this.path = normalize(path);
        }

        private static String normalize(String path) {
            if (path == null || path.isBlank()) {
                return "";
            }
            return path.startsWith("/") ? path.substring(1) : path;
        }

        @Override
        public boolean exists() {
            return classpathResource(path) != null || classpathResource("assets/" + path) != null;
        }

        @Override
        public InputStream read() {
            URL url = classpathResource(path);
            if (url == null) {
                url = classpathResource("assets/" + path);
            }
            if (url == null) {
                throw new GdxRuntimeException("Classpath resource not found: " + path);
            }
            try {
                return url.openStream();
            } catch (java.io.IOException e) {
                throw new GdxRuntimeException("Failed to read classpath resource: " + path, e);
            }
        }

        @Override
        public String readString(String charset) {
            try (InputStream in = read()) {
                return new String(in.readAllBytes(), Charset.forName(charset));
            } catch (java.io.IOException e) {
                throw new GdxRuntimeException("Failed to read classpath resource: " + path, e);
            }
        }

        private static URL classpathResource(String resourcePath) {
            if (resourcePath == null || resourcePath.isBlank()) {
                return ClasspathFiles.class.getClassLoader().getResource("");
            }
            return ClasspathFiles.class.getClassLoader().getResource(resourcePath);
        }
    }
}
