package net.kyrptonaught.customportalapi;

import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.neoforged.bus.api.Event;

public class CustomPortalRegistrationEvent extends Event {
    /**
     * @param builder The {@link CustomPortalBuilder} to register
     */
    public void register(CustomPortalBuilder builder) {
        builder.registerPortal();
    }
}
