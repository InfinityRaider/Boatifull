package com.infinityraider.boatifull.proxy;

import com.infinityraider.boatifull.handler.*;
import com.infinityraider.boatifull.render.BoatLinkRenderer;
import com.infinityraider.infinitylib.proxy.base.IClientProxyBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@SuppressWarnings("unused")
public class ClientProxy implements IProxy, IClientProxyBase {
    @Override
    public void registerEventHandlers() {
        IProxy.super.registerEventHandlers();
        this.registerEventHandler(EntitySpawnHandler.getInstance());
        this.registerEventHandler(TooltipHandler.getInstance());
        this.registerEventHandler(BoatLinkRenderer.getInstance());
    }
}
