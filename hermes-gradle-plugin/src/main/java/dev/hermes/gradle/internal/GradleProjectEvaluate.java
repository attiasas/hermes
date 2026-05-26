package dev.hermes.gradle.internal;

import org.gradle.api.Action;
import org.gradle.api.Project;

import java.lang.reflect.Method;

public final class GradleProjectEvaluate {

    private GradleProjectEvaluate() {}

    /** Runs {@code action} immediately if {@code project} is already evaluated. */
    public static void whenEvaluated(Project project, Action<Project> action) {
        if (isAlreadyEvaluated(project)) {
            action.execute(project);
            return;
        }
        try {
            project.afterEvaluate(action);
        } catch (Throwable e) {
            // Last-resort fallback: if Gradle reports too-late afterEvaluate, execute.
            action.execute(project);
        }
    }

    private static boolean isAlreadyEvaluated(Project project) {
        Object state = project.getState();
        // Best-effort: query any boolean-like method that looks like it represents "evaluated".
        for (Method m : state.getClass().getMethods()) {
            Class<?> returnType = m.getReturnType();
            if (!(returnType == boolean.class || returnType == Boolean.class)) {
                continue;
            }
            String name = m.getName().toLowerCase();
            if (!name.contains("evaluated")) {
                continue;
            }
            try {
                Object v = m.invoke(state);
                return Boolean.TRUE.equals(v);
            } catch (ReflectiveOperationException ignored) {
                // try next method
            }
        }
        return false;
    }
}
