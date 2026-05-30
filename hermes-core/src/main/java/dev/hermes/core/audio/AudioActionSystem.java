package dev.hermes.core.audio;

import dev.hermes.api.audio.AudioBus;
import dev.hermes.api.audio.ClipId;
import dev.hermes.api.audio.PlayOptions;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.input.InputActions;

import java.util.Map;

/** GLOBAL system: plays profile action sounds when input actions are just pressed. */
public final class AudioActionSystem implements System {

    private final InputActions actions;
    private final AudioServiceImpl audio;

    public AudioActionSystem(InputActions actions, AudioServiceImpl audio) {
        this.actions = actions;
        this.audio = audio;
    }

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        AudioProfile profile = audio.currentProfile();
        if (profile == null) {
            return;
        }
        for (Map.Entry<String, String> entry : profile.actionSoundEntries()) {
            if (actions.justPressed(entry.getKey())) {
                audio.play(
                        ClipId.of(entry.getValue()),
                        PlayOptions.builder().bus(AudioBus.SFX).build());
            }
        }
    }
}
