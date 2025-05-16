package net.kyrptonaught.customportalapi.portal.linking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Represents a link between two {@link DimensionalBlockPos} objects.
 *
 * @param fromPos The starting position of the link.
 * @param toPos The ending position of the link.
 */
public record DimensionLink(DimensionalBlockPos fromPos, DimensionalBlockPos toPos) {

	public static final Codec<DimensionLink> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					DimensionalBlockPos.CODEC.fieldOf("fromPos").forGetter(DimensionLink::fromPos),
					DimensionalBlockPos.CODEC.fieldOf("toPos").forGetter(DimensionLink::toPos)
			).apply(instance, DimensionLink::new)
	);
}