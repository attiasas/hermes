package dev.hermes.core.log;

import com.badlogic.gdx.Gdx;
import dev.hermes.api.log.LogLevel;

public final class GdxLogSink implements LogSink {
    
    @Override
    public void log(LogLevel level, String category, String message, Throwable throwable) {
        if (Gdx.app == null) {
            fallbackStderr(level, category, message, throwable);
            return;
        }
        switch (level) {
            case DEBUG:
                if (throwable != null) {
                    Gdx.app.debug(category, message + ": " + throwable.getMessage());
                } else {
                    Gdx.app.debug(category, message);
                }
                break;
            case INFO:
                Gdx.app.log(category, message);
                break;
            case WARN:
                Gdx.app.log(category, "[WARN] " + message);
                break;
            case ERROR:
                if (throwable != null) {
                    Gdx.app.error(category, message, throwable);
                } else {
                    Gdx.app.error(category, message);
                }
                break;
            default:
                Gdx.app.log(category, message);
                break;
        }
    }

    private static void fallbackStderr(LogLevel level, String category, String message, Throwable throwable) {
        System.err.println(level + "[" + category + "] " + message);
        if (throwable != null) {
            throwable.printStackTrace(System.err);
        }
    }
}
