package dev.hermes.api.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.animation.AnimationClipRef;

import java.util.LinkedHashMap;
import java.util.Map;

/** Config + runtime playback state for entity animation clips. */
public final class AnimationController implements Component {

    private Map<String, AnimationClipRef> clips = Map.of();
    private String rigPart = "";
    private String defaultClip = "";
    private float speed = 1f;
    private boolean autoPlay = true;

    private String currentClip = "";
    private AnimationClipRef activeRef;
    private float timeSeconds;
    private boolean playing;
    private boolean finished;

    public Map<String, AnimationClipRef> clips() {
        return clips;
    }

    public void setClips(Map<String, AnimationClipRef> clips) {
        if (clips == null || clips.isEmpty()) {
            this.clips = Map.of();
            return;
        }
        this.clips = Map.copyOf(new LinkedHashMap<>(clips));
    }

    public String rigPart() {
        return rigPart;
    }

    public void setRigPart(String rigPart) {
        this.rigPart = rigPart == null ? "" : rigPart.trim();
    }

    public String defaultClip() {
        return defaultClip;
    }

    public void setDefaultClip(String defaultClip) {
        this.defaultClip = defaultClip == null ? "" : defaultClip.trim();
    }

    public float speed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean autoPlay() {
        return autoPlay;
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public String currentClip() {
        return currentClip;
    }

    public void setCurrentClip(String currentClip) {
        this.currentClip = currentClip == null ? "" : currentClip.trim();
    }

    public AnimationClipRef activeRef() {
        return activeRef;
    }

    public void setActiveRef(AnimationClipRef activeRef) {
        this.activeRef = activeRef;
    }

    public float timeSeconds() {
        return timeSeconds;
    }

    public void setTimeSeconds(float timeSeconds) {
        this.timeSeconds = timeSeconds;
    }

    public boolean playing() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean finished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void initPlayback() {
        if (clips.isEmpty()) {
            return;
        }
        String selected = defaultClip;
        if (selected == null || selected.isBlank() || !clips.containsKey(selected)) {
            selected = clips.keySet().iterator().next();
        }
        setCurrentClip(selected);
        setActiveRef(clips.get(selected));
        setPlaying(true);
        setFinished(false);
        setTimeSeconds(0f);
    }
}
