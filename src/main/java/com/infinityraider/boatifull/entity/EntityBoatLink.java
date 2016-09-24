package com.infinityraider.boatifull.entity;

import com.infinityraider.boatifull.boatlinking.BoatIdProvider;
import com.infinityraider.boatifull.boatlinking.BoatLinker;
import com.infinityraider.boatifull.boatlinking.IBoatLink;
import com.infinityraider.boatifull.reference.Names;
import com.infinityraider.boatifull.render.RenderBoatLink;
import com.infinityraider.infinitylib.network.MessageSetEntityDead;
import com.infinityraider.infinitylib.network.NetworkWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class EntityBoatLink extends Entity implements IBoatLink, IEntityAdditionalSpawnData {
    private int ownerId;
    private EntityBoat owner;
    private int leaderId;
    private EntityBoat leader;

    private int outOfControlTicks;
    private double waterLevel;
    private float boatGlide;
    private double lastYd;

    private EntityBoat.Status status;
    private EntityBoat.Status previousStatus;

    private boolean validated;

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
        super.applyEntityCollision(entity);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @Override
    public void breakLink() {
        this.setDead();
    }

    private void updateMotion() {
        double vY = this.func_189652_ae() ? 0.0D : -0.03999999910593033D;
        double vZ = 0.0D;
        float momentum = 0.05F;

        if (this.getFollower() != null && this.getLeader() != null) {
            double distance = this.getLeader().getDistanceToEntity(this.getFollower());
            if(distance > BoatLinker.LINK_RANGE) {
                double xL = this.getLeader().posX;
                double yL = this.getLeader().posY;
                double zL = this.getLeader().posZ;
                double xF = this.getFollower().posX;
                double yF = this.getFollower().posY;
                double zF = this.getFollower().posZ;

                Vec3d velocity = new Vec3d(xL - xF, yL - yF, zL - zF).normalize().scale(0.25 *(distance - BoatLinker.LINK_RANGE));
                this.motionX = velocity.xCoord;
                this.motionY = velocity.yCoord;
                this.motionZ = velocity.zCoord;

                float yaw = ((float) Math.toDegrees(Math.atan2(this.motionZ, this.motionX)) - 90 + 360) % 360;
                if(this.rotationYaw != yaw) {
                    float deltaYaw = yaw - this.rotationYaw;
                    if(deltaYaw > 180) {
                        deltaYaw = deltaYaw - 360;
                    } else if(deltaYaw < - 180) {
                        deltaYaw = deltaYaw + 360;
                    }
                    this.rotationYaw = this.rotationYaw + 0.35F*deltaYaw;
                    this.setRotation(this.rotationYaw, this.rotationPitch);
                }
            }
        }
        if (this.previousStatus == EntityBoat.Status.IN_AIR && this.status != EntityBoat.Status.IN_AIR && this.status != EntityBoat.Status.ON_LAND) {
            this.waterLevel = this.getEntityBoundingBox().minY + (double) this.height;
            this.setPosition(this.posX, (double) (this.getWaterLevelAbove() - this.height) + 0.101D, this.posZ);
            this.motionY = 0.0D;
            this.lastYd = 0.0D;
            this.status = EntityBoat.Status.IN_WATER;
        } else {
            if (this.status == EntityBoat.Status.IN_WATER) {
                vZ = (this.waterLevel - this.getEntityBoundingBox().minY) / (double) this.height;
                momentum = 0.9F;
            } else if (this.status == EntityBoat.Status.UNDER_FLOWING_WATER) {
                vY = -7.0E-4D;
                momentum = 0.9F;
            } else if (this.status == EntityBoat.Status.UNDER_WATER) {
                vZ = 0.009999999776482582D;
                momentum = 0.45F;
            } else if (this.status == EntityBoat.Status.IN_AIR) {
                momentum = 0.9F;
            } else if (this.status == EntityBoat.Status.ON_LAND) {
                momentum = this.boatGlide;
            }

            this.motionX *= (double) momentum;
            this.motionZ *= (double) momentum;
            this.motionY += vY;

            if (vZ > 0.0D) {
                this.motionY += vZ * 0.06153846016296973D;
                this.motionY *= 0.75D;
            }
        }

    }

    @Override
    public void onUpdate() {
        if(this.ownerId < 0) {
            this.setDead();
            return;
        }
        if(!this.validated) {
            this.validated = BoatLinker.getInstance().validateBoatLink(this);
        }
        if(!this.worldObj.isRemote && this.getFollower() == null && this.validated) {
            this.setDead();
            return;
        }

        this.previousStatus = this.status;
        this.status = this.getBoatStatus();

        if (this.status != EntityBoat.Status.UNDER_WATER && this.status != EntityBoat.Status.UNDER_FLOWING_WATER) {
            this.outOfControlTicks = 0;
        } else {
            ++this.outOfControlTicks;
        }

        if (!this.worldObj.isRemote && this.outOfControlTicks >= 60) {
            this.breakLink();
        }

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        super.onUpdate();

        if(!this.getEntityWorld().isRemote) {
            this.updateMotion();
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
        }

        this.doBlockCollisions();

        List<Entity> list = this.worldObj.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(0.20000000298023224D, -0.009999999776482582D, 0.20000000298023224D), EntitySelectors.getTeamCollisionPredicate(this));
        if (!list.isEmpty()) {
            list.stream().filter(entity -> !entity.isPassenger(this)).forEach(this::applyEntityCollision);
        }

        if(!this.getEntityWorld().isRemote && this.getFollower() != null) {
            this.getFollower().rotationYaw = this.rotationYaw;
        }
    }

    private EntityBoat.Status getBoatStatus() {
        EntityBoat.Status status = this.getUnderwaterStatus();

        if (status != null) {
            this.waterLevel = this.getEntityBoundingBox().maxY;
            return status;
        } else if (this.checkInWater()) {
            return EntityBoat.Status.IN_WATER;
        } else {
            float f = this.getFollower() == null ? 0 : this.getFollower().getBoatGlide();
            if (f > 0.0F) {
                this.boatGlide = f;
                return EntityBoat.Status.ON_LAND;
            } else {
                return EntityBoat.Status.IN_AIR;
            }
        }
    }

    private boolean checkInWater() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        int xMin = MathHelper.floor_double(axisalignedbb.minX);
        int xMax = MathHelper.ceiling_double_int(axisalignedbb.maxX);
        int yMin = MathHelper.floor_double(axisalignedbb.minY);
        int yMax = MathHelper.ceiling_double_int(axisalignedbb.minY + 0.001D);
        int zMin = MathHelper.floor_double(axisalignedbb.minZ);
        int zMax = MathHelper.ceiling_double_int(axisalignedbb.maxZ);
        boolean flag = false;
        this.waterLevel = Double.MIN_VALUE;
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
        try {
            for (int x = xMin; x < xMax; ++x) {
                for (int y = yMin; y < yMax; ++y) {
                    for (int z = zMin; z < zMax; ++z) {
                        pos.setPos(x, y, z);
                        IBlockState iblockstate = this.worldObj.getBlockState(pos);
                        if (iblockstate.getMaterial() == Material.WATER) {
                            float f = EntityBoat.getLiquidHeight(iblockstate, this.worldObj, pos);
                            this.waterLevel = Math.max((double)f, this.waterLevel);
                            flag |= axisalignedbb.minY < (double)f;
                        }
                    }
                }
            }
        }
        finally {
            pos.release();
        }
        return flag;
    }

    private float getWaterLevelAbove() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        int xMin = MathHelper.floor_double(axisalignedbb.minX);
        int xMax = MathHelper.ceiling_double_int(axisalignedbb.maxX);
        int yMin = MathHelper.floor_double(axisalignedbb.maxY);
        int yMax = MathHelper.ceiling_double_int(axisalignedbb.maxY - this.lastYd);
        int zMin = MathHelper.floor_double(axisalignedbb.minZ);
        int zMax = MathHelper.ceiling_double_int(axisalignedbb.maxZ);
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
        try {
            label:
            for (int y = yMin; y < yMax; ++y) {
                float f = 0.0F;
                int x = xMin;
                while (true) {
                    if (x >= xMax) {
                        if (f < 1.0F) {
                            return (float)pos.getY() + f;
                        }
                        break;
                    }
                    for (int z = zMin; z < zMax; ++z) {
                        pos.setPos(x, y, z);
                        IBlockState iblockstate = this.worldObj.getBlockState(pos);
                        if (iblockstate.getMaterial() == Material.WATER) {
                            f = Math.max(f, EntityBoat.getBlockLiquidHeight(iblockstate, this.worldObj, pos));
                        }
                        if (f >= 1.0F) {
                            continue label;
                        }
                    }
                    ++x;
                }
            }
            return (float)(yMax + 1);
        }
        finally {
            pos.release();
        }
    }

    @Nullable
    private EntityBoat.Status getUnderwaterStatus() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        double yTop = axisalignedbb.maxY + 0.001D;
        int xMin = MathHelper.floor_double(axisalignedbb.minX);
        int xMax = MathHelper.ceiling_double_int(axisalignedbb.maxX);
        int yMin = MathHelper.floor_double(axisalignedbb.maxY);
        int yMax = MathHelper.ceiling_double_int(yTop);
        int zMin = MathHelper.floor_double(axisalignedbb.minZ);
        int zMax = MathHelper.ceiling_double_int(axisalignedbb.maxZ);
        boolean flag = false;
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
        try {
            for (int x = xMin; x < xMax; ++x) {
                for (int y = yMin; y < yMax; ++y) {
                    for (int z = zMin; z < zMax; ++z) {
                        pos.setPos(x, y, z);
                        IBlockState iblockstate = this.worldObj.getBlockState(pos);
                        if (iblockstate.getMaterial() == Material.WATER && yTop < (double) EntityBoat.getLiquidHeight(iblockstate, this.worldObj, pos)) {
                            if (iblockstate.getValue(BlockLiquid.LEVEL) != 0) {
                                return EntityBoat.Status.UNDER_FLOWING_WATER;
                            }
                            flag = true;
                        }
                    }
                }
            }
        } finally {
            pos.release();
        }
        return flag ? EntityBoat.Status.UNDER_WATER : null;
    }

    @Override
    public void setDead() {
        if(!this.worldObj.isRemote) {
            BoatLinker.getInstance().unlinkBoat(this.getFollower());
            NetworkWrapper.getInstance().sendToAll(new MessageSetEntityDead(this));
        }
        if(this.getFollower() != null) {
            this.getFollower().dismountRidingEntity();
        }
        super.setDead();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return this.getFollower() != null && this.getFollower().attackEntityFrom(source, amount);
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand) {
        return this.getFollower() == null || this.getFollower().processInitialInteract(player, stack, hand);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        this.leaderId = tag.getInteger(Names.NBT.LEADER);
        this.ownerId = tag.getInteger(Names.NBT.OWNER);
        if(!this.getEntityWorld().isRemote) {
            this.validated = BoatLinker.getInstance().validateBoatLink(this);
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
