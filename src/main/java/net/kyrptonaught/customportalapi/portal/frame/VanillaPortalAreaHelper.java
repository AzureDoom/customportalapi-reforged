package net.kyrptonaught.customportalapi.portal.frame;

import com.google.common.collect.Sets;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Predicate;

public class VanillaPortalAreaHelper extends PortalFrameTester {
    protected final int maxWidth = 21;
    protected final int maxHeight = 21;
    protected Direction.Axis axis;
    protected int height;
    protected int width;

    public VanillaPortalAreaHelper() {

    }

    public PortalFrameTester init(LevelAccessor world, BlockPos blockPos, Direction.Axis axis, Block... foundations) {
        VALID_FRAME = Sets.newHashSet(foundations);
        this.world = world;
        this.axis = axis;
        this.lowerCorner = this.getLowerCorner(blockPos, axis, Direction.Axis.Y);
        this.foundPortalBlocks = 0;
        if (lowerCorner == null) {
            lowerCorner = blockPos;
            width = height = 1;
        } else {
            this.width = this.getSize(axis, 2, maxWidth);
            if (this.width > 0) {
                this.height = this.getSize(Direction.Axis.Y, 3, maxHeight);
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
    public BlockUtil.FoundRectangle getRectangle() {
        return new BlockUtil.FoundRectangle(lowerCorner, width, height);
    }

    @Override
    public Direction.Axis getAxis1() {
        return axis;
    }

    @Override
    public Direction.Axis getAxis2() {
        return Direction.Axis.Y;
    }

    public Optional<PortalFrameTester> getNewPortal(LevelAccessor worldAccess, BlockPos blockPos, Direction.Axis axis, Block... foundations) {
        return getOrEmpty(worldAccess, blockPos, (customAreaHelper) -> {
            return customAreaHelper.isValidFrame() && customAreaHelper.foundPortalBlocks == 0;
        }, axis, foundations);
    }

    public Optional<PortalFrameTester> getOrEmpty(LevelAccessor worldAccess, BlockPos blockPos, Predicate<PortalFrameTester> predicate, Direction.Axis axis, Block... foundations) {
        Optional<PortalFrameTester> optional = Optional.of(new VanillaPortalAreaHelper().init(worldAccess, blockPos, axis, foundations)).filter(predicate);
        if (optional.isPresent()) {
            return optional;
        } else {
            Direction.Axis axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            return Optional.of(new VanillaPortalAreaHelper().init(worldAccess, blockPos, axis2, foundations)).filter(predicate);
        }
    }

    public boolean isAlreadyLitPortalFrame() {
        return this.isValidFrame() && this.foundPortalBlocks == this.width * this.height;
    }

    public boolean isValidFrame() {
        return this.lowerCorner != null && this.width >= 2 && this.width <= maxWidth && this.height >= 3 && this.height <= maxHeight;
    }

    @Override
    public boolean isRequestedSize(int attemptWidth, int attemptHeight) {
        return ((attemptWidth == 0 || width == attemptWidth) && (attemptHeight == 0 || this.height == attemptHeight));
    }

    @Override
    public BlockPos doesPortalFitAt(Level world, BlockPos attemptPos, Direction.Axis axis) {
        if (isEmptySpace(world.getBlockState(attemptPos)) && isEmptySpace(world.getBlockState(attemptPos.relative(axis, 1))) &&
                isEmptySpace(world.getBlockState(attemptPos.above())) && isEmptySpace(world.getBlockState(attemptPos.relative(axis, 1).above())) &&
                isEmptySpace(world.getBlockState(attemptPos.above(2))) && isEmptySpace(world.getBlockState(attemptPos.relative(axis, 1).above(2))) &&
                canHoldPortal(world, attemptPos.below()) && canHoldPortal(world, attemptPos.relative(axis, 1).below()))
            return attemptPos;

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
        double width = arg.axis1Size - entityDimensions.width;
        double height = arg.axis2Size - entityDimensions.height;

        double deltaX = Mth.inverseLerp(entity.getX(), arg.minCorner.getX(), arg.minCorner.getX() + width);
        double deltaY = Mth.inverseLerp(entity.getY(), arg.minCorner.getY(), arg.minCorner.getY() + height);
        double deltaZ = Mth.inverseLerp(entity.getZ(), arg.minCorner.getZ(), arg.minCorner.getZ() + width);


        return new Vec3(deltaX, deltaY, deltaZ);
    }

    @Override
    public PortalInfo getTPTargetInPortal(BlockUtil.FoundRectangle portalRect, Direction.Axis portalAxis, Vec3 prevOffset, Entity entity) {
        EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
        double width = portalRect.axis1Size - entityDimensions.width;
        double height = portalRect.axis2Size - entityDimensions.height;
        double x = Mth.lerp(prevOffset.x, portalRect.minCorner.getX(), portalRect.minCorner.getX() + width);
        double y = Mth.lerp(prevOffset.y, portalRect.minCorner.getY(), portalRect.minCorner.getY() + height);
        double z = Mth.lerp(prevOffset.z, portalRect.minCorner.getZ(), portalRect.minCorner.getZ() + width);
        if (portalAxis == Direction.Axis.X)
            z = portalRect.minCorner.getZ() + 0.5D;
        else if (portalAxis == Direction.Axis.Z)
            x = portalRect.minCorner.getX() + .5D;

        return new PortalInfo(new Vec3(x, y, z), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
    }

    public void lightPortal(Block frameBlock) {
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(frameBlock);
        BlockState blockState = CustomPortalHelper.blockWithAxis(link != null ? link.getPortalBlock().defaultBlockState() : CustomPortalsMod.getDefaultPortalBlock().defaultBlockState(), axis);
        BlockPos.betweenClosed(this.lowerCorner, this.lowerCorner.relative(Direction.UP, this.height - 1).relative(this.axis, this.width - 1)).forEach((blockPos) -> {
            this.world.setBlock(blockPos, blockState, 18);
        });
    }

    public void createPortal(Level world, BlockPos pos, BlockState frameBlock, Direction.Axis axis) {
        Direction.Axis rotatedAxis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        for (int i = -1; i < 4; i++) {
            world.setBlockAndUpdate(pos.above(i).relative(axis, -1), frameBlock);
            world.setBlockAndUpdate(pos.above(i).relative(axis, 2), frameBlock);
            if (i >= 0) {
                fillAirAroundPortal(world, pos.above(i).relative(axis, -1).relative(rotatedAxis, 1));
                fillAirAroundPortal(world, pos.above(i).relative(axis, 2).relative(rotatedAxis, 1));
                fillAirAroundPortal(world, pos.above(i).relative(axis, -1).relative(rotatedAxis, -1));
                fillAirAroundPortal(world, pos.above(i).relative(axis, 2).relative(rotatedAxis, -1));
            }
        }
        for (int i = -1; i < 3; i++) {
            world.setBlockAndUpdate(pos.above(-1).relative(axis, i), frameBlock);
            world.setBlockAndUpdate(pos.above(3).relative(axis, i), frameBlock);

            fillAirAroundPortal(world, pos.above(3).relative(axis, i).relative(rotatedAxis, 1));
            fillAirAroundPortal(world, pos.above(3).relative(axis, i).relative(rotatedAxis, -1));
        }
        placeLandingPad(world, pos.below().relative(rotatedAxis, 1), frameBlock);
        placeLandingPad(world, pos.below().relative(rotatedAxis, -1), frameBlock);
        placeLandingPad(world, pos.below().relative(axis, 1).relative(rotatedAxis, 1), frameBlock);
        placeLandingPad(world, pos.below().relative(axis, 1).relative(rotatedAxis, -1), frameBlock);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                fillAirAroundPortal(world, pos.relative(axis, i).above(j).relative(rotatedAxis, 1));
                fillAirAroundPortal(world, pos.relative(axis, i).above(j).relative(rotatedAxis, -1));
            }
        }
        //inits this instance based off of the newly created portal;
        this.lowerCorner = pos;
        this.width = 2;
        this.height = 3;
        this.axis = axis;
        this.world = world;
        this.foundPortalBlocks = 6;

        lightPortal(frameBlock.getBlock());
    }

    protected void fillAirAroundPortal(Level world, BlockPos pos) {
        if (world.getBlockState(pos).isSolid() || world.getBlockState(pos).isRedstoneConductor(world, pos))
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    protected void placeLandingPad(Level world, BlockPos pos, BlockState frameBlock) {
        if (!world.getBlockState(pos).isSolid())
            world.setBlockAndUpdate(pos, frameBlock);
    }
}