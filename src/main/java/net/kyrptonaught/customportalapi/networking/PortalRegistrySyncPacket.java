package net.kyrptonaught.customportalapi.networking;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.PerWorldPortals;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.simple.SimpleChannel;

import java.util.Optional;

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

    public static void handle(PortalRegistrySyncPacket packet, NetworkEvent.Context contextSupplier) {
        PerWorldPortals.registerWorldPortal(packet.link());
        contextSupplier.setPacketHandled(true);
    }

    public static void register(SimpleChannel channel, Integer id) {
        channel.registerMessage(id, PortalRegistrySyncPacket.class, PortalRegistrySyncPacket::encode, PortalRegistrySyncPacket::decode, PortalRegistrySyncPacket::handle, Optional.of(PlayNetworkDirection.PLAY_TO_CLIENT));
    }

    public static void registerSyncOnPlayerJoin() {
        NeoForge.EVENT_BUS.addListener(PortalRegistrySyncPacket::onPlayerJoinWorld);
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
