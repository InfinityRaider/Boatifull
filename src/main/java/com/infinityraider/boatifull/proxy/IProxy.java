package com.infinityraider.boatifull.proxy;

import com.infinityraider.boatifull.boatlinking.*;
import com.infinityraider.boatifull.handler.*;
import com.infinityraider.infinitylib.proxy.base.IProxyBase;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.*;

public interface IProxy extends IProxyBase {
    @Override
    default void preInitEnd(FMLPreInitializationEvent event) {
        CapabilityManager.INSTANCE.register(IBoatLinkData.class, new BoatLinkProvider.Storage(), BoatLinkData.class);
    }

    @Override
    default void initConfiguration(FMLPreInitializationEvent event) {

    }

    @Override
    default void activateRequiredModules() {

    }

    @Override
    default void registerEventHandlers() {
        this.registerEventHandler(BoatLinker.getInstance());
        this.registerEventHandler(BoatMovementHandler.getInstance());
        this.registerEventHandler(CapabilityHandler.getInstance());
        this.registerEventHandler(InteractionHandler.getInstance());
    }
}
