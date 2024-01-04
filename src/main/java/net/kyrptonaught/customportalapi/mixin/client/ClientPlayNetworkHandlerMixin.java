package net.kyrptonaught.customportalapi.mixin.client;

import net.kyrptonaught.customportalapi.interfaces.ClientPlayerInColoredPortal;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {

    @Redirect(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;createPlayer(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/stats/StatsCounter;Lnet/minecraft/client/ClientRecipeBook;ZZ)Lnet/minecraft/client/player/LocalPlayer;"))
    public LocalPlayer teleported(MultiPlayerGameMode instance, ClientLevel world, StatsCounter statHandler, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting) {
        LocalPlayer newPlayer = instance.createPlayer(world, statHandler, recipeBook, lastSneaking, lastSprinting);
        ((ClientPlayerInColoredPortal) newPlayer).setLastUsedPortalColor(-999);
        return newPlayer;
    }
}