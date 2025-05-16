package net.kyrptonaught.customportalapi.util;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.event.CPAEvent;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

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

    private Consumer<Entity> postTPEvent;

    private final CPAEvent<Entity, SHOULDTP> beforeTPEvent = new CPAEvent<>(SHOULDTP.CONTINUE_TP);

    public PortalLink() {}

    public boolean doesIgnitionMatch(PortalIgnitionSource attemptedSource) {
        return ignitionSource.sourceType == attemptedSource.sourceType && ignitionSource.ignitionSourceID.equals(attemptedSource.ignitionSourceID);
    }

    public boolean canLightInDim(ResourceLocation dim) {
        if (!onlyIgnitableInReturnDimension)
            return true;
        return dim.equals(returnDimensionLocation) || dim.equals(targetDimensionLocation);
    }

    public CPAEvent<Entity, SHOULDTP> getBeforeTPEvent() {
        return beforeTPEvent;
    }

    public void setPostTPEvent(Consumer<Entity> event) {
        postTPEvent = event;
    }

    public void executePostTPEvent(Entity entity) {
        if (postTPEvent != null)
            postTPEvent.accept(entity);
    }

    public PortalFrameTester.PortalFrameTesterFactory getFrameTester() {
        return CustomPortalApiRegistry.getPortalFrameTester(portalFrameTester);
    }
}