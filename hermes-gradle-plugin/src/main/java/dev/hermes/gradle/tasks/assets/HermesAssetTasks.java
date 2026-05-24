package dev.hermes.gradle.tasks.assets;

import dev.hermes.gradle.dsl.HermesExtension;
import dev.hermes.gradle.internal.HermesAssets;

import java.io.File;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * Asset list generation and game resource wiring for {@code :game}.
 */
public final class HermesAssetTasks {

    private HermesAssetTasks() {
    }

    public static void register(Project project) {
        File generatedDir = project.file("build/generated/hermes-assets");
        project
                .getExtensions()
                .getByType(SourceSetContainer.class)
                .getByName("main")
                .getResources()
                .srcDir(generatedDir);

        project
                .getTasks()
                .register(
                        "generateAssetList",
                        task -> {
                            task.setGroup("hermes");
                            task.setDescription("Generate assets.txt from the game assets directory");
                        });
        project.getTasks().named("processResources").configure(t -> t.dependsOn("generateAssetList"));
    }

    public static void configureAfterEvaluate(Project project, HermesExtension extension, File assetsDir) {
        wireGameAssetResources(project, assetsDir);
        configureAssetListTask(project, assetsDir);
    }

    private static void configureAssetListTask(Project project, File assetsDir) {
        File generatedDir = project.file("build/generated/hermes-assets");
        File assetsFile = new File(generatedDir, "assets.txt");
        project
                .getTasks()
                .named(
                        "generateAssetList",
                        task -> {
                            task.getInputs().dir(assetsDir);
                            task.getOutputs().file(assetsFile);
                            task.doLast(
                                    t -> {
                                        try {
                                            if (!assetsDir.isDirectory()) {
                                                return;
                                            }
                                            if (!generatedDir.exists() && !generatedDir.mkdirs()) {
                                                throw new GradleException("Could not create " + generatedDir.getAbsolutePath());
                                            }
                                            if (assetsFile.exists() && !assetsFile.delete()) {
                                                throw new GradleException("Could not delete " + assetsFile.getAbsolutePath());
                                            }
                                            collectAssetPaths(assetsDir, assetsDir, assetsFile);
                                        } catch (java.io.IOException e) {
                                            throw new GradleException("Failed to generate assets.txt", e);
                                        }
                                    });
                        });
    }

    private static void collectAssetPaths(File root, File current, File assetsFile) throws java.io.IOException {
        File[] children = current.listFiles();
        if (children == null) {
            return;
        }
        java.util.Arrays.sort(children, java.util.Comparator.comparing(File::getName));
        for (File child : children) {
            if (child.getName().equals("assets.txt")) {
                continue;
            }
            if (child.getName().equals("icons")) {
                continue;
            }
            if (child.isDirectory()) {
                collectAssetPaths(root, child, assetsFile);
            } else {
                String relative = root.toPath().relativize(child.toPath()).toString().replace('\\', '/');
                try (java.io.FileWriter writer = new java.io.FileWriter(assetsFile, true)) {
                    writer.write(relative + "\n");
                }
            }
        }
    }

    private static void wireGameAssetResources(Project project, File assetsDir) {
        File resourcesRoot = project.file("src/main/resources");
        if (!isUnderDirectory(assetsDir, resourcesRoot)) {
            project
                    .getExtensions()
                    .getByType(SourceSetContainer.class)
                    .getByName("main")
                    .getResources()
                    .srcDir(assetsDir);
        }
    }

    private static boolean isUnderDirectory(File child, File parent) {
        try {
            return child.getCanonicalFile().toPath().startsWith(parent.getCanonicalFile().toPath());
        } catch (java.io.IOException e) {
            return false;
        }
    }

    public static File resolveAssetsDir(Project project, HermesExtension extension) {
        return HermesAssets.resolve(project, extension);
    }
}
