package net.kyrptonaught.customportalapi.portal;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.HashSet;
import java.util.Optional;

public class PortalIgnitionSource {

    public static final PortalIgnitionSource FIRE = new PortalIgnitionSource(
        SourceType.BLOCK_PLACED,
        BuiltInRegistries.BLOCK.getKey(Blocks.FIRE)
    );

    public static final PortalIgnitionSource WATER = fromFluid(Fluids.WATER);

    public enum SourceType {
        USE_ITEM,
        BLOCK_PLACED,
        FLUID,
        CUSTOM
    }

    private static final HashSet<Item> USE_ITEMS = new HashSet<>();

    public final SourceType sourceType;

    public final ResourceLocation ignitionSourceID;

    private PortalIgnitionSource(SourceType sourceType, ResourceLocation ignitionSourceID) {
        this.sourceType = sourceType;
        this.ignitionSourceID = ignitionSourceID;
    }

    public static PortalIgnitionSource fromItem(Item item) {
        USE_ITEMS.add(item);
        return new PortalIgnitionSource(SourceType.USE_ITEM, BuiltInRegistries.ITEM.getKey(item));
    }

    public static PortalIgnitionSource fromFluid(Fluid fluid) {
        return new PortalIgnitionSource(SourceType.FLUID, BuiltInRegistries.FLUID.getKey(fluid));
    }

    public static PortalIgnitionSource fromCustomSource(ResourceLocation ignitionSourceID) {
        return new PortalIgnitionSource(SourceType.CUSTOM, ignitionSourceID);
    }

    public static boolean isRegisteredIgnitionSourceWith(Item item) {
        return USE_ITEMS.contains(item);
    }

    public boolean isWater() {
        return Optional.of(BuiltInRegistries.FLUID.get(ignitionSourceID))
                .filter(fluid -> fluid.isPresent() && fluid.orElseThrow().is(FluidTags.WATER))
                .isPresent();
    }

    public boolean isLava() {
        return Optional.of(BuiltInRegistries.FLUID.get(ignitionSourceID))
                .filter(fluid -> fluid.isPresent() && fluid.orElseThrow().is(FluidTags.LAVA))
                .isPresent();
    }
}