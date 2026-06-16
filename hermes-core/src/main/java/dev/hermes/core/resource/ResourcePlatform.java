package dev.hermes.core.resource;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

/** Runtime platform helpers for resource loading policy. */
public final class ResourcePlatform {

    private ResourcePlatform() {}

    public static boolean isHtmlPlatform() {
        return Gdx.app != null && Gdx.app.getType() == Application.ApplicationType.WebGL;
    }
}
