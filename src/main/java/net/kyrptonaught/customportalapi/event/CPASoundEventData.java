package net.kyrptonaught.customportalapi.event;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record CPASoundEventData(
    SoundEvent sound,
    float pitch,
    float volume
) {

    @Contract(" -> new")
    public @NotNull SimpleSoundInstance getInstance() {
        return SimpleSoundInstance.forLocalAmbience(sound, pitch, volume);
    }
}
