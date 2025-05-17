package net.kyrptonaught.customportalapi;

import com.mojang.logging.LogUtils;
import net.kyrptonaught.customportalapi.portal.linking.PortalLinkingStorage;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Mod(CustomPortalsMod.MOD_ID)
public class CustomPortalsMod {

    public static final String MOD_ID = "cpapireforged";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final Supplier<CustomPortalBlock> CUSTOM_PORTAL_BLOCK = BLOCKS.registerBlock("custom_portal_block",
        (properties) -> new CustomPortalBlock(properties
                .noCollission()
                .randomTicks()
                .strength(-1)
                .sound(
                    SoundType.GLASS
                )
                .lightLevel(state -> 11)
        )
    );

    public static final HashMap<ResourceLocation, ResourceKey<Level>> dimensions = new HashMap<>();
    public static final ConcurrentHashMap<Block, PortalLink> PORTALS = new ConcurrentHashMap<>();
    @Nullable
    public static PortalLinkingStorage portalLinkingStorage;

    public CustomPortalsMod(IEventBus bus) {
        BLOCKS.register(bus);
        bus.addListener(this::onCommonStartUp);
    }

    @Nullable
    public static PortalLink getPortalLinkFromBase(Block baseBlock) {
        if (PORTALS.containsKey(baseBlock)) {
            return PORTALS.get(baseBlock);
        }

        return null;
    }

    public static boolean isRegisteredFrameBlock(BlockState blockState) {
        return PORTALS.containsKey(blockState.getBlock());
    }

    public static void addPortal(Block frameBlock, PortalLink link) {
	    if (!dimensions.isEmpty() && !dimensions.containsKey(link.targetDimensionLocation)) {
            throw new RuntimeException("Dimension not found");
        }

        if (PORTALS.containsKey(frameBlock) || frameBlock.equals(Blocks.OBSIDIAN)) {
            throw new RuntimeException("A portal using the '%s' frame is already registered!'".formatted(frameBlock.getName().getString()));
        } else {
            PORTALS.put(frameBlock, link);
        }
    }

    public void onCommonStartUp(FMLCommonSetupEvent event) {
        ModLoader.postEvent(new CustomPortalRegistrationEvent());
    }
}