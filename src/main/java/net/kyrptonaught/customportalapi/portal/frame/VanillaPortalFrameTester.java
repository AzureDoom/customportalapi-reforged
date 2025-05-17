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

public class VanillaPortalFrameTester extends PortalFrameTester {

    protected final int maxWidth = 21;
    protected final int maxHeight = 21;
    protected Direction.Axis portalAxis = Direction.Axis.X;
    protected int height;
    protected int width;

    public PortalFrameTester init(LevelAccessor level, BlockPos blockPos, Direction.Axis axis, Block... foundations) {
        VALID_FRAME = Sets.newHashSet(foundations);
        levelAccessor = level;
        portalAxis = axis;
        lowerCorner = getLowerCorner(blockPos, axis, Direction.Axis.Y);
        foundPortalBlocks = 0;
        if (lowerCorner == null) {
            lowerCorner = blockPos;
            width = height = 1;
        } else {
            width = getSize(axis, 2, maxWidth);
            if (width > 0) {
                height = getSize(Direction.Axis.Y, 3, maxHeight);
                if (checkForValidFrame(axis, Direction.Axis.Y, width, height)) {
                    countExistingPortalBlocks(axis, Direction.Axis.Y, width, height);
                } else {
                    lowerCorner = null;
                    width = height = 1;
                }
            }
        }

        return this;
    }

    @Override
    @Nullable
    public BlockUtil.FoundRectangle getRectangle() {
        if (lowerCorner == null) {
            return null;
        }

        return new BlockUtil.FoundRectangle(lowerCorner, width, height);
    }

    public Optional<PortalFrameTester> getNewPortal(LevelAccessor level, BlockPos blockPos, Direction.Axis axis, Block... foundations) {
        return getOrEmpty(level, blockPos, customAreaHelper -> customAreaHelper.isValidFrame() && customAreaHelper.foundPortalBlocks == 0, axis, foundations);
    }

    public Optional<PortalFrameTester> getOrEmpty(LevelAccessor level, BlockPos blockPos, Predicate<PortalFrameTester> predicate, Direction.Axis axis, Block... foundations) {
        Optional<PortalFrameTester> optional = Optional.of(new VanillaPortalFrameTester()
                .init(level, blockPos, axis, foundations))
                .filter(predicate);

        if (optional.isPresent()) {
            return optional;
        } else {
            Direction.Axis axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            return Optional.of(new VanillaPortalFrameTester()
                    .init(level, blockPos, axis2, foundations))
                    .filter(predicate);
        }
    }

    public boolean isAlreadyLitPortalFrame() {
        return isValidFrame() && foundPortalBlocks == width * height;
    }

    public boolean isValidFrame() {
        return lowerCorner != null && width >= 2 && width <= maxWidth && height >= 3 && height <= maxHeight;
    }

    @Override
    public boolean isRequestedSize(int attemptWidth, int attemptHeight) {
        return ((attemptWidth == 0 || width == attemptWidth) && (attemptHeight == 0 || this.height == attemptHeight));
    }

    @Override
    @Nullable
    public BlockPos doesPortalFitAt(Level level, BlockPos attemptPos, Direction.Axis axis) {
        if (isEmptySpace(level.getBlockState(attemptPos))
                && isEmptySpace(level.getBlockState(attemptPos.relative(axis, 1)))
                && isEmptySpace(level.getBlockState(attemptPos.above()))
                && isEmptySpace(level.getBlockState(attemptPos.relative(axis, 1).above()))
                && isEmptySpace(level.getBlockState(attemptPos.above(2)))
                && isEmptySpace(level.getBlockState(attemptPos.relative(axis, 1).above(2)))
                && canHoldPortal(level, attemptPos.below())
                && canHoldPortal(level, attemptPos.relative(axis, 1).below())) {
            return attemptPos;
        }

        return null;
    }

    protected boolean isEmptySpace(BlockState blockState) {
        return blockState.canBeReplaced() && !blockState.liquid();
    }

    protected boolean canHoldPortal(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.isSolid() && blockState.isRedstoneConductor(world, pos) && !isEmptySpace(blockState);
    }

    @Override
    public Vec3 getEntityOffsetInPortal(BlockUtil.FoundRectangle arg, Entity entity, Direction.Axis portalAxis) {
        EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
        double width = arg.axis1Size - entityDimensions.width();
        double height = arg.axis2Size - entityDimensions.height();

        double deltaX = Mth.inverseLerp(entity.getX(), arg.minCorner.getX(), arg.minCorner.getX() + width);
        double deltaY = Mth.inverseLerp(entity.getY(), arg.minCorner.getY(), arg.minCorner.getY() + height);
        double deltaZ = Mth.inverseLerp(entity.getZ(), arg.minCorner.getZ(), arg.minCorner.getZ() + width);

        return new Vec3(deltaX, deltaY, deltaZ);
    }

