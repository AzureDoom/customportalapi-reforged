package net.kyrptonaught.customportalapi.event;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.network.PlayerSoundPayload;
import net.kyrptonaught.customportalapi.network.PlayerSoundPayloadHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;

@EventBusSubscriber(modid = CustomPortalsMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

	@SubscribeEvent
	public static void registerPayloadHandlerEvent(RegisterPayloadHandlersEvent event) {
		String version = ModList.get()
				.getModContainerById(CustomPortalsMod.MOD_ID)
				.map(ModContainer::getModInfo)
				.map(IModInfo::getVersion)
				.map(ArtifactVersion::toString)
				.orElse("[UNKNOWN]");

		PayloadRegistrar registrar = event.registrar(version);

		registrar.playToClient(PlayerSoundPayload.TYPE, PlayerSoundPayload.STREAM_CODEC, PlayerSoundPayloadHandler.getInstance()::handleData);
	}
}