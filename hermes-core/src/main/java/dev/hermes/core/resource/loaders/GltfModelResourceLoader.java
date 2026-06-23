package dev.hermes.core.resource.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.resource.DecodedPayload;
import dev.hermes.core.resource.ResourceLoader;
import dev.hermes.core.resource.ResourcePlatform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;

/** Loads split glTF (and desktop GLB) assets through gdx-gltf. */
public final class GltfModelResourceLoader implements ResourceLoader {

    @Override
    public ResourceKind kind() {
        return ResourceKind.GLTF_MODEL;
    }

    @Override
    public DecodedPayload decode(String path) {
        FileHandle file = HermesAssetPaths.internal(path);
        if (!file.exists()) {
            throw new ResourceLoadException("glTF asset not found: " + path);
        }
        String ext = extension(path);
        if (!"gltf".equals(ext) && !"glb".equals(ext)) {
            throw new ResourceLoadException("GLTF_MODEL requires .gltf or .glb path: " + path);
        }
        if ("glb".equals(ext) && ResourcePlatform.isHtmlPlatform()) {
            throw new ResourceLoadException("GLB is not supported on HTML; use split .gltf + .bin: " + path);
        }
        return DecodedPayload.fromSourcePath(path);
    }

    @Override
    public Object upload(DecodedPayload decoded) {
        String path = decoded.sourcePath();
        if (path == null || path.isBlank()) {
            throw new ResourceLoadException("GLTF decode produced no source path");
        }
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) {
            file = HermesAssetPaths.internal(path);
        }
        if (!file.exists()) {
            throw new ResourceLoadException("glTF file not found: " + path);
        }
        String ext = extension(path);
        try {
            FileHandle loadable = materializeForSeparatedLoading(file, path, ext);
            Object loader = createLoader(ext);
            Method load = loader.getClass().getMethod("load", FileHandle.class);
            Object loaded = load.invoke(loader, loadable);
            return extractModel(loaded);
        } catch (ResourceLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceLoadException("Failed to load glTF model: " + path, e);
        }
    }

    @Override
    public void dispose(Object resource) {
        if (resource instanceof Model) {
            ((Model) resource).dispose();
        }
    }

    private static FileHandle materializeForSeparatedLoading(FileHandle file, String path, String ext) {
        File backing = file.file();
        if (backing != null) {
            return file;
        }
        if (!"gltf".equals(ext)) {
            return file;
        }
        if (ResourcePlatform.isHtmlPlatform()) {
            return file;
        }
        try {
            File tempDir =
                    new File(
                            System.getProperty("java.io.tmpdir"),
                            "hermes-gltf-" + UUID.randomUUID());
            if (!tempDir.mkdirs() && !tempDir.isDirectory()) {
                throw new ResourceLoadException("Unable to create temp glTF directory: " + tempDir);
            }
            String fileName = fileNameFromPath(path);
            File gltfFile = new File(tempDir, fileName);
            writeBytes(gltfFile, readAllBytes(file));

            int slash = path.lastIndexOf('/');
            String directory = slash >= 0 ? path.substring(0, slash + 1) : "";
            String binPath = directory + stripExtension(fileName) + ".bin";
            FileHandle binSource = HermesAssetPaths.internal(binPath);
            if (!binSource.exists()) {
                binSource = Gdx.files.internal(binPath);
            }
            if (binSource.exists()) {
                writeBytes(new File(tempDir, fileNameFromPath(binPath)), readAllBytes(binSource));
            }
            return new FileHandle(gltfFile);
        } catch (ResourceLoadException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceLoadException("Failed to materialize classpath glTF: " + path, e);
        }
    }

    private static Object createLoader(String extension) throws Exception {
        String className =
                "glb".equals(extension)
                        ? "net.mgsx.gltf.loaders.glb.GLBLoader"
                        : "net.mgsx.gltf.loaders.gltf.GLTFLoader";
        Class<?> loaderType = Class.forName(className);
        return loaderType.getDeclaredConstructor().newInstance();
    }

    private static Model extractModel(Object loaded) throws Exception {
        if (loaded == null) {
            throw new ResourceLoadException("gdx-gltf returned null payload");
        }
        if (loaded instanceof Model) {
            return (Model) loaded;
        }
        Model model = tryExtractModel(loaded);
        if (model == null) {
            throw new ResourceLoadException(
                    "Unable to extract Model from gdx-gltf payload type: " + loaded.getClass().getName());
        }
        return model;
    }

    private static Model tryExtractModel(Object source) throws Exception {
        if (source == null) {
            return null;
        }
        if (source instanceof Model) {
            return (Model) source;
        }
        Model byMethod = invokeModelMethod(source, "getModel");
        if (byMethod != null) {
            return byMethod;
        }
        Model byField = readModelField(source, "model");
        if (byField != null) {
            return byField;
        }
        Object scene = readField(source, "scene");
        Model sceneModel = tryExtractModel(scene);
        if (sceneModel != null) {
            return sceneModel;
        }
        Object modelInstance = readField(source, "modelInstance");
        Model instanceModel = tryExtractModel(modelInstance);
        if (instanceModel != null) {
            return instanceModel;
        }
        Object[] nested =
                asObjectArray(readField(source, "scenes")) != null
                        ? asObjectArray(readField(source, "scenes"))
                        : asObjectArray(readField(source, "nodes"));
        if (nested != null && nested.length > 0) {
            return tryExtractModel(nested[0]);
        }
        return null;
    }

    private static Model invokeModelMethod(Object source, String methodName) {
        try {
            Method method = source.getClass().getMethod(methodName);
            Object value = method.invoke(source);
            return value instanceof Model ? (Model) value : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Model readModelField(Object source, String fieldName) {
        Object value = readField(source, fieldName);
        return value instanceof Model ? (Model) value : null;
    }

    private static Object readField(Object source, String fieldName) {
        if (source == null) {
            return null;
        }
        try {
            Field field = source.getClass().getField(fieldName);
            return field.get(source);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Object[] asObjectArray(Object value) {
        if (value instanceof Object[]) {
            return (Object[]) value;
        }
        return null;
    }

    private static void writeBytes(File target, byte[] bytes) {
        try (FileOutputStream out = new FileOutputStream(target)) {
            out.write(bytes);
        } catch (Exception e) {
            throw new ResourceLoadException("Failed to write glTF temp file: " + target, e);
        }
    }

    private static byte[] readAllBytes(FileHandle file) {
        try (InputStream in = file.read()) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new ResourceLoadException("Failed to read glTF asset bytes", e);
        }
    }

    private static String fileNameFromPath(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static String stripExtension(String name) {
        int idx = name.lastIndexOf('.');
        return idx < 0 ? name : name.substring(0, idx);
    }

    private static String extension(String path) {
        int idx = path.lastIndexOf('.');
        if (idx < 0 || idx == path.length() - 1) {
            return "";
        }
        return path.substring(idx + 1).toLowerCase(Locale.ROOT);
    }
}
