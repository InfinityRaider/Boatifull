package com.infinityraider.boatifull.entity;

import com.infinityraider.boatifull.boatlinking.BoatIdProvider;
import com.infinityraider.boatifull.boatlinking.BoatLinker;
import com.infinityraider.boatifull.boatlinking.IBoatLink;
import com.infinityraider.boatifull.reference.Names;
import com.infinityraider.boatifull.render.RenderBoatLink;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class EntityBoatLink extends Entity implements IBoatLink, IEntityAdditionalSpawnData {
    private int ownerId;
    private EntityBoat owner;
    private int leaderId;
    private EntityBoat leader;

    public EntityBoatLink(World world) {
        super(world);
        this.setSize(1.375F, 0.5625F);
    }

    public EntityBoatLink(EntityBoat leader, EntityBoat follower) {
        this(leader.getEntityWorld());
        this.ownerId = BoatIdProvider.getBoatId(follower);
        this.owner = follower;
        this.leaderId =  BoatIdProvider.getBoatId(leader);
        this.leader = leader;
        this.copyLocationAndAnglesFrom(follower);
    }

    @Override
    protected void entityInit() {}

    @Override
    public EntityBoat getFollower() {
        if(this.owner == null) {
            this.owner = BoatIdProvider.getBoatFromId(this.ownerId);
        }
        return owner;
    }

    public EntityBoat getLeader() {
        if(this.leader == null) {
            this.leader = BoatIdProvider.getBoatFromId(this.leaderId);
        }
        return leader;
    }

    public void mountFollower() {
        this.getFollower().startRiding(this);
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBox(Entity entity) {
        if(this.getFollower() == null) {
            return null;
        }
        if(entity.getEntityWorld().isRemote) {
            return entity instanceof EntityBoat ? null : entity.getEntityBoundingBox();
        } else {
            return entity == this.getFollower() ? null : entity.getEntityBoundingBox();
        }
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox() {
        return this.getEntityBoundingBox();
    }

    @Override
    public boolean canBePushed()
    {
        return true;
    }

    @Override
    public double getMountedYOffset()
    {
        return 0;
    }

    @Override
    public void applyEntityCollision(Entity entity) {
        if(entity == this.getFollower()) {
            return;
        }
        if (entity instanceof EntityBoat) {
            if (entity.getEntityBoundingBox().minY < this.getEntityBoundingBox().maxY) {
                super.applyEntityCollision(entity);
            }
        }
        else if (entity.getEntityBoundingBox().minY <= this.getEntityBoundingBox().minY) {
            super.applyEntityCollision(entity);
        }
    }

    @Override
    public void breakLink() {
        this.setDead();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if(this.ownerId < 0) {
            this.setDead();
        }
    }

    @Override
    public void setDead() {
        if(!this.worldObj.isRemote) {
            BoatLinker.getInstance().unlinkBoat(this.getFollower());
        }
        if(this.getFollower() != null) {
            this.getFollower().dismountRidingEntity();
        }
        super.setDead();
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        this.leaderId = tag.getInteger(Names.NBT.LEADER);
        this.ownerId = tag.getInteger(Names.NBT.OWNER);
        if(!this.getEntityWorld().isRemote) {
            BoatLinker.getInstance().validateBoatLink(this);
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger(Names.NBT.LEADER, this.leaderId);
        tag.setInteger(Names.NBT.OWNER, this.ownerId);
    }

    @Override
    public void writeSpawnData(ByteBuf buf) {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeEntityToNBT(tag);
        ByteBufUtils.writeTag(buf, tag);
    }

    @Override
    public void readSpawnData(ByteBuf buf) {
        this.readEntityFromNBT(ByteBufUtils.readTag(buf));
    }

    public static class RenderFactory implements IRenderFactory<EntityBoatLink> {
        public static RenderFactory FACTORY = new RenderFactory();

        private RenderFactory() {}

        @Override
        @SideOnly(Side.CLIENT)
        public Render<? super EntityBoatLink> createRenderFor(RenderManager manager) {
            return new RenderBoatLink(manager);
        }
    }
}
