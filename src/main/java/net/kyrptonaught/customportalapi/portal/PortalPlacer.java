package net.kyrptonaught.customportalapi.portal;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.BlockUtil.FoundRectangle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;

import java.util.Optional;

public class PortalPlacer {

    public static boolean attemptPortalLight(Level level, BlockPos portalPos, PortalIgnitionSource ignitionSource) {
        return attemptPortalLight(level, portalPos, CustomPortalHelper.getClosestFrameBlock(level, portalPos), ignitionSource);
    }

    public static boolean attemptPortalLight(Level level, BlockPos portalPos, BlockPos framePos, PortalIgnitionSource ignitionSource) {
        Block foundationBlock = level.getBlockState(framePos).getBlock();
        PortalLink link = CustomPortalsMod.getPortalLinkFromBase(foundationBlock);

        if (link == null || !link.doesIgnitionMatch(ignitionSource) || !link.canLightInDim(level.dimension().location())) {
            return false;
        }

        return createPortal(link, level, portalPos, foundationBlock);
    }

    private static boolean createPortal(PortalLink link, Level level, BlockPos pos, Block foundationBlock) {
        Optional<PortalFrameTester> optional = link.getFrameTester().getNewPortal(level, pos, Direction.Axis.X, foundationBlock);

        // Check for valid frame and correct size (if applicable)
        if (optional.isPresent()) {
            if (optional.get().isRequestedSize(link.strictWidth, link.strictHeight))
                optional.get().lightPortal(foundationBlock);
            return true;
        }
        return false;
    }

    public static Optional<FoundRectangle> createDestinationPortal(ServerLevel serverLevel, BlockPos blockPos, BlockState frameBlock, Direction.Axis axis) {
        WorldBorder worldBorder = serverLevel.getWorldBorder();
        PortalLink link = CustomPortalsMod.getPortalLinkFromBase(frameBlock.getBlock());

        if (link == null) {
            return Optional.empty();
        }

        PortalFrameTester portalFrameTester = link.getFrameTester();

        int topY = Math.min(serverLevel.getMaxY(), serverLevel.getMinY() + serverLevel.getLogicalHeight()) - 5;
        int bottomY = serverLevel.getMinY() + 5;

        if (serverLevel.dimension().location().equals(link.targetDimensionLocation)) {
            if (link.portalSearchYTop == Integer.MIN_VALUE) {
                topY = link.portalSearchYTop;
            }
            if (link.portalSearchYBottom == Integer.MIN_VALUE) {
                bottomY = link.portalSearchYBottom;
            }
        } else {
            if (link.returnPortalSearchYTop == Integer.MIN_VALUE) {
                topY = link.returnPortalSearchYTop;
            }
            if (link.returnPortalSearchYBottom == Integer.MIN_VALUE) {
                bottomY = link.returnPortalSearchYBottom;
            }
        }

        for (BlockPos.MutableBlockPos mutable : BlockPos.spiralAround(blockPos, 32, Direction.WEST, Direction.SOUTH)) {
            BlockPos testingPos = mutable.immutable();
            if (!worldBorder.isWithinBounds(testingPos)) {
                continue;
            }

            for (int y = topY; y >= bottomY; y--) {
                if (canHoldPortal(serverLevel.getBlockState(testingPos.atY(y)))) {
                    BlockPos testRect = portalFrameTester.doesPortalFitAt(serverLevel, testingPos.atY(y + 1), axis);
                    if (testRect != null) {
                        portalFrameTester.createPortal(serverLevel, testRect, frameBlock, axis);
                        return Optional.ofNullable(portalFrameTester.getRectangle());
                    }
                }
            }
        }

        portalFrameTester.createPortal(serverLevel, blockPos, frameBlock, axis);
        return Optional.ofNullable(portalFrameTester.getRectangle());
    }

    private static boolean canHoldPortal(BlockState state) {
        return state.isSolid();
    }
}