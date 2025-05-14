package net.kyrptonaught.customportalapi;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;

public class CustomPortalRegistrationEvent extends Event implements IModBusEvent {

    /**
     * @param builder The {@link CustomPortalBuilder} to register
     */
    public void register(CustomPortalBuilder builder) {
        builder.registerPortal();
    }
}
