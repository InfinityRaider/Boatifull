package com.infinityraider.boatifull.proxy;

import com.infinityraider.boatifull.boatlinking.*;
import com.infinityraider.boatifull.handler.*;
import com.infinityraider.infinitylib.modules.entitylistener.ModuleEntityListener;
import com.infinityraider.infinitylib.proxy.base.IProxyBase;
import net.minecraftforge.fml.common.event.*;

public interface IProxy extends IProxyBase {

    @Override
    default void initConfiguration(FMLPreInitializationEvent event) {
        ConfigurationHandler.getInstance().init(event);
    }

    @Override
    default void activateRequiredModules() {
        ModuleEntityListener.getInstance().activate();
    }

    @Override
    default void registerCapabilities() {
        this.registerCapability(CapabilityBoatId.getInstance());
    }

    @Override
    default void registerEventHandlers() {
        this.registerEventHandler(BoatLinker.getInstance());
        this.registerEventHandler(InteractionHandler.getInstance());
    }

    @Override
    default void registerSounds() {}
}