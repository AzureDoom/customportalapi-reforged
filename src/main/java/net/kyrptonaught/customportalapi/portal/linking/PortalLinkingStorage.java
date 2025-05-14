package net.kyrptonaught.customportalapi.portal.linking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

public class PortalLinkingStorage extends SavedData {

    private final ConcurrentHashMap<ResourceLocation, ConcurrentHashMap<BlockPos, DimensionalBlockPos>> portalLinks =
        new ConcurrentHashMap<>();

    public PortalLinkingStorage() {
        super();
    }

    public static SavedData.Factory<PortalLinkingStorage> factory() {
        return new SavedData.Factory<>(PortalLinkingStorage::new, PortalLinkingStorage::fromNbt, DataFixTypes.LEVEL);
    }

    public static PortalLinkingStorage fromNbt(CompoundTag tag, HolderLookup.Provider provider) {
        PortalLinkingStorage cman = new PortalLinkingStorage();
        ListTag links = (ListTag) tag.get("portalLinks");

        for (int i = 0; i < links.size(); i++) {
            CompoundTag link = links.getCompound(i);
            DimensionalBlockPos toTag = DimensionalBlockPos.fromTag(link.getCompound("to"));
            cman.addLink(
                BlockPos.of(link.getLong("fromPos")),
                ResourceLocation.parse(link.getString("fromDimID")),
                toTag.pos,
                toTag.dimensionType
            );
        }
        return cman;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        ListTag links = new ListTag();
        portalLinks.keys().asIterator().forEachRemaining(dimKey -> portalLinks.get(dimKey).forEach((blockPos, dimensionalBlockPos) -> {
            CompoundTag link = new CompoundTag();
            link.putString("fromDimID", dimKey.toString());
            link.putLong("fromPos", blockPos.asLong());
            link.put("to", dimensionalBlockPos.toTag(new CompoundTag()));
            links.add(link);
        }));
        tag.put("portalLinks", links);
        return tag;
    }

    public DimensionalBlockPos getDestination(BlockPos portalFramePos, ResourceKey<Level> dimID) {
        if (portalLinks.containsKey(dimID.location()))
            return portalLinks.get(dimID.location()).get(portalFramePos);
        return null;
    }

    public void createLink(BlockPos portalFramePos, ResourceKey<Level> dimID, BlockPos destPortalFramePos, ResourceKey<Level> destDimID) {
        addLink(portalFramePos, dimID, destPortalFramePos, destDimID);
        addLink(destPortalFramePos, destDimID, portalFramePos, dimID);
    }

    private void addLink(BlockPos portalFramePos, ResourceLocation dimID, BlockPos destPortalFramePos, ResourceLocation destDimID) {
        if (!portalLinks.containsKey(dimID))
            portalLinks.put(dimID, new ConcurrentHashMap<>());
        portalLinks.get(dimID).put(portalFramePos, new DimensionalBlockPos(destDimID, destPortalFramePos));
    }

    private void addLink(BlockPos portalFramePos, ResourceKey<Level> dimID, BlockPos destPortalFramePos, ResourceKey<Level> destDimID) {
        addLink(portalFramePos, dimID.location(), destPortalFramePos, destDimID.location());
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
