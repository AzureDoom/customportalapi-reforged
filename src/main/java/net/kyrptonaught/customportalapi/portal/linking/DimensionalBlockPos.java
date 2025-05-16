package net.kyrptonaught.customportalapi.portal.linking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class DimensionalBlockPos {

    public static final Codec<DimensionalBlockPos> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("dimID").forGetter(DimensionalBlockPos::getDimensionType),
                    BlockPos.CODEC.fieldOf("pos").forGetter(DimensionalBlockPos::getPos)
            ).apply(instance, DimensionalBlockPos::new)
    );

    public ResourceLocation dimensionType;

    public BlockPos pos;

    public DimensionalBlockPos(ResourceLocation dimension, BlockPos pos) {
        this.pos = pos;
        this.dimensionType = dimension;
    }

    public ResourceLocation getDimensionType() {
        return dimensionType;
    }

    public BlockPos getPos() {
        return pos;
    }
}