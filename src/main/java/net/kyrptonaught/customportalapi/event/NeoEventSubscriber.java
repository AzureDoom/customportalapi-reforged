package net.kyrptonaught.customportalapi.event;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.kyrptonaught.customportalapi.portal.linking.PortalLinkingStorage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid = CustomPortalsMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class NeoEventSubscriber {

	@SubscribeEvent
	public static void onServerStart(ServerStartedEvent event) {
		for (ResourceKey<Level> registryKey : event.getServer().levelKeys()) {
			CustomPortalsMod.dimensions.put(registryKey.location(), registryKey);
		}

		CustomPortalsMod.portalLinkingStorage = event.getServer()
				.overworld()
				.getDataStorage()
				.computeIfAbsent(PortalLinkingStorage.TYPE);
	}

	@SubscribeEvent
	public static void rightClickItemEvent(PlayerInteractEvent.RightClickItem event) {
		Player player = event.getEntity();
		Level level = event.getLevel();
		InteractionHand hand = event.getHand();
		ItemStack stack = player.getItemInHand(hand);

		if (!level.isClientSide()) {
			Item item = stack.getItem();
			if (PortalIgnitionSource.isRegisteredIgnitionSourceWith(item)) {
				HitResult hit = player.pick(6, 1, false);
				if (hit.getType() == HitResult.Type.BLOCK) {
					BlockHitResult blockHit = (BlockHitResult) hit;
					if (!PortalPlacer.attemptPortalLight(level, blockHit.getBlockPos().relative(blockHit.getDirection()), PortalIgnitionSource.fromItem(item)))
						event.setCanceled(true);
				}
			}
		}
	}
}