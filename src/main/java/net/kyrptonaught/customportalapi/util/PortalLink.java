package net.kyrptonaught.customportalapi.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.network.PlayerSoundPayload;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.portal.frame.VanillaPortalFrameTester;

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

    private BiFunction<BlockPos, PortalIgnitionSource, Boolean> prePortalIgniteEvent = (portalPos, source) -> true;

    private BiConsumer<BlockPos, PortalIgnitionSource> postPortalIgniteEvent = (portalPos, source) -> {};

    @Nullable
    private ResourceLocation travelSoundLocation = BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.PORTAL_TRAVEL);

    private Function<Entity, Float> travelSoundVolume = (entity) -> entity.getRandom().nextFloat() * 0.4F + 0.8F;

    private Function<Entity, Float> travelSoundPitch = (entity) -> 0.25f;

    @Nullable
    public ResourceLocation triggerSoundLocation = BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.PORTAL_TRIGGER);

    public Function<Entity, Float> triggerSoundVolume = (entity) -> entity.getRandom().nextFloat() * 0.4F + 0.8F;

    public Function<Entity, Float> triggerSoundPitch = (entity) -> 0.25f;

    @Nullable
    public ResourceLocation ambientSoundLocation = BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.PORTAL_AMBIENT);

    public Function<Level, Float> ambientSoundVolume = (level) -> 0.5f;

    public Function<Level, Float> ambientSoundPitch = (level) -> level.random.nextFloat() * 0.4F + 0.8F;

    public BiFunction<Level, BlockPos, ParticleOptions> portalParticle = (level, pos) -> new BlockParticleOption(
        ParticleTypes.BLOCK,
        CustomPortalHelper.getPortalBaseDefault(level, pos).defaultBlockState()
    );

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
        return ignitionSource.sourceType == attemptedSource.sourceType && ignitionSource.ignitionSourceID.equals(
            attemptedSource.ignitionSourceID
        );
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

    public void setPreTeleportEvent(Function<Entity, Boolean> event) {
        preTeleportEvent = event;
    }

    public void setPostTeleportEvent(Consumer<Entity> event) {
        postTeleportEvent = event;
    }

    public void executePostTeleportEvent(Entity entity) {
        postTeleportEvent.accept(entity);
    }

    public BiFunction<BlockPos, PortalIgnitionSource, Boolean> getPrePortalIgniteEvent() {
        return prePortalIgniteEvent;
    }

    public void setPrePortalIgniteEvent(BiFunction<BlockPos, PortalIgnitionSource, Boolean> event) {
        prePortalIgniteEvent = event;
    }

    public BiConsumer<BlockPos, PortalIgnitionSource> getPostPortalIgniteEvent() {
        return postPortalIgniteEvent;
    }

    public void setPostPortalIgniteEvent(BiConsumer<BlockPos, PortalIgnitionSource> event) {
        postPortalIgniteEvent = event;
    }

    public void setTravelSound(
        ResourceLocation travelSoundLocation,
        Function<Entity, Float> travelSoundVolume,
        Function<Entity, Float> travelSoundPitch
    ) {
        this.travelSoundLocation = travelSoundLocation;
        this.travelSoundVolume = travelSoundVolume;
        this.travelSoundPitch = travelSoundPitch;
    }

    public void playTravelSound(Entity entity) {
        if (entity instanceof ServerPlayer player && travelSoundLocation != null) {
            PacketDistributor.sendToPlayer(
                player,
                new PlayerSoundPayload(travelSoundLocation, travelSoundVolume.apply(entity), travelSoundPitch.apply(entity))
            );
        }
    }

    public void setTriggerSound(
        ResourceLocation triggerSoundLocation,
        Function<Entity, Float> triggerSoundVolume,
        Function<Entity, Float> triggerSoundPitch
    ) {
        this.triggerSoundLocation = triggerSoundLocation;
        this.triggerSoundVolume = triggerSoundVolume;
        this.triggerSoundPitch = triggerSoundPitch;
    }

    public void setAmbientSound(
        ResourceLocation ambientSoundLocation,
        Function<Level, Float> ambientSoundVolume,
        Function<Level, Float> ambientSoundPitch
    ) {
        this.ambientSoundLocation = ambientSoundLocation;
        this.ambientSoundVolume = ambientSoundVolume;
        this.ambientSoundPitch = ambientSoundPitch;
    }

    public PortalFrameTester getFrameTester() {
        return portalFrameTester;
    }
}
