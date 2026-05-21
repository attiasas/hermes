package dev.hermes.gradle;

import dev.hermes.gradle.dsl.HermesExtension;
import dev.hermes.tooling.config.HermesGameConfig;
import dev.hermes.tooling.platform.AndroidPlatform;
import java.lang.reflect.Method;
import org.gradle.api.Project;

/** Applies settings-scoped Android platform DSL to the synced launcher module (no AGP compile dependency). */
final class HermesAndroidLauncherConfigurer {

  private HermesAndroidLauncherConfigurer() {}

  static void wire(Project gameProject, Project launcher, HermesExtension gameExtension) {
    launcher.afterEvaluate(
        evaluated -> {
          if (!launcher.getPlugins().hasPlugin(HermesAndroidGradlePlugin.PLUGIN_ID)) {
            return;
          }
          configure(gameProject, launcher, gameExtension);
        });
  }

  private static void configure(Project gameProject, Project launcher, HermesExtension gameExtension) {
    AndroidPlatform android = HermesPlatforms.resolve(gameProject).getAndroid();
    HermesGameConfig gameConfig = HermesGameConfigs.parse(gameProject);
    String applicationClass = gameExtension.getApplicationClass();
    String versionName = gameProject.getVersion().toString();

    Object androidExt = launcher.getExtensions().getByName("android");
    setProperty(androidExt, "compileSdk", android.getCompileSdk());
    Object defaultConfig = invoke(androidExt, "getDefaultConfig");
    setProperty(defaultConfig, "applicationId", android.getApplicationId());
    setProperty(defaultConfig, "minSdk", android.getMinSdk());
    setProperty(defaultConfig, "targetSdk", android.getTargetSdk());
    setProperty(defaultConfig, "versionCode", android.getVersionCode());
    setProperty(defaultConfig, "versionName", versionName);
    Object placeholders = invoke(defaultConfig, "getManifestPlaceholders");
    invoke(
        placeholders,
        "put",
        new Class<?>[] {Object.class, Object.class},
        "hermesScreenOrientation",
        android.getScreenOrientation());
    invoke(
        defaultConfig,
        "buildConfigField",
        new Class<?>[] {String.class, String.class, String.class},
        "String",
        "HERMES_APPLICATION_CLASS",
        "\"" + applicationClass + "\"");
    invoke(
        defaultConfig,
        "buildConfigField",
        new Class<?>[] {String.class, String.class, String.class},
        "String",
        "HERMES_GAME_SCENE",
        "\"" + gameConfig.getScene() + "\"");
    invoke(
        defaultConfig,
        "resValue",
        new Class<?>[] {String.class, String.class, String.class},
        "string",
        "app_name",
        gameConfig.getTitle());
  }

  private static void setProperty(Object target, String property, Object value) {
    String setter = "set" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
    for (Class<?> type :
        new Class<?>[] {
          int.class,
          Integer.class,
          String.class,
          Object.class,
        }) {
      try {
        Method m = target.getClass().getMethod(setter, type);
        Object arg = value;
        if (type == int.class && value instanceof Integer integer) {
          arg = integer;
        }
        m.invoke(target, arg);
        return;
      } catch (ReflectiveOperationException ignored) {
        // try next overload
      }
    }
    throw new IllegalStateException(
        "No setter " + setter + " on " + target.getClass().getName() + " for value " + value);
  }

  private static Object invoke(Object target, String method, Class<?>[] paramTypes, Object... args) {
    try {
      Method m = target.getClass().getMethod(method, paramTypes);
      return m.invoke(target, args);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to invoke " + method + " on " + target.getClass().getName(), e);
    }
  }

  private static Object invoke(Object target, String method) {
    try {
      Method m = target.getClass().getMethod(method);
      return m.invoke(target);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to invoke " + method + " on " + target.getClass().getName(), e);
    }
  }
}
