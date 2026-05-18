package dev.hermes.gradle;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

/** Hermes targets Java 11 bytecode for game and launcher compatibility (TeaVM, Android). */
final class HermesJavaToolchain {

  private HermesJavaToolchain() {}

  static void applyJava11(Project project) {
    JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
    java.toolchain(spec -> spec.getLanguageVersion().set(JavaLanguageVersion.of(11)));
    project
        .getTasks()
        .withType(JavaCompile.class)
        .configureEach(compile -> compile.getOptions().getRelease().set(11));
  }
}
