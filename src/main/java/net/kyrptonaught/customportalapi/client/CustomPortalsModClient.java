package net.kyrptonaught.customportalapi.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.mixin.client.ChunkRendererRegionAccessor;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;

@EventBusSubscriber(modid = CustomPortalsMod.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public class CustomPortalsModClient {

    private CustomPortalsModClient() {}

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, world, pos, tintIndex) -> {
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
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(
            () -> ItemBlockRenderTypes.setRenderLayer(
                CustomPortalsMod.portalBlock.get(),
                RenderType.translucent()
            )
        );
    }
}
