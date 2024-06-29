package net.kyrptonaught.customportalapi.portal;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
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
    public static boolean attemptPortalLight(Level world, BlockPos portalPos, PortalIgnitionSource ignitionSource) {
        return attemptPortalLight(world, portalPos, CustomPortalHelper.getClosestFrameBlock(world, portalPos),
                ignitionSource);
    }

    public static boolean attemptPortalLight(Level world, BlockPos portalPos, BlockPos framePos, PortalIgnitionSource ignitionSource) {
        Block foundationBlock = world.getBlockState(framePos).getBlock();
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(foundationBlock);

        if (link == null || !link.doesIgnitionMatch(ignitionSource) || !link.canLightInDim(
                world.dimension().location()))
            return false;
        return createPortal(link, world, portalPos, foundationBlock);
    }

    private static boolean createPortal(PortalLink link, Level world, BlockPos pos, Block foundationBlock) {
        Optional<PortalFrameTester> optional = link.getFrameTester().createInstanceOfPortalFrameTester().getNewPortal(
                world, pos, Direction.Axis.X, foundationBlock);
        // is valid frame, and is correct size(if applicable)
        if (optional.isPresent()) {
            if (optional.get().isRequestedSize(link.forcedWidth, link.forcedHeight))
                optional.get().lightPortal(foundationBlock);
            return true;
        }
        return false;
    }

    public static Optional<FoundRectangle> createDestinationPortal(ServerLevel world, BlockPos blockPos, BlockState frameBlock, Direction.Axis axis) {
        WorldBorder worldBorder = world.getWorldBorder();
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(frameBlock.getBlock());
        PortalFrameTester portalFrameTester = link.getFrameTester().createInstanceOfPortalFrameTester();

        int topY = Math.min(world.getMaxBuildHeight(), world.getMinBuildHeight() + world.getLogicalHeight()) - 5;
        int bottomY = world.getMinBuildHeight() + 5;

        if (world.dimension().location().equals(link.dimID)) {
            if (link.portalSearchYTop != null)
                topY = link.portalSearchYTop;
            if (link.portalSearchYBottom != null)
                bottomY = link.portalSearchYBottom;
        } else {
            if (link.returnPortalSearchYTop != null)
                topY = link.returnPortalSearchYTop;
            if (link.returnPortalSearchYBottom != null)
                bottomY = link.returnPortalSearchYBottom;
        }


        for (BlockPos.MutableBlockPos mutable : BlockPos.spiralAround(blockPos, 32, Direction.WEST, Direction.SOUTH)) {
            BlockPos testingPos = mutable.immutable();
            if (!worldBorder.isWithinBounds(testingPos)) continue;

            for (int y = topY; y >= bottomY; y--) {
                if (canHoldPortal(world.getBlockState(testingPos.atY(y)))) {
                    BlockPos testRect = portalFrameTester.doesPortalFitAt(world, testingPos.atY(y + 1), axis);
                    if (testRect != null) {
                        portalFrameTester.createPortal(world, testRect, frameBlock, axis);
                        return Optional.of(portalFrameTester.getRectangle());
                    }
                }
            }
        }
        portalFrameTester.createPortal(world, blockPos, frameBlock, axis);
        return Optional.of(portalFrameTester.getRectangle());
    }

    private static boolean canHoldPortal(BlockState state) {
        return state.isSolid();
    }
}