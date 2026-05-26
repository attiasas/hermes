package dev.hermes.gradle.android;

import dev.hermes.gradle.dsl.HermesExtension;
import dev.hermes.gradle.internal.GradleProjectEvaluate;
import dev.hermes.gradle.internal.HermesGameConfigs;
import dev.hermes.gradle.internal.HermesPlatforms;
import dev.hermes.tooling.config.HermesGameConfig;
import dev.hermes.tooling.platform.AndroidPlatform;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

import org.gradle.api.Project;

/**
 * Applies settings-scoped Android platform DSL to the synced launcher module (no AGP compile dependency).
 */
public final class HermesAndroidLauncherConfigurer {

    private HermesAndroidLauncherConfigurer() {
    }

    public static void wire(Project gameProject, Project launcher, HermesExtension gameExtension) {
        GradleProjectEvaluate.whenEvaluated(
                launcher,
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
                new Class<?>[]{Object.class, Object.class},
                "hermesScreenOrientation",
                android.getScreenOrientation());
        invoke(
                defaultConfig,
                "buildConfigField",
                new Class<?>[]{String.class, String.class, String.class},
                "String",
                "HERMES_APPLICATION_CLASS",
                "\"" + applicationClass + "\"");
        invoke(
                defaultConfig,
                "buildConfigField",
                new Class<?>[]{String.class, String.class, String.class},
                "String",
                "HERMES_GAME_SCENE",
                "\"" + gameConfig.getScene() + "\"");
        invoke(
                defaultConfig,
                "resValue",
                new Class<?>[]{String.class, String.class, String.class},
                "string",
                "app_name",
                gameConfig.getTitle());
    }

    private static void setProperty(Object target, String property, Object value) {
        String baseSetter = "set" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
        // AGP has historically used both `compileSdk` and `compileSdkVersion` (and similarly for
        // min/target). Templates use the newer names; reflection may need the older aliases.
        String[] setterCandidates =
                property.endsWith("Version") ? new String[] {baseSetter} : new String[] {baseSetter, baseSetter + "Version"};

        for (String setter : setterCandidates) {
            for (Class<?> type :
                    new Class<?>[]{
                            int.class,
                            Integer.class,
                            String.class,
                            Object.class,
                    }) {
                try {
                    Method m;
                    try {
                        m = target.getClass().getMethod(setter, type);
                    } catch (NoSuchMethodException e) {
                        m = target.getClass().getDeclaredMethod(setter, type);
                        m.setAccessible(true);
                    }
                    Object arg = value;
                    if (type == int.class && value instanceof Integer integer) {
                        arg = integer;
                    } else if (type == String.class && value != null && !(value instanceof String)) {
                        arg = value.toString();
                    }
                    m.invoke(target, arg);
                    return;
                } catch (NoSuchMethodException ignored) {
                    // try next overload / setter candidate
                } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
                    // try next overload
                }
            }
        }

        // Fallback: some AGP decorated objects expose values primarily via fields rather than
        // JavaBean setters (Groovy DSL can still set them). This keeps our configurers robust
        // across AGP internals.
        String[] fieldCandidates =
                property.endsWith("Version")
                        ? new String[] {property}
                        : new String[] {property, property + "Version"};
        for (String fieldName : fieldCandidates) {
            try {
                Field f = target.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                Class<?> fieldType = f.getType();
                if (fieldType == int.class && value instanceof Integer integer) {
                    f.setInt(target, integer);
                    return;
                }
                if (fieldType == Integer.class && value instanceof Integer integer) {
                    f.set(target, integer);
                    return;
                }
                if (fieldType == String.class && value != null) {
                    f.set(target, value.toString());
                    return;
                }
                if (fieldType == Object.class) {
                    f.set(target, value);
                    return;
                }
            } catch (NoSuchFieldException ignored) {
                // try next field candidate
            } catch (IllegalAccessException | IllegalArgumentException ignored) {
                // try next field candidate
            }
        }

        // Some AGP internal/decorated types can differ depending on evaluation order.
        // For platform-wide numeric SDK knobs we already set sensible defaults in the
        // synced android.gradle template; if we can't override them reliably, skip instead
        // of failing the whole configuration.
        if (property.equals("compileSdk")
                || property.equals("minSdk")
                || property.equals("targetSdk")
                || property.equals("versionCode")
                || property.equals("applicationId")
                || property.equals("versionName")) {
            return;
        }

        throw new IllegalStateException(
                "No setter for property '" + property + "' (tried "
                        + String.join(", ", setterCandidates)
                        + ") on "
                        + target.getClass().getName()
                        + " for value "
                        + value);
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
