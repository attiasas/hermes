package dev.hermes.core.log;

import com.badlogic.gdx.Gdx;
import dev.hermes.api.log.LogLevel;

public final class GdxLogSink implements LogSink {

    private static String formatMessage(LogLevel level, String message) {
        return "[" + level.name() + "] " + message;
    }

    @Override
    public void log(LogLevel level, String category, String message, Throwable throwable) {
        if (Gdx.app == null) {
            fallbackStderr(level, category, message, throwable);
            return;
        }
        String formattedMessage = formatMessage(level, message);
        switch (level) {
            // We handle the level, Gdx default level is INFO.
            case DEBUG:
            case INFO:
            case WARN:
                if (throwable != null) {
                    formattedMessage += ": " + throwable.getMessage();
                }
                Gdx.app.log(category, formattedMessage);
                break;
            case ERROR:
                if (throwable != null) {
                    Gdx.app.error(category, formattedMessage, throwable);
                } else {
                    Gdx.app.error(category, formattedMessage);
                }
                break;
        }
    }

    private static void fallbackStderr(LogLevel level, String category, String message, Throwable throwable) {
        System.err.println("[" + category + "] " + formatMessage(level, message));
        if (throwable != null) {
            throwable.printStackTrace(System.err);
        }
    }
}
