package net.kyrptonaught.customportalapi.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;

@FunctionalInterface
public interface PortalIgniteEvent {

    void afterLight(Player player, Level world, BlockPos portalPos, BlockPos framePos, PortalIgnitionSource portalIgnitionSource);
}
