package net.kyrptonaught.customportalapi.portal.linking;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class DimensionalBlockPos {
    public ResourceLocation dimensionType;
    public BlockPos pos;

    public DimensionalBlockPos(ResourceLocation dimension, BlockPos pos) {
        this.pos = pos;
        this.dimensionType = dimension;
    }

    public static DimensionalBlockPos fromTag(CompoundTag tag) {
        return new DimensionalBlockPos(new ResourceLocation(tag.getString("dimID")), BlockPos.of(tag.getLong("pos")));
    }

    public CompoundTag toTag(CompoundTag tag) {
        tag.putString("dimID", this.dimensionType.toString());
        tag.putLong("pos", pos.asLong());
        return tag;
    }
}