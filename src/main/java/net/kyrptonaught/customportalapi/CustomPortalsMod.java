package net.kyrptonaught.customportalapi;

import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.kyrptonaught.customportalapi.init.ParticleInit;
import net.kyrptonaught.customportalapi.networking.NetworkManager;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.kyrptonaught.customportalapi.portal.frame.FlatPortalAreaHelper;
import net.kyrptonaught.customportalapi.portal.frame.VanillaPortalAreaHelper;
import net.kyrptonaught.customportalapi.portal.linking.PortalLinkingStorage;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.function.Supplier;

import static net.kyrptonaught.customportalapi.CustomPortalsMod.MOD_ID;

@Mod(MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomPortalsMod {
    public static final String MOD_ID = "cpapireforged";

    public static DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MOD_ID);

    public static final Supplier<CustomPortalBlock> portalBlock = BLOCKS.register("custom_portal_block", () -> new CustomPortalBlock(Block.Properties.copy(Blocks.NETHER_PORTAL).noCollission().strength(-1).sound(SoundType.GLASS).lightLevel(state -> 11)));
    public static HashMap<ResourceLocation, ResourceKey<Level>> dims = new HashMap<>();
    public static ResourceLocation VANILLAPORTAL_FRAMETESTER = new ResourceLocation(MOD_ID, "vanillanether");
    public static ResourceLocation FLATPORTAL_FRAMETESTER = new ResourceLocation(MOD_ID, "flat");
    public static PortalLinkingStorage portalLinkingStorage;

    public CustomPortalsMod(IEventBus bus) {
        BLOCKS.register(bus);
        ParticleInit.PARTICLES.register(bus);
        onInitialize(bus);
    }

    private void onServerStart(ServerStartedEvent event) {
        for (ResourceKey<Level> registryKey : event.getServer().levelKeys())
            dims.put(registryKey.location(), registryKey);
        portalLinkingStorage = (PortalLinkingStorage) event.getServer().overworld().getDataStorage().computeIfAbsent(PortalLinkingStorage.factory(), MOD_ID);
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
        NeoForge.EVENT_BUS.addListener(this::onServerStart);
        CustomPortalApiRegistry.registerPortalFrameTester(VANILLAPORTAL_FRAMETESTER, VanillaPortalAreaHelper::new);
        CustomPortalApiRegistry.registerPortalFrameTester(FLATPORTAL_FRAMETESTER, FlatPortalAreaHelper::new);
        NeoForge.EVENT_BUS.addListener(this::onRightClickItem);
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
//		CustomPortalBuilder.beginPortal().frameBlock(Blocks.GLOWSTONE).destDimID(new ResourceLocation("the_nether")).lightWithWater().tintColor(46, 5, 25).registerPortal();
    }
}