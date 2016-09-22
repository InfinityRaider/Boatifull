package com.infinityraider.boatifull.handler;

import com.infinityraider.boatifull.network.MessageRequestBoatSync;
import com.infinityraider.infinitylib.network.NetworkWrapper;
import net.minecraft.entity.item.EntityBoat;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntitySpawnHandler {
    private static final EntitySpawnHandler INSTANCE = new EntitySpawnHandler();

    public static EntitySpawnHandler getInstance() {
        return INSTANCE;
    }

    private EntitySpawnHandler() {}

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        if(event.getWorld().isRemote && event.getEntity() instanceof EntityBoat) {
            NetworkWrapper.getInstance().sendToServer(new MessageRequestBoatSync((EntityBoat) event.getEntity()));
        }
    }
}
