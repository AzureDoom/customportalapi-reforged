package net.kyrptonaught.customportalapi.networking;

import java.util.Optional;
import java.util.function.Supplier;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.PerWorldPortals;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public record PortalRegistrySyncPacket(PortalLink link) {

	public static void sendForcePacket(ServerPlayer player, BlockPos pos) {
		NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new net.kyrptonaught.customportalapi.networking.ForcePlacePortalPacket(pos));
	}

	public static PortalRegistrySyncPacket decode(FriendlyByteBuf buf) {
		return new PortalRegistrySyncPacket(new PortalLink(buf.readResourceLocation(), buf.readResourceLocation(), buf.readInt()));
	}

	public static void encode(PortalRegistrySyncPacket packet, FriendlyByteBuf buf) {
		buf.writeResourceLocation(packet.link().block).writeResourceLocation(packet.link().dimID).writeInt(packet.link().colorID);
	}

	public static void handle(PortalRegistrySyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		PerWorldPortals.registerWorldPortal(packet.link());
		contextSupplier.get().setPacketHandled(true);
	}

	public static void register(SimpleChannel channel, Integer id) {
		channel.registerMessage(id, PortalRegistrySyncPacket.class, PortalRegistrySyncPacket::encode, PortalRegistrySyncPacket::decode, PortalRegistrySyncPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}

	public static void registerSyncOnPlayerJoin() {
		MinecraftForge.EVENT_BUS.addListener(PortalRegistrySyncPacket::onPlayerJoinWorld);
	}

	@SubscribeEvent
	public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			for (PortalLink link : CustomPortalApiRegistry.getAllPortalLinks()) {
				NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PortalRegistrySyncPacket(link));
			}
		}
	}
}
