package net.krptonaught.customportalapi;

import net.kyrptonaught.customportalapi.CustomPortalRegistrationEvent;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = CustomPortalsMod.MOD_ID)
public class TestMod {

	@SubscribeEvent
	public static void createPortals(CustomPortalRegistrationEvent event) {
		CustomPortalBuilder builder = new CustomPortalBuilder()
						.frame(Blocks.GLOWSTONE)
						.destination(ResourceLocation.withDefaultNamespace("the_nether"))
						.lightWithFluid(Fluids.WATER)
						.tintColor(255, 0, 255);

		event.register(builder);

		builder = new CustomPortalBuilder()
				.frame(Blocks.DIAMOND_BLOCK)
				.destination(ResourceLocation.withDefaultNamespace("the_end"))
				.flatPortal()
				.lightWithItem(Items.DIAMOND)
				.tintColor(0, 255, 255)
				.preTeleportEvent(entity -> entity.getWeaponItem() != null && !entity.getWeaponItem().is(Items.NETHERITE_BLOCK))
				.postTeleportEvent(entity -> CustomPortalsMod.LOGGER.info("Teleported entity: {}", entity.getName().getString()))
				.travelSound(SoundEvents.LIGHTNING_BOLT_THUNDER.location(), (entity) -> entity.getRandom().nextFloat() * 0.4F + 0.8F, (entity) -> 1.0f)
				.triggerSound(SoundEvents.WATER_AMBIENT.location(), (entity) -> entity.getRandom().nextFloat() * 0.4F + 0.8F, (entity) -> 1.0f)
				.ambientSound(SoundEvents.AMETHYST_BLOCK_CHIME.location(), (level -> 1.0f), (level) -> level.random.nextFloat() * 0.4F + 0.8F)
				.portalParticle((level, pos) -> ParticleTypes.CRIT);

		event.register(builder);
	}
}