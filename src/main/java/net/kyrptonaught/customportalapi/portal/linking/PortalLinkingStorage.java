package net.kyrptonaught.customportalapi.portal.linking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PortalLinkingStorage extends SavedData {

    public static final Codec<PortalLinkingStorage> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            DimensionLink.CODEC.listOf().fieldOf("dimensionLinks").forGetter(PortalLinkingStorage::getDimensionLinks)
        ).apply(instance, PortalLinkingStorage::new)
    );

    public static final SavedDataType<PortalLinkingStorage> TYPE = new SavedDataType<>(
        "customportalapi_dimension_links",
        PortalLinkingStorage::new,
        CODEC,
        null
    );

    private final List<DimensionLink> dimensionLinks = new ArrayList<>();

    public PortalLinkingStorage() {}

    public PortalLinkingStorage(List<DimensionLink> portalLinks) {
        this.dimensionLinks.addAll(portalLinks);
    }

    public List<DimensionLink> getDimensionLinks() {
        return dimensionLinks;
    }

    @Nullable
    public DimensionalBlockPos getDestination(BlockPos portalFramePos, ResourceKey<Level> dim) {
        for (DimensionLink link : dimensionLinks) {
            if (link.fromPos().dimension().equals(dim.location()) && link.fromPos().pos().equals(portalFramePos)) {
                return link.toPos();
            }
        }

        return null;
    }

    public void createLink(BlockPos portalFramePos, ResourceKey<Level> fromDim, BlockPos destPortalFramePos, ResourceKey<Level> destDim) {
        addLink(portalFramePos, fromDim, destPortalFramePos, destDim);
        addLink(destPortalFramePos, destDim, portalFramePos, fromDim);
    }

    private void addLink(BlockPos portalFramePos, ResourceLocation fromDim, BlockPos destPortalFramePos, ResourceLocation destDim) {
        boolean found = false;
        for (DimensionLink link : dimensionLinks) {
            if (link.fromPos().dimension().equals(fromDim) && link.fromPos().pos().equals(portalFramePos)) {
                found = true;
                break;
            }
        }

        if (!found) {
            dimensionLinks.add(
                new DimensionLink(new DimensionalBlockPos(fromDim, portalFramePos), new DimensionalBlockPos(destDim, destPortalFramePos))
            );
        }
    }

    private void addLink(BlockPos portalFramePos, ResourceKey<Level> fromDim, BlockPos destPortalFramePos, ResourceKey<Level> destDim) {
        addLink(portalFramePos, fromDim.location(), destPortalFramePos, destDim.location());
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
