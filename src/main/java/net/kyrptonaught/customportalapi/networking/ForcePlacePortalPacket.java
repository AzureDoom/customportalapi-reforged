package net.kyrptonaught.customportalapi.networking;

import java.util.Optional;
import java.util.function.Supplier;

import net.kyrptonaught.customportalapi.client.ClientHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

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

    public static void handle(ForcePlacePortalPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        ClientHandler.forcePortal(packet);
        contextSupplier.get().setPacketHandled(true);
    }

    public static void register(SimpleChannel channel, Integer id) {
        channel.registerMessage(id, ForcePlacePortalPacket.class, ForcePlacePortalPacket::encode, ForcePlacePortalPacket::decode, ForcePlacePortalPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
