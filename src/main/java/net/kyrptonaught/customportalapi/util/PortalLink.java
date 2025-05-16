package net.kyrptonaught.customportalapi.util;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;
import java.util.function.Function;

public class PortalLink {

    public Block frameBlock;
    public PortalIgnitionSource ignitionSource = PortalIgnitionSource.FIRE;
    public CustomPortalBlock portalBlock = CustomPortalsMod.CUSTOM_PORTAL_BLOCK.get();
    public ResourceLocation targetDimensionLocation;
    public ResourceLocation returnDimensionLocation = ResourceLocation.withDefaultNamespace("overworld");
    public boolean onlyIgnitableInReturnDimension = false;
    public int color;
    public int strictWidth, strictHeight;
    public Integer portalSearchYBottom, portalSearchYTop;
    public Integer returnPortalSearchYBottom, returnPortalSearchYTop;
    public ResourceLocation portalFrameTester = CustomPortalsMod.VANILLAPORTAL_FRAMETESTER;
    private Consumer<Entity> postTeleportEvent = entity -> {};
    private Function<Entity, Boolean> preTeleportEvent = entity -> true;

    public PortalLink() {}

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

    public PortalFrameTester.PortalFrameTesterFactory getFrameTester() {
        return CustomPortalApiRegistry.getPortalFrameTester(portalFrameTester);
    }
}