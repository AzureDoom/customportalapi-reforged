package net.kyrptonaught.customportalapi.portal.linking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record DimensionLink(ResourceLocation fromDimension, BlockPos fromPos, DimensionalBlockPos toPos) {

	public static final Codec<DimensionLink> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					ResourceLocation.CODEC.fieldOf("fromDimID").forGetter(DimensionLink::fromDimension),
					BlockPos.CODEC.fieldOf("fromPos").forGetter(DimensionLink::fromPos),
					DimensionalBlockPos.CODEC.fieldOf("toPos").forGetter(DimensionLink::toPos)
			).apply(instance, DimensionLink::new)
	);
}