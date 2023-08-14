package net.kyrptonaught.customportalapi.portal;

import java.util.Optional;

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

public class PortalPlacer {
	public static boolean attemptPortalLight(Level world, BlockPos portalPos, PortalIgnitionSource ignitionSource) {
		return attemptPortalLight(world, portalPos, CustomPortalHelper.getClosestFrameBlock(world, portalPos), ignitionSource);
	}

	public static boolean attemptPortalLight(Level world, BlockPos portalPos, BlockPos framePos, PortalIgnitionSource ignitionSource) {
		Block foundationBlock = world.getBlockState(framePos).getBlock();
		PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(foundationBlock);

		if (link == null || !link.doesIgnitionMatch(ignitionSource) || !link.canLightInDim(world.dimension().location()))
			return false;
		return createPortal(link, world, portalPos, foundationBlock);
	}

	private static boolean createPortal(PortalLink link, Level world, BlockPos pos, Block foundationBlock) {
		Optional<PortalFrameTester> optional = link.getFrameTester().createInstanceOfPortalFrameTester().getNewPortal(world, pos, Direction.Axis.X, foundationBlock);
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
		for (BlockPos.MutableBlockPos mutable : BlockPos.spiralAround(blockPos, 16, Direction.WEST, Direction.SOUTH)) {
			BlockPos testingPos = mutable.immutable();
			if (!worldBorder.isWithinBounds(testingPos))
				continue;

			int solidY = Math.min(world.getMaxBuildHeight(), world.getMinBuildHeight() + world.getLogicalHeight()) - 5;
			BlockPos pos = null;
			while (solidY >= 3) {
				if (canHoldPortal(world.getBlockState(testingPos.atY(solidY)))) {
					BlockPos testRect = portalFrameTester.doesPortalFitAt(world, testingPos.atY(solidY + 1), axis);
					if (testRect != null) {
						pos = testRect;
						break;
					}
				}
				solidY--;
			}

			if (pos != null) {
				portalFrameTester.createPortal(world, pos, frameBlock, axis);
				return Optional.of(portalFrameTester.getRectangle());
			}
		}
		portalFrameTester.createPortal(world, blockPos, frameBlock, axis);
		return Optional.of(portalFrameTester.getRectangle());
	}

	private static boolean canHoldPortal(BlockState state) {
		return state.isSolid();
	}
}