package net.kyrptonaught.customportalapi.portal.frame;

import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;

public abstract class PortalFrameTester {
    protected HashSet<Block> VALID_FRAME = null;
    protected int foundPortalBlocks;
    public BlockPos lowerCorner;
    protected LevelAccessor world;

    public abstract PortalFrameTester init(LevelAccessor world, BlockPos blockPos, Axis axis, Block... foundations);

    public abstract Optional<PortalFrameTester> getNewPortal(LevelAccessor worldAccess, BlockPos blockPos, Axis axis, Block... foundations);

    public abstract Optional<PortalFrameTester> getOrEmpty(LevelAccessor worldAccess, BlockPos blockPos, Predicate<PortalFrameTester> predicate, Direction.Axis axis, Block... foundations);

    public abstract boolean isAlreadyLitPortalFrame();

    public abstract boolean isValidFrame();

    public abstract void lightPortal(Block frameBlock);

    public abstract void createPortal(Level world, BlockPos pos, BlockState frameBlock, Direction.Axis axis);

    public abstract boolean isRequestedSize(int attemptWidth, int attemptHeight);

    public abstract BlockUtil.FoundRectangle getRectangle();

    public abstract Direction.Axis getAxis1();

    public abstract Direction.Axis getAxis2();

    public abstract BlockPos doesPortalFitAt(Level world, BlockPos attemptPos, Direction.Axis axis);

    public abstract Vec3 getEntityOffsetInPortal(BlockUtil.FoundRectangle arg, Entity entity, Direction.Axis portalAxis);

    public abstract PortalInfo getTPTargetInPortal(BlockUtil.FoundRectangle portalRect, Direction.Axis portalAxis, Vec3 prevOffset, Entity entity);

    protected BlockPos getLowerCorner(BlockPos blockPos, Direction.Axis axis1, Direction.Axis axis2) {
        if (!validStateInsidePortal(world.getBlockState(blockPos), VALID_FRAME))
            return null;
        return getLimitForAxis(getLimitForAxis(blockPos, axis1), axis2);
    }

    protected BlockPos getLimitForAxis(BlockPos blockPos, Direction.Axis axis) {
        if (blockPos == null || axis == null) return null;
        int offset = 1;
        while (validStateInsidePortal(world.getBlockState(blockPos.relative(axis, -offset)), VALID_FRAME)) {
            offset++;
            if (offset > 20) return null;
            if ((axis.equals(Direction.Axis.Y) && blockPos.getY() - offset < world.getMinBuildHeight()) ||
                    (!axis.equals(Direction.Axis.Y) && !world.getWorldBorder().isWithinBounds(blockPos.relative(axis, -offset))))
                return null;
        }
        return blockPos.relative(axis, -(offset - 1));
    }

    protected int getSize(Direction.Axis axis, int minSize, int maxSize) {
        for (int i = 1; i <= maxSize; i++) {
            BlockState blockState = this.world.getBlockState(this.lowerCorner.relative(axis, i));
            if (!validStateInsidePortal(blockState, VALID_FRAME)) {
                if (VALID_FRAME.contains(blockState.getBlock())) {
                    return i >= minSize ? i : 0;

                }
                break;
            }
        }
        return 0;
    }

    protected boolean checkForValidFrame(Direction.Axis axis1, Direction.Axis axis2, int size1, int size2) {
        BlockPos checkPos = lowerCorner.mutable();
        for (int i = 0; i < size1; i++) {
            if (!VALID_FRAME.contains(world.getBlockState(checkPos.relative(axis2, -1)).getBlock()) || !VALID_FRAME.contains(world.getBlockState(checkPos.relative(axis2, size2)).getBlock()))
                return false;
            checkPos = checkPos.relative(axis1, 1);
        }
        checkPos = lowerCorner.mutable();
        for (int i = 0; i < size2; i++) {
            if (!VALID_FRAME.contains(world.getBlockState(checkPos.relative(axis1, -1)).getBlock()) || !VALID_FRAME.contains(world.getBlockState(checkPos.relative(axis1, size1)).getBlock()))
                return false;
            checkPos = checkPos.relative(axis2, 1);
        }
        return true;
    }

    protected void countExistingPortalBlocks(Direction.Axis axis1, Direction.Axis axis2, int size1, int size2) {
        for (int i = 0; i < size1; i++)
            for (int j = 0; j < size2; j++)
                if (CustomPortalHelper.isInstanceOfCustomPortal(world.getBlockState(this.lowerCorner.relative(axis1, i).relative(axis2, j))))
                    foundPortalBlocks++;
    }

    public static boolean validStateInsidePortal(BlockState blockState, HashSet<Block> foundations) {
        PortalIgnitionSource ignitionSource = PortalIgnitionSource.FIRE;
        for (Block block : foundations) {
            PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
            if (link != null) {
                ignitionSource = link.portalIgnitionSource;
                break;
            }
        }
        if (blockState.isAir() || CustomPortalHelper.isInstanceOfCustomPortal(blockState))
            return true;
        if (ignitionSource == PortalIgnitionSource.FIRE)
            return blockState.is(BlockTags.FIRE);
        if (ignitionSource.isWater())
            return blockState.getFluidState().is(FluidTags.WATER);
        if (ignitionSource.isLava())
            return blockState.getFluidState().is(FluidTags.LAVA);
        if (ignitionSource.sourceType == PortalIgnitionSource.SourceType.FLUID) {
            return BuiltInRegistries.FLUID.getKey(blockState.getFluidState().getType()) == ignitionSource.ignitionSourceID;
        }
        return false;
    }

    @FunctionalInterface
    public interface PortalFrameTesterFactory {
        PortalFrameTester createInstanceOfPortalFrameTester();
    }
}
