package net.kyrptonaught.customportalapi.init;

import com.mojang.serialization.Codec;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ParticleInit {
    public static DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, CustomPortalsMod.MOD_ID);

    public static final Supplier<ParticleType<BlockParticleOption>> CUSTOMPORTALPARTICLE = PARTICLES.register("customportalparticle", () -> new ParticleType<BlockParticleOption>(false, BlockParticleOption.DESERIALIZER) {
        private Codec<BlockParticleOption> codec = BlockParticleOption.codec(this);

        @Override
        public Codec<BlockParticleOption> codec() {
            return codec;
        }
    });
}
