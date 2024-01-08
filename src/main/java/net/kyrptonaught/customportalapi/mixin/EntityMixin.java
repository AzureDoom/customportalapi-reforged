package net.kyrptonaught.customportalapi.mixin;

import net.kyrptonaught.customportalapi.interfaces.CustomTeleportingEntity;
import net.kyrptonaught.customportalapi.interfaces.EntityInCustomPortal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityInCustomPortal, CustomTeleportingEntity {

    @Shadow
    public Level level;

    @Unique
    boolean didTP = false;

    @Unique
    int timeInPortal = 0, maxTimeInPortal = 80, cooldown = 0;

    @Unique
    private BlockPos inPortalPos;
    private PortalInfo customTPTarget;

    @Unique
    @Override
    public boolean didTeleport() {
        return didTP;
    }

    @Unique
    @Override
    public void setDidTP(boolean didTP) {
        this.didTP = didTP;
        if (didTP) {
            timeInPortal = maxTimeInPortal;
            cooldown = 10;
        } else {
            timeInPortal = 0;
            cooldown = 0;
        }
        // inPortalPos = null;
    }

    @Unique
    @Override
    public int getTimeInPortal() {
        return timeInPortal;
    }

    @Unique
    @Override
    public void tickInPortal(BlockPos portalPos) {
        cooldown = 10;
        inPortalPos = portalPos;
    }

    @Unique
    @Override
    public BlockPos getInPortalPos() {
        return inPortalPos;
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void CPAinCustomPortal(CallbackInfo ci) {
        if (cooldown > 0) {
            cooldown--;
            timeInPortal = Math.min(timeInPortal + 1, maxTimeInPortal);
            if (cooldown <= 0) {
                setDidTP(false);
            }
        }
    }

    @Unique
    @Override
    public PortalInfo getCustomTeleportTarget() {
        return customTPTarget;
    }

    @Unique
    @Override
    public void setCustomTeleportTarget(PortalInfo teleportTarget) {
        this.customTPTarget = teleportTarget;
    }

    @Inject(method = "findDimensionEntryPoint", at = @At("HEAD"), cancellable = true)
    public void CPAgetCustomTPTarget(ServerLevel destination, CallbackInfoReturnable<PortalInfo> cir) {
        if (this.didTeleport())
            cir.setReturnValue(getCustomTeleportTarget());
    }

    @Inject(method = "load", at = @At(value = "TAIL"))
    public void CPAreadCustomPortalFromTag(CompoundTag tag, CallbackInfo ci) {
        this.didTP = tag.getBoolean("cpadidTP");
        this.cooldown = tag.getInt("cpaCooldown");
    }

    @Inject(method = "saveWithoutId", at = @At(value = "RETURN"))
    public void CPAwriteCustomPortalToTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putBoolean("cpadidTP", didTP);
        cir.getReturnValue().putInt("cpaCooldown", cooldown);
    }
}