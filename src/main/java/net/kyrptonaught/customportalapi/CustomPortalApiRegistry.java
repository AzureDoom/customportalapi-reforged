package net.kyrptonaught.customportalapi;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.util.PortalLink;

public class CustomPortalApiRegistry {

    protected static final ConcurrentHashMap<Block, PortalLink> portals = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<ResourceLocation, PortalFrameTester.PortalFrameTesterFactory> PortalFrameTesters =
        new ConcurrentHashMap<>();

    private CustomPortalApiRegistry() {}

    public static PortalLink getPortalLinkFromBase(Block baseBlock) {
        if (baseBlock == null)
            return null;
        if (portals.containsKey(baseBlock))
            return portals.get(baseBlock);
        return null;
    }

    public static boolean isRegisteredFrameBlock(BlockState blockState) {
        return portals.containsKey(blockState.getBlock());
    }

    public static Collection<PortalLink> getAllPortalLinks() {
        return portals.values();
    }

    public static void registerPortalFrameTester(
        ResourceLocation frameTesterID,
        PortalFrameTester.PortalFrameTesterFactory createPortalFrameTester
    ) {
        PortalFrameTesters.put(frameTesterID, createPortalFrameTester);
    }

    public static PortalFrameTester.PortalFrameTesterFactory getPortalFrameTester(ResourceLocation frameTesterID) {
        return PortalFrameTesters.getOrDefault(frameTesterID, null);
    }

    public static void addPortal(Block frameBlock, PortalLink link) {
        if (frameBlock == null)
            throw new RuntimeException("Frame block must not be null");
        if (link.getPortalBlock() == null)
            throw new RuntimeException("Portal block must not be null");
        if (link.portalIgnitionSource == null)
            throw new RuntimeException("Portal ignition source must not be null");
        if (link.dimID == null)
            throw new RuntimeException("Dimension is null");
        if (!CustomPortalsMod.dims.isEmpty() && !CustomPortalsMod.dims.containsKey(link.dimID))
            throw new RuntimeException("Dimension not found");
        if (CustomPortalsMod.getDefaultPortalBlock() == null)
            throw new RuntimeException("Built-in CustomPortalBlock is null");

        if (portals.containsKey(frameBlock) || frameBlock.equals(Blocks.OBSIDIAN)) {
            throw new RuntimeException(
                "A portal of the frame '" + frameBlock + "' is already registered"
            );
        } else {
            portals.put(frameBlock, link);
        }
    }
}
