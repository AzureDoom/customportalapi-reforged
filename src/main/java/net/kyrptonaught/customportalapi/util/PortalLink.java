package net.kyrptonaught.customportalapi.util;

import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.portal.frame.VanillaPortalFrameTester;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public class PortalLink {

    @Nullable
    private Block frameBlock;
    public PortalIgnitionSource ignitionSource = PortalIgnitionSource.FIRE;
    public CustomPortalBlock portalBlock = CustomPortalsMod.CUSTOM_PORTAL_BLOCK.get();
    public ResourceLocation targetDimensionLocation = ResourceLocation.withDefaultNamespace("nether");
    public ResourceLocation returnDimensionLocation = ResourceLocation.withDefaultNamespace("overworld");
    public boolean onlyIgnitableInReturnDimension = false;
    public int color;
    public int strictWidth, strictHeight;
    public int portalSearchYBottom, portalSearchYTop = Integer.MIN_VALUE;
    public int returnPortalSearchYBottom, returnPortalSearchYTop = Integer.MIN_VALUE;
    public PortalFrameTester portalFrameTester = new VanillaPortalFrameTester();
    private Consumer<Entity> postTeleportEvent = entity -> {};
    private Function<Entity, Boolean> preTeleportEvent = entity -> true;

    public Block getFrameBlock() {
        if (frameBlock == null) {
            throw new IllegalStateException("Frame block is not set!");
        }

        return frameBlock;
    }

    public void setFrameBlock(Block frameBlock) {
        this.frameBlock = frameBlock;
    }

    public boolean doesIgnitionMatch(PortalIgnitionSource attemptedSource) {
        return ignitionSource.sourceType == attemptedSource.sourceType && ignitionSource.ignitionSourceID.equals(attemptedSource.ignitionSourceID);
    }

    public boolean canLightInDim(ResourceLocation dim) {
        if (!onlyIgnitableInReturnDimension) {
            return true;
        }

        return dim.equals(returnDimensionLocation) || dim.equals(targetDimensionLocation);
    }

    public Function<Entity, Boolean> getPreTeleportEvent() {
        return preTeleportEvent;
    }

    /**
     * Set the pre-teleport event.
     *
     * @param event A function that accepts an entity and returns a boolean
     */
    public void setPreTeleportEvent(Function<Entity, Boolean> event) {
        preTeleportEvent = event;
    }

    public void setPostTeleportEvent(Consumer<Entity> event) {
        postTeleportEvent = event;
    }

    public void executePostTeleportEvent(Entity entity) {
        postTeleportEvent.accept(entity);
    }

    public PortalFrameTester getFrameTester() {
        return portalFrameTester;
    }
}