    @Override
    public TeleportTransition getTPTargetInPortal(ServerLevel serverLevel, FoundRectangle portalRect, Axis portalAxis, Vec3 prevOffset, Entity entity, PortalLink link) {
        EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
        double width = portalRect.axis1Size - entityDimensions.width();
        double height = portalRect.axis2Size - entityDimensions.height();
        double x = Mth.lerp(prevOffset.x, portalRect.minCorner.getX(), portalRect.minCorner.getX() + width);
        double y = Mth.lerp(prevOffset.y, portalRect.minCorner.getY(), portalRect.minCorner.getY() + height);
        double z = Mth.lerp(prevOffset.z, portalRect.minCorner.getZ(), portalRect.minCorner.getZ() + width);
        if (portalAxis == Direction.Axis.X) {
            z = portalRect.minCorner.getZ() + 0.5D;
        }
        else if (portalAxis == Direction.Axis.Z) {
            x = portalRect.minCorner.getX() + 0.5D;
        }

        TeleportTransition.PostTeleportTransition post = TeleportTransition.DO_NOTHING.then(entity1 -> {
            link.playTravelSound(entity1);
            entity1.placePortalTicket(portalRect.minCorner);
            link.executePostTeleportEvent(entity1);
        });

        return new TeleportTransition(serverLevel,
                new Vec3(x, y, z),
                entity.getDeltaMovement(),
                entity.getYRot(), entity.getXRot(),
                post);
    }

    public void lightPortal(Block frameBlock) {
        if (lowerCorner == null) {
            return;
        }

        PortalLink link = CustomPortalsMod.getPortalLinkFromBase(frameBlock);
        BlockState blockState = CustomPortalHelper.blockWithAxis(link != null
                ? link.portalBlock.defaultBlockState()
                : CustomPortalsMod.CUSTOM_PORTAL_BLOCK.get().defaultBlockState(),
                portalAxis);

        BlockPos.betweenClosed(lowerCorner, lowerCorner.relative(Direction.UP, height - 1)
                        .relative(portalAxis, width - 1))
                .forEach((blockPos) -> levelAccessor.setBlock(blockPos, blockState, 18));
    }

    public void createPortal(Level level, BlockPos pos, BlockState frameBlock, Direction.Axis axis) {
        Direction.Axis rotatedAxis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        for (int i = -1; i < 4; i++) {
            level.setBlockAndUpdate(pos.above(i).relative(axis, -1), frameBlock);
            level.setBlockAndUpdate(pos.above(i).relative(axis, 2), frameBlock);
            if (i >= 0) {
                fillAirAroundPortal(level, pos.above(i).relative(axis, -1).relative(rotatedAxis, 1));
                fillAirAroundPortal(level, pos.above(i).relative(axis, 2).relative(rotatedAxis, 1));
                fillAirAroundPortal(level, pos.above(i).relative(axis, -1).relative(rotatedAxis, -1));
                fillAirAroundPortal(level, pos.above(i).relative(axis, 2).relative(rotatedAxis, -1));
            }
        }
        for (int i = -1; i < 3; i++) {
            level.setBlockAndUpdate(pos.above(-1).relative(axis, i), frameBlock);
            level.setBlockAndUpdate(pos.above(3).relative(axis, i), frameBlock);

            fillAirAroundPortal(level, pos.above(3).relative(axis, i).relative(rotatedAxis, 1));
            fillAirAroundPortal(level, pos.above(3).relative(axis, i).relative(rotatedAxis, -1));
        }
        placeLandingPad(level, pos.below().relative(rotatedAxis, 1), frameBlock);
        placeLandingPad(level, pos.below().relative(rotatedAxis, -1), frameBlock);
        placeLandingPad(level, pos.below().relative(axis, 1).relative(rotatedAxis, 1), frameBlock);
        placeLandingPad(level, pos.below().relative(axis, 1).relative(rotatedAxis, -1), frameBlock);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                fillAirAroundPortal(level, pos.relative(axis, i).above(j).relative(rotatedAxis, 1));
                fillAirAroundPortal(level, pos.relative(axis, i).above(j).relative(rotatedAxis, -1));
            }
        }

        // Initialize this instance based off of the newly created portal
        lowerCorner = pos;
        width = 2;
        height = 3;
        portalAxis = axis;
        levelAccessor = level;
        foundPortalBlocks = 6;

        lightPortal(frameBlock.getBlock());
    }

    protected void fillAirAroundPortal(Level level, BlockPos pos) {
        if (level.getBlockState(pos).isSolid() || level.getBlockState(pos).isRedstoneConductor(level, pos)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    protected void placeLandingPad(Level level, BlockPos pos, BlockState frameBlock) {
        if (!level.getBlockState(pos).isSolid()) {
            level.setBlockAndUpdate(pos, frameBlock);
        }
    }
}