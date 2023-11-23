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

public class FlatPortalAreaHelper extends PortalFrameTester {
    protected final int maxXSize = 21, maxZSize = 21;
    protected int xSize = -1, zSize = -1;

    public FlatPortalAreaHelper() {
    }

    public FlatPortalAreaHelper init(LevelAccessor world, BlockPos blockPos, Direction.Axis axis, Block... foundations) {
        VALID_FRAME = Sets.newHashSet(foundations);
        this.world = world;
        this.lowerCorner = this.getLowerCorner(blockPos, Direction.Axis.X, Direction.Axis.Z);
        this.foundPortalBlocks = 0;
        if (lowerCorner == null) {
            lowerCorner = blockPos;
            xSize = zSize = 1;
        } else {
            this.xSize = this.getSize(Direction.Axis.X, 2, maxXSize);
            if (this.xSize > 0) {
                this.zSize = this.getSize(Direction.Axis.Z, 2, maxZSize);
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

    public Optional<PortalFrameTester> getNewPortal(LevelAccessor worldAccess, BlockPos blockPos, Direction.Axis axis, Block... foundations) {
        return getOrEmpty(worldAccess, blockPos, (areaHelper) -> {
            return areaHelper.isValidFrame() && areaHelper.foundPortalBlocks == 0;
        }, axis, foundations);
    }

    public Optional<PortalFrameTester> getOrEmpty(LevelAccessor worldAccess, BlockPos blockPos, Predicate<PortalFrameTester> predicate, Direction.Axis axis, Block... foundations) {
        return Optional.of((PortalFrameTester) new FlatPortalAreaHelper().init(worldAccess, blockPos, axis, foundations)).filter(predicate);
    }

    public boolean isAlreadyLitPortalFrame() {
        return this.isValidFrame() && this.foundPortalBlocks == this.xSize * this.zSize;
    }

    public boolean isValidFrame() {
        return this.lowerCorner != null && xSize >= 2 && zSize >= 2 && xSize < maxXSize && zSize < maxZSize;
    }

    public void lightPortal(Block frameBlock) {
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(frameBlock);
        BlockState blockState = CustomPortalHelper.blockWithAxis(link != null ? link.getPortalBlock().defaultBlockState() : CustomPortalsMod.getDefaultPortalBlock().defaultBlockState(), Direction.Axis.Y);
        BlockPos.betweenClosed(this.lowerCorner, this.lowerCorner.relative(Direction.Axis.X, this.xSize - 1).relative(Direction.Axis.Z, this.zSize - 1)).forEach((blockPos) -> {
            this.world.setBlock(blockPos, blockState, 18);
        });
    }

    @Override
    public void createPortal(Level world, BlockPos pos, BlockState frameBlock, Direction.Axis axis) {
        for (int i = -1; i < 3; i++) {
            world.setBlockAndUpdate(pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, -1), frameBlock);
            world.setBlockAndUpdate(pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, 2), frameBlock);

            world.setBlockAndUpdate(pos.relative(Direction.Axis.Z, i).relative(Direction.Axis.X, -1), frameBlock);
            world.setBlockAndUpdate(pos.relative(Direction.Axis.Z, i).relative(Direction.Axis.X, 2), frameBlock);
        }
        for (int i = 0; i < 2; i++) {
            placeLandingPad(world, pos.relative(Direction.Axis.X, i).below(), frameBlock);
            placeLandingPad(world, pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, 1).below(), frameBlock);

            fillAirAroundPortal(world, pos.relative(Direction.Axis.X, i).above());
            fillAirAroundPortal(world, pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, 1).above());
            fillAirAroundPortal(world, pos.relative(Direction.Axis.X, i).above(2));
            fillAirAroundPortal(world, pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, 1).above(2));
        }
        // inits this instance based off of the newly created portal;
        this.lowerCorner = pos;
        this.xSize = zSize = 2;
        this.world = world;
        this.foundPortalBlocks = 4;
        lightPortal(frameBlock.getBlock());
    }

    protected void fillAirAroundPortal(Level world, BlockPos pos) {
        if (world.getBlockState(pos).isSolid())
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);
    }

    protected void placeLandingPad(Level world, BlockPos pos, BlockState frameBlock) {
        if (!world.getBlockState(pos).isSolid())
            world.setBlockAndUpdate(pos, frameBlock);
    }

    @Override
    public boolean isRequestedSize(int attemptWidth, int attemptHeight) {
        return ((xSize == attemptWidth || attemptHeight == 0) && (zSize == attemptHeight) || attemptWidth == 0) || ((xSize == attemptHeight || attemptHeight == 0) && (zSize == attemptWidth || attemptWidth == 0));
    }

    @Override
    public BlockUtil.FoundRectangle getRectangle() {
        return new BlockUtil.FoundRectangle(lowerCorner, xSize, zSize);
    }

    @Override
    public Direction.Axis getAxis1() {
        return Direction.Axis.X;
    }

    @Override
    public Direction.Axis getAxis2() {
        return Direction.Axis.Z;
    }

    @Override
    public BlockPos doesPortalFitAt(Level world, BlockPos attemptPos, Direction.Axis axis) {
        BlockUtil.FoundRectangle rect = BlockUtil.getLargestRectangleAround(attemptPos.above(), Direction.Axis.X, 4, Direction.Axis.Z, 4, blockPos -> {
            return world.getBlockState(blockPos).isSolid() && !world.getBlockState(blockPos.above()).isSolid() && !world.getBlockState(blockPos.above()).liquid() && !world.getBlockState(blockPos.above(2)).isSolid() && !world.getBlockState(blockPos.above(2)).liquid();
        });
        return rect.axis1Size >= 4 && rect.axis2Size >= 4 ? rect.minCorner : null;
    }

    @Override
    public Vec3 getEntityOffsetInPortal(BlockUtil.FoundRectangle arg, Entity entity, Direction.Axis portalAxis) {
        EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
        double xSize = arg.axis1Size - entityDimensions.width;
        double zSize = arg.axis2Size - entityDimensions.width;

        double deltaX = Mth.inverseLerp(entity.getX(), arg.minCorner.getX(), arg.minCorner.getX() + xSize);
        double deltaY = Mth.inverseLerp(entity.getY(), arg.minCorner.getY() - 1, arg.minCorner.getY() + 1);
        double deltaZ = Mth.inverseLerp(entity.getZ(), arg.minCorner.getZ(), arg.minCorner.getZ() + zSize);

        return new Vec3(deltaX, deltaY, deltaZ);
    }

    @Override
    public PortalInfo getTPTargetInPortal(BlockUtil.FoundRectangle portalRect, Direction.Axis portalAxis, Vec3 prevOffset, Entity entity) {
        EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
        double xSize = portalRect.axis1Size - entityDimensions.width;
        double zSize = portalRect.axis2Size - entityDimensions.width;

        double x = Mth.lerp(prevOffset.x, portalRect.minCorner.getX(), portalRect.minCorner.getX() + xSize);
        double y = Mth.lerp(prevOffset.y, portalRect.minCorner.getY() - 1, portalRect.minCorner.getY() + 1);
        double z = Mth.lerp(prevOffset.z, portalRect.minCorner.getZ(), portalRect.minCorner.getZ() + zSize);

        y = Math.max(y, portalRect.minCorner.getY());
        return new PortalInfo(new Vec3(x, y, z), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
    }
}