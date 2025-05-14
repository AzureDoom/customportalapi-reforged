package net.kyrptonaught.customportalapi;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.kyrptonaught.customportalapi.util.PortalLink;

public class PerWorldPortals {

    private static final Set<Block> worldPortals = ConcurrentHashMap.newKeySet();

    private PerWorldPortals() {}

    public static void removeOldPortalsFromRegistry() {
        for (Block block : worldPortals)
            CustomPortalApiRegistry.portals.remove(block);
        worldPortals.clear();
    }

    public static void registerWorldPortal(@NotNull PortalLink portalLink) {
        if (!CustomPortalApiRegistry.portals.containsKey(BuiltInRegistries.BLOCK.get(portalLink.block))) {
            Block blockId = BuiltInRegistries.BLOCK.get(portalLink.block);
            worldPortals.add(blockId);
            CustomPortalApiRegistry.addPortal(blockId, portalLink);
        }
    }
}
