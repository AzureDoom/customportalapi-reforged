package net.kyrptonaught.customportalapi.init;

import com.mojang.serialization.Codec;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ParticleInit {
    public static DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, CustomPortalsMod.MOD_ID);

    public static final RegistryObject<ParticleType<BlockParticleOption>> CUSTOMPORTALPARTICLE = PARTICLES.register("customportalparticle", () -> new ParticleType<BlockParticleOption>(false, BlockParticleOption.DESERIALIZER) {
        private Codec<BlockParticleOption> codec = BlockParticleOption.codec(this);
        @Override
        public Codec<BlockParticleOption> codec() {
            return codec;
        }
    });
}
