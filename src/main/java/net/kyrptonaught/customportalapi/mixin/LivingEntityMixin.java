package net.kyrptonaught.customportalapi.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin {
}
