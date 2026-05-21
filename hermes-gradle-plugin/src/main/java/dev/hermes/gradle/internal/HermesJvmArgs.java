package dev.hermes.gradle.internal;

import org.gradle.api.tasks.JavaExec;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaLauncher;

/** JVM flags that depend on the runtime Java version. */
public final class HermesJvmArgs {

  public static final String NATIVE_ACCESS_FLAG = "--enable-native-access=ALL-UNNAMED";

  private HermesJvmArgs() {}

  /** {@code --enable-native-access} exists from Java 17 onward; Hermes targets Java 11 for game code. */
  public static boolean supportsNativeAccess(JavaExec task) {
    try {
      JavaLauncher launcher = task.getJavaLauncher().get();
      JavaLanguageVersion version = launcher.getMetadata().getLanguageVersion();
      return version.compareTo(JavaLanguageVersion.of(17)) >= 0;
    } catch (Exception ignored) {
      return false;
    }
  }

  public static void stripNativeAccess(java.util.List<String> jvmArgs) {
    jvmArgs.removeIf(arg -> NATIVE_ACCESS_FLAG.equals(arg));
  }
}
