package net.kyrptonaught.customportalapi.interfaces;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ClientPlayerInColoredPortal {

    int getLastUsedPortalColor();

    void setLastUsedPortalColor(int color);

}
