package net.kyrptonaught.customportalapi.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntityMixin {
}
