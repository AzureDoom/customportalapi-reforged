package net.kyrptonaught.customportalapi.util;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.portal.linking.DimensionalBlockPos;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CustomTeleporter {

    private CustomTeleporter() {}

    @Nullable
    public static TeleportTransition createTeleportTarget(Level level, Entity entity, Block portalBase, BlockPos portalPos) {
        PortalLink link = CustomPortalsMod.getPortalLinkFromBase(portalBase);
        if (link == null) {
            return null;
        }
        if (!link.getPreTeleportEvent().apply(entity)) {
            return null;
        }

        ResourceKey<Level> destinationKey = level.dimension() == CustomPortalsMod.dimensions.get(link.targetDimensionLocation)
                ? CustomPortalsMod.dimensions.get(link.returnDimensionLocation)
                : CustomPortalsMod.dimensions.get(link.targetDimensionLocation);

        ServerLevel destination = ((ServerLevel) level).getServer().getLevel(destinationKey);
        if (destination == null) {
            return null;
        }
        if (!entity.canUsePortal(false)) {
            return null;
        }

        return customTPTarget(destination, entity, portalPos, portalBase, link.getFrameTester());
    }

    @Nullable
    private static TeleportTransition customTPTarget(ServerLevel destinationLevel, Entity entity, BlockPos enteredPortalPos, Block frameBlock, PortalFrameTester frameTester) {
        if (CustomPortalsMod.portalLinkingStorage == null) {
            return null;
        }

        Direction.Axis portalAxis = CustomPortalHelper.getAxisFrom(entity.level().getBlockState(enteredPortalPos));
        BlockUtil.FoundRectangle fromPortalRectangle = frameTester
            .init(entity.level(), enteredPortalPos, portalAxis, frameBlock)
            .getRectangle();

        if (fromPortalRectangle == null) {
            return null;
        }

        DimensionalBlockPos destinationPos = CustomPortalsMod.portalLinkingStorage.getDestination(fromPortalRectangle.minCorner, entity.level().dimension());

        if (destinationPos != null && destinationPos.dimension().equals(destinationLevel.dimension().location())) {
            PortalFrameTester portalFrameTester = frameTester.init(destinationLevel, destinationPos.pos(), portalAxis, frameBlock);
            if (portalFrameTester.isValidFrame() && portalFrameTester.getRectangle() != null) {
                if (!portalFrameTester.isAlreadyLitPortalFrame()) {
                    portalFrameTester.lightPortal(frameBlock);
                }

                PortalLink link = CustomPortalsMod.getPortalLinkFromBase(frameBlock);

                if (link == null) {
                    return null;
                }

                return portalFrameTester.getTPTargetInPortal(destinationLevel,
                        portalFrameTester.getRectangle(),
                        portalAxis,
                        portalFrameTester.getEntityOffsetInPortal(fromPortalRectangle, entity, portalAxis),
                        entity,
                        link);
            }
        }

        return createDestinationPortal(destinationLevel, entity, portalAxis, fromPortalRectangle, frameBlock.defaultBlockState());
    }

    @Nullable
    public static TeleportTransition createDestinationPortal(ServerLevel destination, Entity entity, Direction.Axis axis, BlockUtil.FoundRectangle portalFramePos, BlockState frameBlock) {
        if (CustomPortalsMod.portalLinkingStorage == null) {
            return null;
        }

        WorldBorder worldBorder = destination.getWorldBorder();
        double xMin = Math.max(-2.9999872E7D, worldBorder.getMinX() + 16.0D);
        double zMin = Math.max(-2.9999872E7D, worldBorder.getMinZ() + 16.0D);
        double xMax = Math.min(2.9999872E7D, worldBorder.getMaxX() - 16.0D);
        double zMax = Math.min(2.9999872E7D, worldBorder.getMaxZ() - 16.0D);
        double scaleFactor = DimensionType.getTeleportationScale(entity.level().dimensionType(), destination.dimensionType());
        BlockPos blockPos = BlockPos.containing(Mth.clamp(entity.getX() * scaleFactor, xMin, xMax),
                entity.getY(),
                Mth.clamp(entity.getZ() * scaleFactor, zMin, zMax));

        Optional<BlockUtil.FoundRectangle> portal = PortalPlacer.createDestinationPortal(destination, blockPos, frameBlock, axis);
        if (portal.isPresent()) {
            PortalLink link = CustomPortalsMod.getPortalLinkFromBase(frameBlock.getBlock());

            if (link == null) {
                return null;
            }

            PortalFrameTester portalFrameTester = link.getFrameTester();

            CustomPortalsMod.portalLinkingStorage.createLink(portalFramePos.minCorner, entity.level().dimension(), portal.get().minCorner, destination.dimension());
            return portalFrameTester.getTPTargetInPortal(
                destination,
                portal.get(),
                axis,
                portalFrameTester.getEntityOffsetInPortal(portalFramePos, entity, axis),
                entity,
                link
            );
        }

        return failedToFindValidTarget(destination, entity, blockPos);
    }

    protected static TeleportTransition failedToFindValidTarget(ServerLevel level, Entity entity, BlockPos pos) {
        CustomPortalsMod.LOGGER.error("Unable to find a valid teleport location, forced to place on top of world");
        BlockPos destinationPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos);
        return new TeleportTransition(level,
            new Vec3(destinationPos.getX() + .5, destinationPos.getY(), destinationPos.getZ() + .5),
            entity.getDeltaMovement(),
            entity.getYRot(),
            entity.getXRot(),
            TeleportTransition.DO_NOTHING);
    }
}