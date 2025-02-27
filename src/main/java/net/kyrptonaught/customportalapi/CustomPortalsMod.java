package net.kyrptonaught.customportalapi;

import com.mojang.logging.LogUtils;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.kyrptonaught.customportalapi.portal.frame.FlatPortalAreaHelper;
import net.kyrptonaught.customportalapi.portal.frame.VanillaPortalAreaHelper;
import net.kyrptonaught.customportalapi.portal.linking.PortalLinkingStorage;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.function.Supplier;

@Mod(CustomPortalsMod.MOD_ID)
public class CustomPortalsMod {
    public static final String MOD_ID = "cpapireforged";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);

    public static final Supplier<CustomPortalBlock> portalBlock = BLOCKS.register("custom_portal_block",
            () -> new CustomPortalBlock(
                    Block.Properties.ofFullCopy(Blocks.NETHER_PORTAL).noCollission().strength(-1).sound(
                            SoundType.GLASS).lightLevel(state -> 11)));
    public static HashMap<ResourceLocation, ResourceKey<Level>> dims = new HashMap<>();
    public static ResourceLocation VANILLAPORTAL_FRAMETESTER = ResourceLocation.fromNamespaceAndPath(MOD_ID, "vanillanether");
    public static ResourceLocation FLATPORTAL_FRAMETESTER = ResourceLocation.fromNamespaceAndPath(MOD_ID, "flat");
    public static PortalLinkingStorage portalLinkingStorage;

    public CustomPortalsMod(IEventBus bus) {
        BLOCKS.register(bus);
        bus.addListener(this::onCommonStartUp);
        bus.addListener(this::createPortals);
        NeoForge.EVENT_BUS.addListener(this::onServerStart);
        CustomPortalApiRegistry.registerPortalFrameTester(VANILLAPORTAL_FRAMETESTER, VanillaPortalAreaHelper::new);
        CustomPortalApiRegistry.registerPortalFrameTester(FLATPORTAL_FRAMETESTER, FlatPortalAreaHelper::new);
        NeoForge.EVENT_BUS.addListener(this::onRightClickItem);
    }

    public static void logError(String message) {
        LOGGER.error(message);
    }

    public static CustomPortalBlock getDefaultPortalBlock() {
        return portalBlock.get();
    }

    public void onCommonStartUp(FMLCommonSetupEvent event) {
        ModLoader.postEvent(new CustomPortalRegistrationEvent());
    }

    public void createPortals(CustomPortalRegistrationEvent event) {
//        CustomPortalBuilder builder = CustomPortalBuilder.beginPortal().frameBlock(Blocks.GLOWSTONE).destDimID(ResourceLocation.withDefaultNamespace("the_nether")).lightWithWater().tintColor(46, 5, 25);
//        event.register(builder);
    }

    private void onServerStart(ServerStartedEvent event) {
        for (ResourceKey<Level> registryKey : event.getServer().levelKeys())
            dims.put(registryKey.location(), registryKey);
        portalLinkingStorage = event.getServer().overworld().getDataStorage().computeIfAbsent(
                PortalLinkingStorage.factory(), MOD_ID);
    }

    private void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level world = event.getLevel();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);

        if (!world.isClientSide()) {
            Item item = stack.getItem();
            if (PortalIgnitionSource.isRegisteredIgnitionSourceWith(item)) {
                HitResult hit = player.pick(6, 1, false);
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHit = (BlockHitResult) hit;
                    if (!PortalPlacer.attemptPortalLight(world,
                            blockHit.getBlockPos().relative(blockHit.getDirection()),
                            PortalIgnitionSource.ItemUseSource(item)))
                        event.setCanceled(true);
                }
            }
        }
    }
}