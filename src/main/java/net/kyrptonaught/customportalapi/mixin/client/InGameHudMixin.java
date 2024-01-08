package net.kyrptonaught.customportalapi.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.interfaces.ClientPlayerInColoredPortal;
import net.kyrptonaught.customportalapi.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@OnlyIn(Dist.CLIENT)
@Mixin(Gui.class)
public class InGameHudMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Redirect(method = "renderPortalOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;setColor(FFFF)V", ordinal = 0))
    public void changeColor(GuiGraphics instance, float red, float green, float blue, float alpha) {
        int color = ((ClientPlayerInColoredPortal) minecraft.player).getLastUsedPortalColor();
        if (color >= 0) {
            float[] colors = ColorUtil.getColorForBlock(color);
            RenderSystem.setShaderColor(colors[0], colors[1], colors[2], alpha);
        } else
            RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    @Redirect(method = "renderPortalOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getParticleIcon(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    public TextureAtlasSprite renderCustomPortalOverlay(BlockModelShaper blockModels, BlockState blockState) {
        if (((ClientPlayerInColoredPortal) minecraft.player).getLastUsedPortalColor() >= 0) {
            return this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(CustomPortalsMod.portalBlock.get().defaultBlockState());
        }
        return this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
    }
}