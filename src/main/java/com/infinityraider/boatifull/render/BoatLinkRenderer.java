package com.infinityraider.boatifull.render;

import com.infinityraider.boatifull.Boatifull;
import com.infinityraider.boatifull.boatlinking.BoatLinkProvider;
import com.infinityraider.boatifull.boatlinking.IBoatLinkData;
import com.infinityraider.infinitylib.render.RenderUtilBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class BoatLinkRenderer extends RenderUtilBase {
    private static final BoatLinkRenderer INSTANCE = new BoatLinkRenderer();

    public static BoatLinkRenderer getInstance() {
        return INSTANCE;
    }

    private BoatLinkRenderer() {}

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void renderBoatLinks(RenderWorldLastEvent event) {
        List<Entity> entities = Boatifull.proxy.getClientWorld().getLoadedEntityList();
        EntityPlayer player = Boatifull.proxy.getClientPlayer();

        for(Entity entity : entities) {
            if(!(entity instanceof EntityBoat)) {
                continue;
            }
            EntityBoat boat = (EntityBoat) entity;
            IBoatLinkData linkData = BoatLinkProvider.getLinkedBoats(boat);

            if(linkData == null || !linkData.hasLeadingBoat()) {
                return;
            }

            EntityBoat leading = linkData.getLeadingBoat();

            if(leading == null) {
                return;
            }

            double xP = player.prevPosX + (player.posX - player.prevPosX) * (double) event.getPartialTicks();
            double yP = player.prevPosY + (player.posY - player.prevPosY) * (double) event.getPartialTicks();
            double zP = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) event.getPartialTicks();

            double xB = boat.prevPosX + (boat.posX - boat.prevPosX) * (double) event.getPartialTicks();
            double yB = boat.prevPosY + (boat.posY - boat.prevPosY) * (double) event.getPartialTicks();
            double zB = boat.prevPosZ + (boat.posZ - boat.prevPosZ) * (double) event.getPartialTicks();

            GlStateManager.pushMatrix();

            GlStateManager.translate(xB - xP, yB - yP, zB - zP);


            renderCoordinateSystemDebug();

            GlStateManager.popMatrix();
        }
    }
}
