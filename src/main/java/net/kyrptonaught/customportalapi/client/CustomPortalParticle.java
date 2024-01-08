package net.kyrptonaught.customportalapi.client;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.util.ColorUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.BlockParticleOption;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class CustomPortalParticle extends PortalParticle {
    protected CustomPortalParticle(ClientLevel clientWorld, double d, double e, double f, double g, double h, double i) {
        super(clientWorld, d, e, f, g, h, i);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<BlockParticleOption> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(BlockParticleOption blockStateParticleEffect, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i) {
            var portalParticle = new CustomPortalParticle(clientWorld, d, e, f, g, h, i);
            portalParticle.pickSprite(this.spriteProvider);
            var block = blockStateParticleEffect.getState().getBlock();
            var link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
            if (link != null) {
                float[] rgb = ColorUtil.getColorForBlock(link.colorID);
                portalParticle.setColor(rgb[0], rgb[1], rgb[2]);
            }
            return portalParticle;
        }
    }
}