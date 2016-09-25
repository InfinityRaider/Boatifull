package com.infinityraider.boatifull.render;

import com.infinityraider.boatifull.entity.EntityBoatChest;
import net.minecraft.client.model.IMultipassModel;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntityBoatChest extends Render<EntityBoatChest> {
    // Because the one in RenderBoat is private >>
    public static final ResourceLocation[] BOAT_TEXTURES = new ResourceLocation[] {
            new ResourceLocation("textures/entity/boat/boat_oak.png"),
            new ResourceLocation("textures/entity/boat/boat_spruce.png"),
            new ResourceLocation("textures/entity/boat/boat_birch.png"),
            new ResourceLocation("textures/entity/boat/boat_jungle.png"),
            new ResourceLocation("textures/entity/boat/boat_acacia.png"),
            new ResourceLocation("textures/entity/boat/boat_darkoak.png")};

    private static final TileEntityChest DUMMY_CHEST = new TileEntityChest();

    protected final ModelBoatNoPaddles model;

    public RenderEntityBoatChest(RenderManager renderManager) {
        super(renderManager);
        this.model = new ModelBoatNoPaddles();
        this.shadowSize = 0.5F;
    }

    @Override
    public void doRender(EntityBoatChest entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        this.setupTranslation(x, y, z);
        this.setupRotation(entity, entityYaw, partialTicks);
        this.bindEntityTexture(entity);

        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        this.model.render(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        this.renderChest(partialTicks);

        if (this.renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    public void renderChest(float partialTicks) {
        TileEntitySpecialRenderer<TileEntityChest> renderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(DUMMY_CHEST);
        if(renderer != null) {
            GlStateManager.pushMatrix();

            GlStateManager.translate(-0.5, 0.125, -0.5);
            GlStateManager.rotate(180, 1, 0, 0);
            GlStateManager.rotate(90, 0, 1, 0);

            renderer.renderTileEntityAt(DUMMY_CHEST, 0, 0, 0, partialTicks, -1);

            GlStateManager.popMatrix();
        }
    }

    public void setupRotation(EntityBoat boat, float entityYaw, float partialTicks) {
        GlStateManager.rotate(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
        float time = (float) boat.getTimeSinceHit() - partialTicks;
        float dmg = boat.getDamageTaken() - partialTicks;

        if (dmg < 0.0F) {
            dmg = 0.0F;
        }

        if (time > 0.0F) {
            GlStateManager.rotate(MathHelper.sin(time) * time * dmg / 10.0F * (float)boat.getForwardDirection(), 1.0F, 0.0F, 0.0F);
        }

        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
    }

    public void setupTranslation(double x, double y, double z) {
        GlStateManager.translate((float)x, (float)y + 0.375F, (float)z);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBoatChest entity) {
        return BOAT_TEXTURES[entity.getBoatType().ordinal()];
    }

    public boolean isMultipass()
    {
        return true;
    }

    @Override
    public void renderMultipass(EntityBoatChest boat, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        this.setupTranslation(x, y, z);
        this.setupRotation(boat, entityYaw, partialTicks);
        this.bindEntityTexture(boat);
        this.model.renderMultipass(boat, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }

    public static class ModelBoatNoPaddles extends ModelBase implements IMultipassModel {
        public ModelRenderer[] boatSides = new ModelRenderer[5];
        /** Part of the model rendered to make it seem like there's no water in the boat */
        public ModelRenderer noWater;

        public ModelBoatNoPaddles() {
            this.boatSides[0] = (new ModelRenderer(this, 0, 0)).setTextureSize(128, 64);
            this.boatSides[1] = (new ModelRenderer(this, 0, 19)).setTextureSize(128, 64);
            this.boatSides[2] = (new ModelRenderer(this, 0, 27)).setTextureSize(128, 64);
            this.boatSides[3] = (new ModelRenderer(this, 0, 35)).setTextureSize(128, 64);
            this.boatSides[4] = (new ModelRenderer(this, 0, 43)).setTextureSize(128, 64);
            this.boatSides[0].addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
            this.boatSides[0].setRotationPoint(0.0F, 3.0F, 1.0F);
            this.boatSides[1].addBox(-13.0F, -7.0F, -1.0F, 18, 6, 2, 0.0F);
            this.boatSides[1].setRotationPoint(-15.0F, 4.0F, 4.0F);
            this.boatSides[2].addBox(-8.0F, -7.0F, -1.0F, 16, 6, 2, 0.0F);
            this.boatSides[2].setRotationPoint(15.0F, 4.0F, 0.0F);
            this.boatSides[3].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
            this.boatSides[3].setRotationPoint(0.0F, 4.0F, -9.0F);
            this.boatSides[4].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
            this.boatSides[4].setRotationPoint(0.0F, 4.0F, 9.0F);
            this.boatSides[0].rotateAngleX = ((float)Math.PI / 2F);
            this.boatSides[1].rotateAngleY = ((float)Math.PI * 3F / 2F);
            this.boatSides[2].rotateAngleY = ((float)Math.PI / 2F);
            this.boatSides[3].rotateAngleY = (float)Math.PI;
            this.noWater = (new ModelRenderer(this, 0, 0)).setTextureSize(128, 64);
            this.noWater.addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
            this.noWater.setRotationPoint(0.0F, -3.0F, 1.0F);
            this.noWater.rotateAngleX = ((float)Math.PI / 2F);
        }

        public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
            for (int i = 0; i < 5; ++i) {
                this.boatSides[i].render(scale);
            }
        }

        public void renderMultipass(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.colorMask(false, false, false, false);
            this.noWater.render(scale);
            GlStateManager.colorMask(true, true, true, true);
        }

        public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {}
    }
}
