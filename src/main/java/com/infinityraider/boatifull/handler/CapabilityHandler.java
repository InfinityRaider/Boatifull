package com.infinityraider.boatifull.handler;

import com.infinityraider.boatifull.boatlinking.BoatLinkProvider;
import net.minecraft.entity.item.EntityBoat;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityHandler {
    private static final CapabilityHandler INSTANCE = new CapabilityHandler();

    public static CapabilityHandler getInstance() {
        return INSTANCE;
    }

    private CapabilityHandler() {}

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void addEntityCapabilities(AttachCapabilitiesEvent.Entity event) {
        if(event.getEntity() instanceof EntityBoat) {
            event.addCapability(BoatLinkProvider.KEY, new BoatLinkProvider((EntityBoat) event.getEntity()));
        }
    }
}
