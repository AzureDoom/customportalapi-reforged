package net.kyrptonaught.customportalapi.mixin.client;

import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.block.Portal;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

	@ModifyArg(method = "handlePortalTransitionEffect", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/sounds/SoundManager;play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V"))
	public SoundInstance playSound(SoundInstance original) {
		SoundInstance triggerSound = customportalapi_reforged$getTriggerSound((LocalPlayer) (Object) this);
		if (triggerSound != null) {
			return triggerSound;
		}

		return original;
	}

	@Unique
	@Nullable
	private SoundInstance customportalapi_reforged$getTriggerSound(LocalPlayer player) {
		PortalProcessor portalManager = player.portalProcess;
		Portal portalBlock = portalManager != null && portalManager.isInsidePortalThisTick()
				? ((PortalManagerAccessor) portalManager).getPortal()
				: null;
		BlockPos portalPos = portalManager != null && portalManager.isInsidePortalThisTick()
				? ((PortalManagerAccessor) portalManager).getEntryPosition()
				: null;

		if (portalBlock == null) {
			return null;
		}

		if (portalBlock instanceof CustomPortalBlock customportalblock && portalPos != null) {
			PortalLink link = CustomPortalsMod.getPortalLinkFromBase(customportalblock.getPortalBase(player.clientLevel, portalPos));
			if (link != null && link.triggerSoundLocation != null) {
				return SimpleSoundInstance.forLocalAmbience(BuiltInRegistries.SOUND_EVENT.get(link.triggerSoundLocation).orElseThrow().value(),
						link.triggerSoundVolume.apply(player),
						link.triggerSoundPitch.apply(player));
			}
		}

		return null;
	}
}