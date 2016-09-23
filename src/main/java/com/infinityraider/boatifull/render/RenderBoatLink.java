package com.infinityraider.boatifull.render;

import com.infinityraider.boatifull.Boatifull;
import com.infinityraider.boatifull.boatlinking.BoatLinker;
import com.infinityraider.boatifull.entity.EntityBoatLink;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderBoatLink extends Render<EntityBoatLink> {
    public RenderBoatLink(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityBoatLink link, double x, double y, double z, float entityYaw, float partialTicks) {
        EntityBoat leader = link.getLeader();
        EntityBoat follower = link.getFollower();
        EntityPlayer player = Boatifull.proxy.getClientPlayer();

        if(leader == null || follower == null) {
            return;
        }

        //player coordinates
        double xP = player.prevPosX + partialTicks*(player.posX - player.prevPosX);
        double yP = player.prevPosY + partialTicks*(player.posY - player.prevPosY);
        double zP = player.prevPosZ + partialTicks*(player.posZ - player.prevPosZ);

        //follower coordinates & orientation
        double xF = follower.prevPosX + partialTicks*(follower.posX - follower.prevPosX);
        double yF = follower.prevPosY + partialTicks*(follower.posY - follower.prevPosY);
        double zF = follower.prevPosZ + partialTicks*(follower.posZ - follower.prevPosZ);
        double yawF = follower.prevRotationYaw + partialTicks*(follower.rotationYaw - follower.prevRotationYaw);
        double cosF = Math.cos(Math.toRadians(yawF));
        double sinF = Math.sin(Math.toRadians(yawF));

        //leader coordinates & orientation
        double xL = leader.prevPosX + partialTicks*(leader.posX - leader.prevPosX);
        double yL = leader.prevPosY + partialTicks*(leader.posY - leader.prevPosY);
        double zL = leader.prevPosZ + partialTicks*(leader.posZ - leader.prevPosZ);
        double yawL = leader.prevRotationYaw + partialTicks*(leader.rotationYaw - leader.prevRotationYaw);
        double cosL = Math.cos(Math.toRadians(yawL));
        double sinL = Math.sin(Math.toRadians(yawL));

        //position of rope coupling point relative to the boat's coordinate system
        double dX = 0;
        double dY = 0.565;
        double dZ = 1;

        //starting point of the cable (front of follower)
        double x1 = dX*cosF - dZ*sinF;
        double y1 = 0;
        double z1 = dX*sinF + dZ*cosF;

        //end point of the cable (back of leader)
        double x2 = xL - xF + dX*cosL + dZ*sinL;
        double y2 = yL - yF;
        double z2 = zL - zF + dX*sinL - dZ*cosL;

        //amplitude of the cable slack
        //double a = this.getAmplitude(leader.getDistanceToEntity(follower));
        double a = this.getAmplitude(Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2) + (z1 - z2)*(z1 - z2)));

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        //translate to the center of the following boat
        GlStateManager.translate(xF - xP, yF - yP + dY, zF - zP);

        //disable texture and lighting
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GL11.glLineWidth(10);

        //draw the rope
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        byte n = 16;
        for(int i = 0; i <= n; i++) {
            float t = (float) i / (float) n;
            buffer.pos(x1 + t*(x2 - x1), y1 + t*(y2 - y1) - a * MathHelper.sin((float) Math.PI * t), z1 + t*(z2 - z1)).color(112, 65, 34, 255).endVertex();
        }

        tessellator.draw();

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    private double getAmplitude(double d) {
        double l = BoatLinker.LINK_RANGE;
        if(d == 0) {
            return l / 2.0;
        }
        if(d >= l) {
            return 0;
        }
        return l / 2.0 - d / 2.0;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBoatLink entity) {
        return null;
    }
}
