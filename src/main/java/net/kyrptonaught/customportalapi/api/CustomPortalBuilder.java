package net.kyrptonaught.customportalapi.api;

import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.frame.FlatPortalFrameTester;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class CustomPortalBuilder {
    private final PortalLink portalLink;

    public CustomPortalBuilder() {
        portalLink = new PortalLink();
    }

    /**
     * Register the portal when completed.
     * This should be called last, only when you are finished configuring the portal.
     */
    public void build() {
        CustomPortalsMod.addPortal(portalLink.getFrameBlock(), portalLink);
    }

    /**
     * Specify a frame block as a {@link ResourceLocation}.
     *
     * @param blockLocation ResourceLocation of the Block to be used as the portal's frame block
     */
    public CustomPortalBuilder frame(ResourceLocation blockLocation) {
        portalLink.setFrameBlock(BuiltInRegistries.BLOCK.getValue(blockLocation));
        return this;
    }

    /**
     * Specify a frame block as a {@link Block}.
     *
     * @param block Block to be used as the portal's frame block
     */
    public CustomPortalBuilder frame(Block block) {
        portalLink.setFrameBlock(block);
        return this;
    }

    /**
     * Specify the destination dimension of the portal.
     *
     * @param dimensionLocation ResourceLocation of the dimension the portal will teleport to
     */
    public CustomPortalBuilder destination(ResourceLocation dimensionLocation) {
        portalLink.targetDimensionLocation = dimensionLocation;
        return this;
    }

    /**
     * Specify the color to be used to tint the portal block. Accepts a single int value.
     *
     * @param color Color to be used to tint the portal block
     */
    public CustomPortalBuilder tintColor(int color) {
        portalLink.color = color;
        return this;
    }

    /**
     * Specify the color in RGB to be used to tint the portal block.
     */
    public CustomPortalBuilder tintColor(int r, int g, int b) {
        portalLink.color = ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff);
        return this;
    }

    /**
     * Set the ignition source to an item.
     *
     * @param item Item to be used to ignite the portal
     */
    public CustomPortalBuilder lightWithItem(Item item) {
        portalLink.ignitionSource = PortalIgnitionSource.fromItem(item);
        return this;
    }

    /**
     * Set the ignition source to a fluid.
     *
     * @param fluid Fluid to be used to ignite the portal
     */
    public CustomPortalBuilder lightWithFluid(Fluid fluid) {
        portalLink.ignitionSource = PortalIgnitionSource.fromFluid(fluid);
        return this;
    }

    /**
     * Specify a custom ignition source to ignite the portal.
     * You must manually trigger the ignition yourself.
     */
    public CustomPortalBuilder customIgnitionSource(ResourceLocation customSourceLocation) {
        portalLink.ignitionSource = PortalIgnitionSource.fromCustomSource(customSourceLocation);
        return this;
    }

    /**
     * Specify a custom ignition source to ignite the portal.
     * You must manually trigger the ignition yourself.
     */
    public CustomPortalBuilder customIgnitionSource(PortalIgnitionSource ignitionSource) {
        portalLink.ignitionSource = ignitionSource;
        return this;
    }

    /**
     * Set specific dimensions for the portal.
     *
     * @param width  Width of portal
     * @param height Height of portal
     */
    public CustomPortalBuilder withStrictDimensions(int width, int height) {
        portalLink.strictWidth = width;
        portalLink.strictHeight = height;
        return this;
    }

    /**
     * Specify a custom block to be used as the portal block.
     */
    public CustomPortalBuilder customPortalBlock(CustomPortalBlock portalBlock) {
        portalLink.portalBlock = portalBlock;
        return this;
    }

    /**
     * Specify the dimension this portal will return you to.
     *
     * @param returnDimensionLocation ResourceLocation of the dimension the portal will return you to
     * @param onlyIgnitableInReturnDimension Whether the portal can only be ignited in the return dimension
     */
    public CustomPortalBuilder returnDimension(ResourceLocation returnDimensionLocation, boolean onlyIgnitableInReturnDimension) {
        portalLink.returnDimensionLocation = returnDimensionLocation;
        portalLink.onlyIgnitableInReturnDimension = onlyIgnitableInReturnDimension;
        return this;
    }

    /**
     * Specify that this portal can only be ignited in the overworld.
     * Attempting to light it in other dimensions will fail.
     */
    public CustomPortalBuilder onlyLightInOverworld() {
        portalLink.onlyIgnitableInReturnDimension = true;
        return this;
    }

    /**
     * Specify that this is a flat portal (end portal style).
     */
    public CustomPortalBuilder flatPortal() {
        portalLink.portalFrameTester = new FlatPortalFrameTester();
        return this;
    }

    /**
     * Specify a custom portal frame tester to be used.
     *
     * @param frameTester The custom portal frame tester to be used
     */
    public CustomPortalBuilder customFrameTester(PortalFrameTester frameTester) {
        portalLink.portalFrameTester = frameTester;
        return this;
    }

    /**
     * Register an event to be called immediately before the specified entity is teleported.
     * Returning true will allow the teleportation to continue, while returning false will cancel it.
     *
     * @param event A function that accepts an entity and returns a boolean
     */
    public CustomPortalBuilder preTeleportEvent(Function<Entity, Boolean> event) {
        portalLink.setPreTeleportEvent(event);
        return this;
    }

    /**
     * Register an event to be called after the specified entity is teleported.
     *
     * @param event A consumer that accepts an entity
     */
    public CustomPortalBuilder postTeleportEvent(Consumer<Entity> event) {
        portalLink.setPostTeleportEvent(event);
        return this;
    }

    /**
     * Set the sound to be played when the player travels through the portal. Volume and pitch are accepted as functions
     * to allow for dynamic values.
     *
     * @param travelSoundLocation ResourceLocation of the sound to be played
     * @param travelSoundVolume Volume of the sound
     * @param travelSoundPitch Pitch of the sound
     */
    public CustomPortalBuilder travelSound(ResourceLocation travelSoundLocation, Function<Entity, Float> travelSoundVolume, Function<Entity, Float> travelSoundPitch) {
        portalLink.setTravelSound(travelSoundLocation, travelSoundVolume, travelSoundPitch);
        return this;
    }

    /**
     * Set the sound to be played when the player travels through the portal. Volume and pitch are accepted as functions
     * to allow for dynamic values.
     *
     * @param triggerSoundLocation ResourceLocation of the sound to be played
     * @param triggerSoundVolume Volume of the sound
     * @param triggerSoundPitch Pitch of the sound
     */
    public CustomPortalBuilder triggerSound(ResourceLocation triggerSoundLocation, Function<Entity, Float> triggerSoundVolume, Function<Entity, Float> triggerSoundPitch) {
        portalLink.setTriggerSound(triggerSoundLocation, triggerSoundVolume, triggerSoundPitch);
        return this;
    }

    /**
     * Set the sound to be played randomly while nearby the portal.
     *
     * @param ambientSoundLocation ResourceLocation of the sound to be played
     * @param ambientSoundVolume Volume of the sound
     * @param ambientSoundPitch Pitch of the sound
     */
    public CustomPortalBuilder ambientSound(ResourceLocation ambientSoundLocation, Function<Level, Float> ambientSoundVolume, Function<Level, Float> ambientSoundPitch) {
        portalLink.setAmbientSound(ambientSoundLocation, ambientSoundVolume, ambientSoundPitch);
        return this;
    }

    /**
     * Set a custom portal particle effect.
     *
     * @param particleFunction A function that accepts a Level and BlockPos and returns a ParticleOptions
     */
    public CustomPortalBuilder portalParticle(BiFunction<Level, BlockPos, ParticleOptions> particleFunction) {
        portalLink.portalParticle = particleFunction;
        return this;
    }
}