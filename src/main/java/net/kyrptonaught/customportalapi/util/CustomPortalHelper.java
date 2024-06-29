package net.kyrptonaught.customportalapi.util;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

public class CustomPortalHelper {
    public static boolean isInstanceOfCustomPortal(Level world, BlockPos pos) {
        return isInstanceOfCustomPortal(world.getBlockState(pos));
    }

    public static boolean isInstanceOfCustomPortal(BlockState state) {
        return state.getBlock() instanceof CustomPortalBlock;
    }

    public static boolean isInstanceOfPortalFrame(Level world, BlockPos pos) {
        if (world.isInWorldBounds(pos))
            return CustomPortalApiRegistry.isRegisteredFrameBlock(world.getBlockState(pos));
        return false;
    }

    public static Block getPortalBase(Level world, BlockPos pos) {
        if (isInstanceOfCustomPortal(world, pos)) {
            return ((CustomPortalBlock) world.getBlockState(pos).getBlock()).getPortalBase(world, pos);
        } else if (isInstanceOfPortalFrame(world, pos))
            return world.getBlockState(pos).getBlock();

        return Blocks.AIR;
    }

    public static Block getPortalBaseDefault(Level world, BlockPos pos) {
        if (isInstanceOfCustomPortal(world, pos)) {
            Axis axis = getAxisFrom(world.getBlockState(pos));

            if (axis != Axis.Y) {
                if (isInstanceOfPortalFrame(world, pos.below()))
                    return world.getBlockState(pos.below()).getBlock();
                if (isInstanceOfPortalFrame(world, pos.above()))
                    return world.getBlockState(pos.above()).getBlock();
            } else
                axis = Direction.Axis.Z;

            if (isInstanceOfPortalFrame(world, pos.relative(axis, -1)))
                return world.getBlockState(pos.relative(axis, -1)).getBlock();
            if (isInstanceOfPortalFrame(world, pos.relative(axis, 1)))
                return world.getBlockState(pos.relative(axis, 1)).getBlock();

            return getPortalBaseDefault(world, pos.relative(axis, -1));
        } else if (isInstanceOfPortalFrame(world, pos))
            return world.getBlockState(pos).getBlock();

        return Blocks.AIR;
    }

    public static BlockPos getClosestFrameBlock(Level world, BlockPos pos) {
        if (isInstanceOfPortalFrame(world, pos.below()))
            return pos.below();
        if (isInstanceOfPortalFrame(world, pos.east()))
            return pos.east();
        if (isInstanceOfPortalFrame(world, pos.west()))
            return pos.west();
        if (isInstanceOfPortalFrame(world, pos.north()))
            return pos.north();
        if (isInstanceOfPortalFrame(world, pos.south()))
            return pos.south();
        if (isInstanceOfPortalFrame(world, pos.above()))
            return pos.above();
        return pos;
    }

    public static Direction.Axis getAxisFrom(BlockState state) {
        if (state.getBlock() instanceof CustomPortalBlock)
            return state.getValue(CustomPortalBlock.AXIS);
        if (state.getBlock() instanceof NetherPortalBlock)
            return state.getValue(NetherPortalBlock.AXIS);
        if (state.getBlock() instanceof EndPortalBlock)
            return Direction.Axis.Y;
        return Axis.X;
    }

    public static BlockState blockWithAxis(BlockState state, Direction.Axis axis) {
        if (state.getBlock() instanceof CustomPortalBlock)
            return state.setValue(CustomPortalBlock.AXIS, axis);
        return state;
    }
}