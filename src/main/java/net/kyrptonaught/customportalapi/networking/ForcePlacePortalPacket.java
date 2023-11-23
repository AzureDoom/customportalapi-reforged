package net.kyrptonaught.customportalapi.networking;

import net.kyrptonaught.customportalapi.client.ClientHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.simple.SimpleChannel;

import java.util.Optional;

public record ForcePlacePortalPacket(BlockPos pos) {

    public static void sendForcePacket(ServerPlayer player, BlockPos pos) {
        NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ForcePlacePortalPacket(pos));
    }

    public static ForcePlacePortalPacket decode(FriendlyByteBuf buf) {
        return new ForcePlacePortalPacket(buf.readBlockPos());
    }

    public static void encode(ForcePlacePortalPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static void handle(ForcePlacePortalPacket packet, NetworkEvent.Context contextSupplier) {
        ClientHandler.forcePortal(packet);
        contextSupplier.setPacketHandled(true);
    }

    public static void register(SimpleChannel channel, Integer id) {
        channel.registerMessage(id, ForcePlacePortalPacket.class, ForcePlacePortalPacket::encode, ForcePlacePortalPacket::decode, ForcePlacePortalPacket::handle, Optional.of(PlayNetworkDirection.PLAY_TO_CLIENT));
    }
}
