package net.kyrptonaught.customportalapi.interfaces;

import net.minecraft.world.level.portal.PortalInfo;

public interface CustomTeleportingEntity {

    void setCustomTeleportTarget(PortalInfo teleportTarget);

    PortalInfo getCustomTeleportTarget();
}
