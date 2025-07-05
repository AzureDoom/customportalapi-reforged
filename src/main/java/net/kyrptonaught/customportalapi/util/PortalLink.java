package net.kyrptonaught.customportalapi.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.event.CPAEvent;
import net.kyrptonaught.customportalapi.event.CPASoundEventData;
import net.kyrptonaught.customportalapi.event.PortalIgniteEvent;
import net.kyrptonaught.customportalapi.event.PortalPreIgniteEvent;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;

public class PortalLink {

    public ResourceLocation block;

    public PortalIgnitionSource portalIgnitionSource = PortalIgnitionSource.FIRE;

    private Supplier<CustomPortalBlock> portalBlock = CustomPortalsMod.portalBlock;

    public ResourceLocation dimID;

    public ResourceLocation returnDimID = ResourceLocation.withDefaultNamespace("overworld");

    public boolean onlyIgnitableInReturnDim = false;

    public int colorID;

    public int forcedWidth, forcedHeight;

    public Integer portalSearchYBottom, portalSearchYTop;

    public Integer returnPortalSearchYBottom, returnPortalSearchYTop;

    public ResourceLocation portalFrameTester = CustomPortalsMod.VANILLAPORTAL_FRAMETESTER;

    private Consumer<Entity> postTPEvent;

    private final CPAEvent<Entity, SHOULDTP> beforeTPEvent = new CPAEvent<>(SHOULDTP.CONTINUE_TP);

    private final CPAEvent<Player, CPASoundEventData> inPortalAmbienceEvent = new CPAEvent<>();

    private final CPAEvent<Player, CPASoundEventData> postTpPortalAmbienceEvent = new CPAEvent<>();

    private PortalIgniteEvent portalIgniteEvent = (player, world, portalPos, framePos, portalIgnitionSource) -> {};

    private PortalPreIgniteEvent portalPreIgniteEvent = (player, world, portalPos, framePos, portalIgnitionSource) -> true;

    public PortalLink() {}

    public PortalLink(ResourceLocation blockID, ResourceLocation dimID, int colorID) {
        this.block = blockID;
        this.dimID = dimID;
        this.colorID = colorID;
    }

    public CustomPortalBlock getPortalBlock() {
        return portalBlock.get();
    }

    public void setPortalBlock(Supplier<CustomPortalBlock> block) {
        this.portalBlock = block;
    }

    public boolean doesIgnitionMatch(PortalIgnitionSource attemptedSource) {
        return portalIgnitionSource.sourceType == attemptedSource.sourceType && portalIgnitionSource.ignitionSourceID.equals(
            attemptedSource.ignitionSourceID
        );
    }

    public boolean canLightInDim(ResourceLocation dim) {
        if (!onlyIgnitableInReturnDim)
            return true;
        return dim.equals(returnDimID) || dim.equals(dimID);
    }

    public CPAEvent<Entity, SHOULDTP> getBeforeTPEvent() {
        return beforeTPEvent;
    }

    public CPAEvent<Player, CPASoundEventData> getInPortalAmbienceEvent() {
        return inPortalAmbienceEvent;
    }

    public CPAEvent<Player, CPASoundEventData> getPostTpPortalAmbienceEvent() {
        return postTpPortalAmbienceEvent;
    }

    public void setPostTPEvent(Consumer<Entity> event) {
        postTPEvent = event;
    }

    public void executePostTPEvent(Entity entity) {
        if (postTPEvent != null)
            postTPEvent.accept(entity);
    }

    public PortalIgniteEvent getPortalIgniteEvent() {
        return portalIgniteEvent;
    }

    public void setPortalIgniteEvent(PortalIgniteEvent portalIgniteEvent) {
        this.portalIgniteEvent = portalIgniteEvent;
    }

    public PortalPreIgniteEvent getPortalPreIgniteEvent() {
        return portalPreIgniteEvent;
    }

    public void setPortalPreIgniteEvent(PortalPreIgniteEvent portalPreIgniteEvent) {
        this.portalPreIgniteEvent = portalPreIgniteEvent;
    }

    public PortalFrameTester.PortalFrameTesterFactory getFrameTester() {
        return CustomPortalApiRegistry.getPortalFrameTester(portalFrameTester);
    }
}
