package net.kyrptonaught.customportalapi.portal.frame;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.BlockUtil;
import net.minecraft.BlockUtil.FoundRectangle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class PortalFrameTester {

    @Nullable
    public BlockPos lowerCorner;
    protected HashSet<Block> VALID_FRAME = new HashSet<>();
    protected int foundPortalBlocks;
    protected LevelAccessor levelAccessor;

    public static boolean validStateInsidePortal(BlockState blockState, HashSet<Block> foundations) {
        PortalIgnitionSource ignitionSource = PortalIgnitionSource.FIRE;
        for (Block block : foundations) {
            PortalLink link = CustomPortalsMod.getPortalLinkFromBase(block);
            if (link != null) {
                ignitionSource = link.ignitionSource;
                break;
            }
        }

        if (blockState.isAir() || CustomPortalHelper.isInstanceOfCustomPortal(blockState)) {
            return true;
        }
        if (ignitionSource == PortalIgnitionSource.FIRE) {
            return blockState.is(BlockTags.FIRE);
        }
        if (ignitionSource.isWater()) {
            return blockState.getFluidState().is(FluidTags.WATER);
        }
        if (ignitionSource.isLava()) {
            return blockState.getFluidState().is(FluidTags.LAVA);
        }
        if (ignitionSource.sourceType == PortalIgnitionSource.SourceType.FLUID) {
            return BuiltInRegistries.FLUID.getKey(blockState.getFluidState().getType()) == ignitionSource.ignitionSourceID;
        }

        return false;
    }

    public abstract PortalFrameTester init(LevelAccessor level, BlockPos blockPos, Axis axis, Block... foundations);

    public abstract Optional<PortalFrameTester> getNewPortal(LevelAccessor level, BlockPos blockPos, Axis axis, Block... foundations);

    public abstract Optional<PortalFrameTester> getOrEmpty(LevelAccessor level, BlockPos blockPos, Predicate<PortalFrameTester> predicate, Direction.Axis axis, Block... foundations);

    public abstract boolean isAlreadyLitPortalFrame();

    public abstract boolean isValidFrame();

    public abstract void lightPortal(Block frameBlock);

    public abstract void createPortal(Level level, BlockPos pos, BlockState frameBlock, Direction.Axis axis);

    public abstract boolean isRequestedSize(int attemptWidth, int attemptHeight);

    @Nullable
    public abstract BlockUtil.FoundRectangle getRectangle();

    @Nullable
    public abstract BlockPos doesPortalFitAt(Level level, BlockPos attemptPos, Direction.Axis axis);

    public abstract Vec3 getEntityOffsetInPortal(BlockUtil.FoundRectangle arg, Entity entity, Direction.Axis portalAxis);

    public abstract TeleportTransition getTPTargetInPortal(ServerLevel serverLevel, FoundRectangle portalRect, Axis portalAxis, Vec3 prevOffset, Entity entity, PortalLink link);

    @Nullable
    protected BlockPos getLowerCorner(BlockPos blockPos, Direction.Axis axis1, Direction.Axis axis2) {
        if (!validStateInsidePortal(levelAccessor.getBlockState(blockPos), VALID_FRAME)) {
            return null;
        }

        return getLimitForAxis(getLimitForAxis(blockPos, axis1), axis2);
    }

    @Nullable
    protected BlockPos getLimitForAxis(@Nullable BlockPos blockPos, @Nullable Direction.Axis axis) {
        if (blockPos == null || axis == null) {
            return null;
        }

        int offset = 1;
        while (validStateInsidePortal(levelAccessor.getBlockState(blockPos.relative(axis, -offset)), VALID_FRAME)) {
            offset++;
            if (offset > 20)
                return null;
            if ((axis.equals(Direction.Axis.Y) && blockPos.getY() - offset < levelAccessor.getMinY())
                    || (!axis.equals(Direction.Axis.Y)
                    && !levelAccessor.getWorldBorder().isWithinBounds(blockPos.relative(axis, -offset)))) {

                return null;
            }
        }

        return blockPos.relative(axis, -(offset - 1));
    }

    protected int getSize(Direction.Axis axis, int minSize, int maxSize) {
        if (lowerCorner == null) {
            return 0;
        }

        for (int i = 1; i <= maxSize; i++) {
            BlockState blockState = levelAccessor.getBlockState(lowerCorner.relative(axis, i));
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
        if (lowerCorner == null) {
            return false;
        }

        BlockPos checkPos = lowerCorner.mutable();
        if (frameContainsBlock(axis1, axis2, size1, size2, checkPos)) {
            return false;
        }

        checkPos = lowerCorner.mutable();
	    return !frameContainsBlock(axis2, axis1, size2, size1, checkPos);
    }

    private boolean frameContainsBlock(Axis axis1, Axis axis2, int size1, int size2, BlockPos checkPos) {
        for (int i = 0; i < size1; i++) {
            if (!VALID_FRAME.contains(levelAccessor.getBlockState(checkPos.relative(axis2, -1)).getBlock())
                    || !VALID_FRAME.contains(levelAccessor.getBlockState(checkPos.relative(axis2, size2)).getBlock())) {

                return true;
            }

            checkPos = checkPos.relative(axis1, 1);
        }

        return false;
    }

    protected void countExistingPortalBlocks(Direction.Axis axis1, Direction.Axis axis2, int size1, int size2) {
        if (lowerCorner == null) {
            return;
        }

        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size2; j++) {
                if (CustomPortalHelper.isInstanceOfCustomPortal(levelAccessor.getBlockState(lowerCorner.relative(axis1, i).relative(axis2, j)))) {
                    foundPortalBlocks++;
                }
            }
        }
    }
}