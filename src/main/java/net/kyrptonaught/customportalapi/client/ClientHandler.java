package net.kyrptonaught.customportalapi.client;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.networking.ForcePlacePortalPacket;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.minecraft.client.Minecraft;

public class ClientHandler {
	public static void forcePortal(ForcePlacePortalPacket packet) {
		Minecraft.getInstance().execute(() -> {
			var world = Minecraft.getInstance().level;
			world.setBlockAndUpdate(packet.pos(), CustomPortalHelper.blockWithAxis(CustomPortalsMod.getDefaultPortalBlock().defaultBlockState(), CustomPortalHelper.getAxisFrom(world.getBlockState(packet.pos()))));
		});
	}
}
