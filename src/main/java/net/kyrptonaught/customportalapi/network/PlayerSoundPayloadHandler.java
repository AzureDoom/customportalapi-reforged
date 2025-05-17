package net.kyrptonaught.customportalapi.network;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PlayerSoundPayloadHandler {

	private static final PlayerSoundPayloadHandler INSTANCE = new PlayerSoundPayloadHandler();

	public static PlayerSoundPayloadHandler getInstance() {
		return INSTANCE;
	}

	public void handleData(final PlayerSoundPayload data, final IPayloadContext context) {
		context.enqueueWork(() -> {
					SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(data.location()).orElseThrow().value();
					context.player().playSound(soundEvent, data.volume(), data.pitch());
				})
				.exceptionally(e -> {
					context.disconnect(Component.literal(e.getMessage()));
					return null;
				});
	}
}