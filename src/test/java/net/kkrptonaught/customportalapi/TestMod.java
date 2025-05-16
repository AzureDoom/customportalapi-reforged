package net.kkrptonaught.customportalapi;

import net.kyrptonaught.customportalapi.CustomPortalRegistrationEvent;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = CustomPortalsMod.MOD_ID)
public class TestMod {

	@SubscribeEvent
	public static void createPortals(CustomPortalRegistrationEvent event) {
		CustomPortalBuilder builder = CustomPortalBuilder.beginPortal()
						.frameBlock(Blocks.GLOWSTONE)
						.destDimID(ResourceLocation.withDefaultNamespace("the_nether"))
						.lightWithWater()
						.tintColor(255, 0, 255);

		event.register(builder);
	}
}