package net.kyrptonaught.customportalapi.client;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.init.ParticleInit;
import net.kyrptonaught.customportalapi.mixin.client.ChunkRendererRegionAccessor;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CustomPortalsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomPortalsModClient {

	@SubscribeEvent
	public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
		event.getBlockColors().register((state, world, pos, tintIndex) -> {
			if (pos != null && world instanceof RenderChunkRegion) {
				var block = CustomPortalHelper.getPortalBase(((ChunkRendererRegionAccessor) world).getLevel(), pos);
				var link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
				if (link != null)
					return link.colorID;
			}
			return 1908001;
		}, CustomPortalsMod.portalBlock.get());
	}

	@SubscribeEvent
	public static void onParticleFactoryRegistry(final RegisterParticleProvidersEvent event) {
		Minecraft.getInstance().particleEngine.register(ParticleInit.CUSTOMPORTALPARTICLE.get(), CustomPortalParticle.Factory::new);
	}
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(CustomPortalsMod.portalBlock.get(), RenderType.translucent()));
    }
}