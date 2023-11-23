package net.kyrptonaught.customportalapi;

import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PerWorldPortals {
    private static final Set<Block> worldPortals = ConcurrentHashMap.newKeySet();

    public static void removeOldPortalsFromRegistry() {
        for (var block : worldPortals)
            CustomPortalApiRegistry.portals.remove(block);
        worldPortals.clear();
    }

    public static void registerWorldPortal(PortalLink portalLink) {
        if (!CustomPortalApiRegistry.portals.containsKey(BuiltInRegistries.BLOCK.get(portalLink.block))) {
            var blockId = BuiltInRegistries.BLOCK.get(portalLink.block);
            worldPortals.add(blockId);
            CustomPortalApiRegistry.addPortal(blockId, portalLink);
        }
    }
}
