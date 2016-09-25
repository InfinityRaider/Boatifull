package com.infinityraider.boatifull.entity;

import com.infinityraider.boatifull.render.RenderEntityBoatChest;
import com.infinityraider.infinitylib.utility.inventory.IInventorySerializableItemHandler;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class EntityBoatChest extends EntityBoat implements IInventorySerializableItemHandler {
    private static final int INVENTORY_SIZE = 27;

    private ItemStack[] inventory;

    /** Constructor which is used to instantiate the entity client side or server side after a world reload using reflection */
    @SuppressWarnings("unused")
    public EntityBoatChest(World world) {
        super(world);
        this.inventory = new ItemStack[INVENTORY_SIZE];
    }

    public EntityBoatChest(World world, double x, double y, double z) {
        super(world, x, y, z);
        this.inventory = new ItemStack[INVENTORY_SIZE];
    }

    public EntityBoatChest(EntityBoat boat) {
        this(boat.getEntityWorld(), boat.prevPosX, boat.prevPosY, boat.prevPosZ);
        this.posX = boat.posX;
        this.posY = boat.posY;
        this.posZ = boat.posZ;
        this.motionX = boat.motionX;
        this.motionY = boat.motionY;
        this.motionZ = boat.motionZ;
        this.rotationPitch = boat.rotationPitch;
        this.rotationYaw = boat.rotationYaw;
        this.setBoatType(boat.getBoatType());
    }

    /**
     * Overridden to drop all the items in the inventory too
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else if (!this.worldObj.isRemote && !this.isDead) {
            if (source instanceof EntityDamageSourceIndirect && source.getEntity() != null && this.isPassenger(source.getEntity())) {
                return false;
            } else {
                this.setForwardDirection(-this.getForwardDirection());
                this.setTimeSinceHit(10);
                this.setDamageTaken(this.getDamageTaken() + amount * 10.0F);
                this.setBeenAttacked();
                boolean flag = source.getEntity() instanceof EntityPlayer && ((EntityPlayer)source.getEntity()).capabilities.isCreativeMode;
                if (flag || this.getDamageTaken() > 40.0F) {
                    if (!flag && this.worldObj.getGameRules().getBoolean("doEntityDrops")) {
                        this.dropItems();
                    }
                    this.setDead();
                }
                return true;
            }
        } else {
            return true;
        }
    }

    public void dropItems() {
        this.dropItemWithOffset(this.getItemBoat(), 1, 0.0F);
        this.entityDropItem(new ItemStack(Blocks.CHEST, 1), 0.0F);
        for(int i = 0; i < this.getSizeInventory(); i ++) {
            ItemStack stack = this.getStackInSlot(i);
            if(stack != null && stack.stackSize > 0) {
                this.entityDropItem(stack, 0.0F);
            }
        }
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand) {
        if(!this.worldObj.isRemote && !player.isSneaking()) {
            player.displayGUIChest(this);
            player.addStat(StatList.CHEST_OPENED);
        }
        return true;
    }

    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return false;
    }

    @Nullable
    public Entity getControllingPassenger() {
        return null;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        this.writeInventoryToNBT(tag);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        this.readInventoryFromNBT(tag);
    }


    /**
     * --------------------
     * Capability overrides
     * --------------------
     */

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) this : super.getCapability(capability, facing);
    }


    /**
     * -----------------
     * Inventory Methods
     * -----------------
     */

    @Override
    public int getSizeInventory() {
        return this.inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.isValidSlot(slot) ? this.inventory[slot] : null;
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(int index, int count) {
        return this.extractItem(index, count, false);
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int index) {
        if(this.isValidSlot(index)) {
            ItemStack stack = this.inventory[index];
            this.setInventorySlotContents(index, null);
            return stack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
        if(this.isValidSlot(index)) {
            this.inventory[index] = stack;
            this.markDirty();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.inventory = new ItemStack[this.getSlots()];
    }

    public static class RenderFactory implements IRenderFactory<EntityBoatChest> {
        public static final RenderFactory FACTORY = new RenderFactory();

        private RenderFactory() {}

        @Override
        @SideOnly(Side.CLIENT)
        public Render<? super EntityBoatChest> createRenderFor(RenderManager manager) {
            return new RenderEntityBoatChest(manager);
        }
    }
}
