package net.kyrptonaught.customportalapi.mixin.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.block.Portal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PortalProcessor.class)
public interface PortalManagerAccessor {

    @Accessor
    Portal getPortal();

    @Accessor
    BlockPos getEntryPosition();
}
