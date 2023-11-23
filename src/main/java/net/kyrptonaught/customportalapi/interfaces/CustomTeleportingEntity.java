package net.kyrptonaught.customportalapi.interfaces;

import net.minecraft.world.level.portal.PortalInfo;

public interface CustomTeleportingEntity {

    PortalInfo getCustomTeleportTarget();

    void setCustomTeleportTarget(PortalInfo teleportTarget);
}
