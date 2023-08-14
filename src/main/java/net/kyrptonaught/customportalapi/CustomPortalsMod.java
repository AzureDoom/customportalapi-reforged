package net.kyrptonaught.customportalapi;

import static net.kyrptonaught.customportalapi.CustomPortalsMod.MOD_ID;

import java.util.HashMap;

import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.kyrptonaught.customportalapi.init.ParticleInit;
import net.kyrptonaught.customportalapi.networking.NetworkManager;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.kyrptonaught.customportalapi.portal.frame.FlatPortalAreaHelper;
import net.kyrptonaught.customportalapi.portal.frame.VanillaPortalAreaHelper;
import net.kyrptonaught.customportalapi.portal.linking.PortalLinkingStorage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomPortalsMod {
	public static final String MOD_ID = "cpapireforged";

	public static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
	
    public static final RegistryObject<CustomPortalBlock> portalBlock = BLOCKS.register("custom_portal_block", () -> new CustomPortalBlock(Block.Properties.copy(Blocks.NETHER_PORTAL).noCollission().strength(-1).sound(SoundType.GLASS).lightLevel(state -> 11)));
	public static HashMap<ResourceLocation, ResourceKey<Level>> dims = new HashMap<>();
	public static ResourceLocation VANILLAPORTAL_FRAMETESTER = new ResourceLocation(MOD_ID, "vanillanether");
	public static ResourceLocation FLATPORTAL_FRAMETESTER = new ResourceLocation(MOD_ID, "flat");
	public static PortalLinkingStorage portalLinkingStorage;

	public CustomPortalsMod() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

		BLOCKS.register(bus);

		ParticleInit.PARTICLES.register(bus);
		onInitialize(bus);
	}

	private void onServerStart(ServerStartedEvent event) {
		for (ResourceKey<Level> registryKey : event.getServer().levelKeys())
			dims.put(registryKey.location(), registryKey);
		portalLinkingStorage = (PortalLinkingStorage) event.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(PortalLinkingStorage::fromNbt, PortalLinkingStorage::new, MOD_ID);
	}

	private void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		var player = event.getEntity();
		var world = event.getLevel();
		var hand = event.getHand();
		var stack = player.getItemInHand(hand);

		if (!world.isClientSide()) {
			var item = stack.getItem();
			if (PortalIgnitionSource.isRegisteredIgnitionSourceWith(item)) {
				var hit = player.pick(6, 1, false);
				if (hit.getType() == HitResult.Type.BLOCK) {
					var blockHit = (BlockHitResult) hit;
					if (PortalPlacer.attemptPortalLight(world, blockHit.getBlockPos().relative(blockHit.getDirection()), PortalIgnitionSource.ItemUseSource(item)))
						event.setResult(Event.Result.ALLOW);
				}
			}
		}
	}

	public void onInitialize(IEventBus bus) {
		MinecraftForge.EVENT_BUS.addListener(this::onServerStart);
		CustomPortalApiRegistry.registerPortalFrameTester(VANILLAPORTAL_FRAMETESTER, VanillaPortalAreaHelper::new);
		CustomPortalApiRegistry.registerPortalFrameTester(FLATPORTAL_FRAMETESTER, FlatPortalAreaHelper::new);
		MinecraftForge.EVENT_BUS.addListener(this::onRightClickItem);
	}

	public static void logError(String message) {
		System.out.println("[" + MOD_ID + "]ERROR: " + message);
	}

	public static CustomPortalBlock getDefaultPortalBlock() {
		return portalBlock.get();
	}

	@SubscribeEvent
	public static void onCommonStartUp(FMLCommonSetupEvent event) {
		NetworkManager.register();
		CustomPortalBuilder.beginPortal().frameBlock(Blocks.GLOWSTONE).destDimID(new ResourceLocation("the_nether")).lightWithWater().tintColor(46, 5, 25).registerPortal();
	}
}