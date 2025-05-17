package net.kyrptonaught.customportalapi.portal.frame;

import com.google.common.collect.Sets;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.BlockUtil;
import net.minecraft.BlockUtil.FoundRectangle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public class FlatPortalFrameTester extends PortalFrameTester {

    protected final int maxXSize = 21, maxZSize = 21;
    protected int xSize = -1, zSize = -1;

    public FlatPortalFrameTester init(LevelAccessor level, BlockPos blockPos, Direction.Axis axis, Block... foundations) {
        VALID_FRAME = Sets.newHashSet(foundations);
        levelAccessor = level;
        lowerCorner = getLowerCorner(blockPos, Direction.Axis.X, Direction.Axis.Z);
        foundPortalBlocks = 0;
        if (lowerCorner == null) {
            lowerCorner = blockPos;
            xSize = zSize = 1;
        } else {
            xSize = getSize(Direction.Axis.X, 2, maxXSize);
            if (xSize > 0) {
                zSize = getSize(Direction.Axis.Z, 2, maxZSize);
                if (checkForValidFrame(Direction.Axis.X, Direction.Axis.Z, xSize, zSize)) {
                    countExistingPortalBlocks(Direction.Axis.X, Direction.Axis.Z, xSize, zSize);
                } else {
                    lowerCorner = null;
                    xSize = zSize = 1;
                }
            }
        }

        return this;
    }

    public Optional<PortalFrameTester> getNewPortal(LevelAccessor level, BlockPos blockPos, Direction.Axis axis, Block... foundations) {
        return getOrEmpty(level, blockPos, areaHelper -> areaHelper.isValidFrame() && areaHelper.foundPortalBlocks == 0, axis, foundations);
    }

    public Optional<PortalFrameTester> getOrEmpty(LevelAccessor level, BlockPos blockPos, Predicate<PortalFrameTester> predicate, Direction.Axis axis, Block... foundations) {
        return Optional.of((PortalFrameTester) new FlatPortalFrameTester()
                .init(level, blockPos, axis, foundations))
                .filter(predicate);
    }

    public boolean isAlreadyLitPortalFrame() {
        return isValidFrame() && foundPortalBlocks == xSize * zSize;
    }

    public boolean isValidFrame() {
        return lowerCorner != null && xSize >= 2 && zSize >= 2 && xSize < maxXSize && zSize < maxZSize;
    }

    public void lightPortal(Block frameBlock) {
        if (lowerCorner == null) {
            return;
        }

        PortalLink link = CustomPortalsMod.getPortalLinkFromBase(frameBlock);
        BlockState blockState = CustomPortalHelper.blockWithAxis(link != null
                        ? link.portalBlock.defaultBlockState()
                        : CustomPortalsMod.CUSTOM_PORTAL_BLOCK.get().defaultBlockState(),
            Direction.Axis.Y);

        BlockPos.betweenClosed(lowerCorner, lowerCorner.relative(Direction.Axis.X, xSize - 1)
                .relative(Direction.Axis.Z, zSize - 1))
                .forEach(blockPos -> levelAccessor.setBlock(blockPos, blockState, 18));
    }

    @Override
    public void createPortal(Level level, BlockPos pos, BlockState frameBlock, Direction.Axis axis) {
        for (int i = -1; i < 3; i++) {
            level.setBlockAndUpdate(pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, -1), frameBlock);
            level.setBlockAndUpdate(pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, 2), frameBlock);

            level.setBlockAndUpdate(pos.relative(Direction.Axis.Z, i).relative(Direction.Axis.X, -1), frameBlock);
            level.setBlockAndUpdate(pos.relative(Direction.Axis.Z, i).relative(Direction.Axis.X, 2), frameBlock);
        }
        for (int i = 0; i < 2; i++) {
            placeLandingPad(level, pos.relative(Direction.Axis.X, i).below(), frameBlock);
            placeLandingPad(level, pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, 1).below(), frameBlock);

            fillAirAroundPortal(level, pos.relative(Direction.Axis.X, i).above());
            fillAirAroundPortal(level, pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, 1).above());
            fillAirAroundPortal(level, pos.relative(Direction.Axis.X, i).above(2));
            fillAirAroundPortal(level, pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, 1).above(2));
        }

        // Initialize this instance based off of the newly created portal
        lowerCorner = pos;
        xSize = zSize = 2;
        levelAccessor = level;
        foundPortalBlocks = 4;
        lightPortal(frameBlock.getBlock());
    }

    protected void fillAirAroundPortal(Level level, BlockPos pos) {
        if (level.getBlockState(pos).isSolid()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);
        }
    }

    protected void placeLandingPad(Level level, BlockPos pos, BlockState frameBlock) {
        if (!level.getBlockState(pos).isSolid()) {
            level.setBlockAndUpdate(pos, frameBlock);
        }
    }

    @Override
    public boolean isRequestedSize(int attemptWidth, int attemptHeight) {
        return (xSize == attemptWidth || attemptHeight == 0)
                && zSize == attemptHeight
                || attemptWidth == 0
                || (xSize == attemptHeight || attemptHeight == 0)
                && zSize == attemptWidth;
    }

    @Override
    @Nullable
    public BlockUtil.FoundRectangle getRectangle() {
        if (lowerCorner == null) {
            return null;
        }

        return new BlockUtil.FoundRectangle(lowerCorner, xSize, zSize);
    }

    @Override
    @Nullable
    public BlockPos doesPortalFitAt(Level level, BlockPos attemptPos, Direction.Axis axis) {
        BlockUtil.FoundRectangle rect = BlockUtil.getLargestRectangleAround(attemptPos.above(), Direction.Axis.X, 4, Direction.Axis.Z, 4,
                blockPos -> level.getBlockState(blockPos).isSolid()
                        && !level.getBlockState(blockPos.above()).isSolid()
                        && !level.getBlockState(blockPos.above()).liquid()
                        && !level.getBlockState(blockPos.above(2)).isSolid()
                        && !level.getBlockState(blockPos.above(2)).liquid());

        return rect.axis1Size >= 4 && rect.axis2Size >= 4 ? rect.minCorner : null;
    }

    @Override
    public Vec3 getEntityOffsetInPortal(BlockUtil.FoundRectangle rect, Entity entity, Direction.Axis portalAxis) {
        EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
        double xSize = rect.axis1Size - entityDimensions.width();
        double zSize = rect.axis2Size - entityDimensions.width();

        double deltaX = Mth.inverseLerp(entity.getX(), rect.minCorner.getX(), rect.minCorner.getX() + xSize);
        double deltaY = Mth.inverseLerp(entity.getY(), rect.minCorner.getY() - 1D, rect.minCorner.getY() + 1D);
        double deltaZ = Mth.inverseLerp(entity.getZ(), rect.minCorner.getZ(), rect.minCorner.getZ() + zSize);

        return new Vec3(deltaX, deltaY, deltaZ);
    }

    @Override
    public TeleportTransition getTPTargetInPortal(ServerLevel serverLevel, FoundRectangle portalRect, Axis portalAxis, Vec3 prevOffset, Entity entity, PortalLink link) {
        EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
        float xSize = portalRect.axis1Size - entityDimensions.width();
        float zSize = portalRect.axis2Size - entityDimensions.width();

        double x = Mth.lerp(prevOffset.x, portalRect.minCorner.getX(), portalRect.minCorner.getX() + xSize);
        double z = Mth.lerp(prevOffset.z, portalRect.minCorner.getZ(), portalRect.minCorner.getZ() + zSize);

        TeleportTransition.PostTeleportTransition post = TeleportTransition.DO_NOTHING.then(entity1 -> {
            link.playTravelSound(entity1);
            entity1.placePortalTicket(portalRect.minCorner);
            link.executePostTeleportEvent(entity1);
        });

        return new TeleportTransition(serverLevel,
                new Vec3(x, portalRect.minCorner.getY() + 1D, z),
                entity.getDeltaMovement(),
                entity.getYRot(), entity.getXRot(),
                post);
    }
}