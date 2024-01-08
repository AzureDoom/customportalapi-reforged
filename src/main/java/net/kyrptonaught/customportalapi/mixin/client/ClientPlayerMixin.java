package net.kyrptonaught.customportalapi.mixin.client;

import com.mojang.authlib.GameProfile;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.interfaces.ClientPlayerInColoredPortal;
import net.kyrptonaught.customportalapi.interfaces.EntityInCustomPortal;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(LocalPlayer.class)
public abstract class ClientPlayerMixin extends Player implements EntityInCustomPortal, ClientPlayerInColoredPortal {

    @Shadow
    public float spinningEffectIntensity;

    @Shadow
    public float oSpinningEffectIntensity;

    @Shadow
    @Final
    protected Minecraft minecraft;
    int portalColor;

    public ClientPlayerMixin(Level world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow
    public abstract void closeContainer();

    @Override
    public int getLastUsedPortalColor() {
        return portalColor;
    }

    @Override
    public void setLastUsedPortalColor(int color) {
        this.portalColor = color;

    }

    @Inject(method = "handleNetherPortalClient", at = @At(value = "HEAD"), cancellable = true)
    public void injectCustomNausea(CallbackInfo ci) {
        if (this.isInsidePortal) {
            setLastUsedPortalColor(-1);
        } else if (this.getTimeInPortal() > 0) {
            int previousColor = getLastUsedPortalColor();
            PortalLink link = this.getInPortalPos() != null ? CustomPortalApiRegistry.getPortalLinkFromBase(
                    CustomPortalHelper.getPortalBase(this.level(), this.getInPortalPos())) : null;
            if (link != null) setLastUsedPortalColor(link.colorID);
            updateCustomNausea(previousColor);
            ci.cancel();
        }
    }

    @Unique
    private void updateCustomNausea(int previousColor) {
        this.oSpinningEffectIntensity = this.spinningEffectIntensity;
        float f = 0.0F;
        if (this.isInsidePortal) {
            if (this.minecraft.screen != null && !this.minecraft.screen.isPauseScreen() && !(this.minecraft.screen instanceof DeathScreen)) {
                if (this.minecraft.screen instanceof AbstractContainerScreen) {
                    this.closeContainer();
                }

                this.minecraft.setScreen(null);
            }

            if (this.oSpinningEffectIntensity == 0.0F && previousColor != -999) { // previous color prevents this from playing after a teleport. A tp sets the previousColor to -999
                PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(
                        CustomPortalHelper.getPortalBase(level(), getInPortalPos()));
                if (link != null && link.getInPortalAmbienceEvent().hasEvent()) {
                    this.minecraft.getSoundManager().play(link.getInPortalAmbienceEvent().execute(this).getInstance());
                } else
                    this.minecraft.getSoundManager().play(
                            SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRIGGER,
                                    this.random.nextFloat() * 0.4F + 0.8F, 0.25F));
            }

            f = 0.0125F;
            this.isInsidePortal = false;
        } else if (this.hasEffect(MobEffects.CONFUSION) && !this.getEffect(MobEffects.CONFUSION).endsWithin(60)) {
            f = 0.006666667F;
        } else if (this.spinningEffectIntensity > 0.0F) {
            f = -0.05F;
        }

        this.spinningEffectIntensity = Mth.clamp(this.spinningEffectIntensity + f, 0.0F, 1.0F);
        this.processPortalCooldown();
    }
}