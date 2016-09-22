package com.infinityraider.boatifull.handler;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class BoatMovementHandler {
    private static final BoatMovementHandler INSTANCE = new BoatMovementHandler();

    public static BoatMovementHandler getInstance() {
        return INSTANCE;
    }

    public BoatMovementHandler() {}

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onEntityTick(TickEvent event) {

    }
}
