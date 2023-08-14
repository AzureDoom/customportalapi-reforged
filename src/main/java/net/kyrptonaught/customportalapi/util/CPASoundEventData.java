package net.kyrptonaught.customportalapi.util;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

public record CPASoundEventData(SoundEvent sound, float pitch, float volume) {

    public SimpleSoundInstance getInstance() {
        return SimpleSoundInstance.forLocalAmbience(sound, pitch, volume);
    }
}
