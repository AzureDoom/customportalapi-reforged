package net.kyrptonaught.customportalapi.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@OnlyIn(Dist.CLIENT)
@Mixin(Gui.class)
public class InGameHudMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private int lastColor = -1;

    @ModifyExpressionValue(method = "renderPortalOverlay", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/ARGB;white(F)I"))
    public int changeColor(int original) {
        if (minecraft.player == null) {
            return original;
        }

        isCustomPortal(minecraft.player);
        return lastColor >= 0 ? lastColor : original;
    }

    @Redirect(
        method = "renderPortalOverlay", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getParticleIcon(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"
        )
    )
    public TextureAtlasSprite renderCustomPortalOverlay(BlockModelShaper blockModels, BlockState blockState) {
        if (lastColor >= 0) {
            return this.minecraft.getBlockRenderer()
                .getBlockModelShaper()
                .getParticleIcon(
                    CustomPortalsMod.CUSTOM_PORTAL_BLOCK.get().defaultBlockState()
                );
        }
        return this.minecraft.getBlockRenderer()
            .getBlockModelShaper()
            .getParticleIcon(
                Blocks.NETHER_PORTAL.defaultBlockState()
            );
    }

    @Unique
    private void isCustomPortal(LocalPlayer player) {
        PortalProcessor portalManager = player.portalProcess;
        Portal portalBlock = portalManager != null && portalManager.isInsidePortalThisTick()
            ? ((PortalManagerAccessor) portalManager).getPortal()
            : null;
        BlockPos portalPos = portalManager != null && portalManager.isInsidePortalThisTick()
            ? ((PortalManagerAccessor) portalManager).getEntryPosition()
            : null;

        if (portalBlock == null) {
            return;
        }

        if (portalBlock instanceof CustomPortalBlock customportalblock) {
            PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(customportalblock.getPortalBase(player.clientLevel, portalPos));
            if (link != null) {
                lastColor = link.color;
                return;
            }
        }

        lastColor = -1;
    }
}