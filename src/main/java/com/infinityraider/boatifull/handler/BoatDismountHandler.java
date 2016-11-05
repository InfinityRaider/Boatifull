package com.infinityraider.boatifull.handler;

import com.infinityraider.boatifull.Boatifull;
import com.infinityraider.infinitylib.modules.entitylistener.IEntityLeaveOrJoinWorldListener;
import com.infinityraider.infinitylib.modules.entitylistener.ModuleEntityListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BoatDismountHandler implements IEntityLeaveOrJoinWorldListener {
    private static final BoatDismountHandler INSTANCE = new BoatDismountHandler();

    public static BoatDismountHandler getInstance() {
        return INSTANCE;
    }

    private EntityBoat dismounted;

    private BoatDismountHandler() {
        ModuleEntityListener.getInstance().registerListener(this);
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onDismountFromBoat(EntityMountEvent event) {
        if(event.getEntityBeingMounted() instanceof EntityBoat && event.isDismounting() && event.getEntityMounting() instanceof EntityPlayer) {
            EntityBoat boat = (EntityBoat) event.getEntityBeingMounted();
            EntityPlayer player = (EntityPlayer) event.getEntityMounting();
            if(player == Boatifull.instance.getClientPlayer()) {
                this.dismounted = boat;
            }
        }
    }


    @SubscribeEvent
    @SuppressWarnings("unused")
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if(this.dismounted != null && event.player == Boatifull.instance.getClientPlayer()) {
            double cos = Math.cos(Math.toRadians(this.dismounted.rotationYaw));
            double sin = Math.sin(Math.toRadians(this.dismounted.rotationYaw));
            double dx = 0;
            double dy = 1.5;
            double dz = -0.5;
            double x = this.dismounted.posX + dx*cos - dz*sin;
            double y = this.dismounted.posY + dy;
            double z = this.dismounted.posZ + dx*sin + dz*cos;
            event.player.setPosition(x, y, z);
            this.dismounted = null;
        }
    }

    @Override
    public void onEntityJoinWorld(Entity entity) {}

    @Override
    public void onEntityLeaveWorld(Entity entity) {
        if(entity == this.dismounted) {
            this.dismounted = null;
        }
    }
}
