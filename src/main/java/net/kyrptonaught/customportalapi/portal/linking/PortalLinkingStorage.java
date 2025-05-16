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

    public static final Codec<PortalLinkingStorage> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    DimensionLink.CODEC.listOf().fieldOf("portalLinks").forGetter(PortalLinkingStorage::getPortalLinks)
            ).apply(instance, PortalLinkingStorage::new)
    );

    public static final SavedDataType<PortalLinkingStorage> TYPE =  new SavedDataType<>(
            "customportalapi_portal_links",
            PortalLinkingStorage::new,
            CODEC
    );

    private final List<DimensionLink> portalLinks = new ArrayList<>();

    public PortalLinkingStorage() {
    }

    public PortalLinkingStorage(List<DimensionLink> portalLinks) {
        this.portalLinks.addAll(portalLinks);
    }

    public List<DimensionLink> getPortalLinks() {
        return portalLinks;
    }

    @Nullable
    public DimensionalBlockPos getDestination(BlockPos portalFramePos, ResourceKey<Level> dimID) {
        for (DimensionLink link : portalLinks) {
            if (link.fromDimension().equals(dimID.location()) && link.fromPos().equals(portalFramePos)) {
                return link.toPos();
            }
        }

        return null;
    }

    public void createLink(BlockPos portalFramePos, ResourceKey<Level> dimID, BlockPos destPortalFramePos, ResourceKey<Level> destDimID) {
        addLink(portalFramePos, dimID, destPortalFramePos, destDimID);
        addLink(destPortalFramePos, destDimID, portalFramePos, dimID);
    }

    private void addLink(BlockPos portalFramePos, ResourceLocation dimID, BlockPos destPortalFramePos, ResourceLocation destDimID) {
        boolean found = false;
        for (DimensionLink link : portalLinks) {
            if (link.fromDimension().equals(dimID) && link.fromPos().equals(portalFramePos)) {
                found = true;
                break;
            }
        }

        if (!found) {
            portalLinks.add(new DimensionLink(dimID, portalFramePos, new DimensionalBlockPos(destDimID, destPortalFramePos)));
        }
    }

    private void addLink(BlockPos portalFramePos, ResourceKey<Level> dimID, BlockPos destPortalFramePos, ResourceKey<Level> destDimID) {
        addLink(portalFramePos, dimID.location(), destPortalFramePos, destDimID.location());
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}