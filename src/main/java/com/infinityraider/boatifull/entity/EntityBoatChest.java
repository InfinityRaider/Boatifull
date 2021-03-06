package com.infinityraider.boatifull.entity;

import com.infinityraider.boatifull.render.RenderEntityBoatChest;
import com.infinityraider.infinitylib.utility.inventory.IInventorySerializableItemHandler;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class EntityBoatChest extends EntityBoat implements IInventorySerializableItemHandler {
    private static DataParameter<Integer> DATA_PLAYERS_USING = EntityDataManager.createKey(EntityBoatChest.class, DataSerializers.VARINT);

    private static final int INVENTORY_SIZE = 27;

    private NonNullList<ItemStack> inventory;

    private float lidAngle;
    private float prevLidAngle;

    /** Constructor which is used to instantiate the entity client side or server side after a world reload using reflection */
    @SuppressWarnings("unused")
    public EntityBoatChest(World world) {
        super(world);
        this.inventory = NonNullList.withSize(27, ItemStack.EMPTY);
        this.setSize(1.375F, 0.8625F);
    }

    public EntityBoatChest(World world, double x, double y, double z) {
        super(world, x, y, z);
        this.inventory = NonNullList.withSize(27, ItemStack.EMPTY);
        this.setSize(1.375F, 0.8625F);
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

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(DATA_PLAYERS_USING, 0);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.updateLidAngle();
    }

    protected void updateLidAngle() {
        this.prevLidAngle = this.lidAngle;
        int numPlayersUsing = this.getPlayersUsing();
        if (numPlayersUsing > 0 && this.lidAngle == 0.0F) {
            this.getEntityWorld().playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.getEntityWorld().rand.nextFloat() * 0.1F + 0.9F);
        }
        if (numPlayersUsing == 0 && this.lidAngle > 0.0F || numPlayersUsing > 0 && this.lidAngle < 1.0F) {
            float oldAngle = this.lidAngle;
            if (numPlayersUsing > 0) {
                this.lidAngle += 0.1F;
            } else {
                this.lidAngle -= 0.1F;
            }
            if (this.lidAngle > 1.0F) {
                this.lidAngle = 1.0F;
            }
            float maxAngle = 0.5F;
            if (this.lidAngle < maxAngle && oldAngle >= maxAngle) {
                this.getEntityWorld().playSound(null, this.posX, this.posY, this.posZ, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.getEntityWorld().rand.nextFloat() * 0.1F + 0.9F);
            }
            if (this.lidAngle < 0.0F) {
                this.lidAngle = 0.0F;
            }
        }
    }

    /**
     * Overridden to drop all the items in the inventory too
     */
    @Override
    public boolean attackEntityFrom(@Nonnull DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else if (!this.getEntityWorld().isRemote && !this.isDead) {
            if (source instanceof EntityDamageSourceIndirect && source.getEntity() != null && this.isPassenger(source.getEntity())) {
                return false;
            } else {
                this.setForwardDirection(-this.getForwardDirection());
                this.setTimeSinceHit(10);
                this.setDamageTaken(this.getDamageTaken() + amount * 10.0F);
                this.setBeenAttacked();
                boolean flag = source.getEntity() instanceof EntityPlayer && ((EntityPlayer)source.getEntity()).capabilities.isCreativeMode;
                if (flag || this.getDamageTaken() > 40.0F) {
                    if (!flag && this.getEntityWorld().getGameRules().getBoolean("doEntityDrops")) {
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
            if(!stack.isEmpty() && stack.getCount() > 0) {
                this.entityDropItem(stack, 0.0F);
            }
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if(!this.getEntityWorld().isRemote && !player.isSneaking()) {
            player.displayGUIChest(this);
            player.addStat(StatList.CHEST_OPENED);
        }
        return true;
    }

    public int getPlayersUsing() {
        return this.getDataManager().get(DATA_PLAYERS_USING);
    }

    public void setPlayersUsing(int amount) {
        amount = amount < 0 ? 0 : amount;
        this.getDataManager().set(DATA_PLAYERS_USING, amount);
    }

    public float getLidAngle() {
        return this.lidAngle;
    }

    public float getPrevLidAngle() {
        return this.prevLidAngle;
    }

    @Override
    protected boolean canFitPassenger(@Nonnull Entity passenger) {
        return false;
    }

    @Nullable
    public Entity getControllingPassenger() {
        return null;
    }

    @Override
    protected void writeEntityToNBT(@Nonnull NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        this.writeInventoryToNBT(tag);
    }

    @Override
    protected void readEntityFromNBT(@Nonnull NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        this.readInventoryFromNBT(tag);
    }


    /**
     * --------------------
     * Capability overrides
     * --------------------
     */

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) this : super.getCapability(capability, facing);
    }


    /**
     * -----------------
     * Inventory Methods
     * -----------------
     */

    @Override
    public int getSizeInventory() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        return this.isValidSlot(slot) ? this.inventory.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int index, int count) {
        return this.extractItem(index, count, false);
    }

    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int index) {
        if(this.isValidSlot(index)) {
            ItemStack stack = this.inventory.get(index);
            this.setInventorySlotContents(index, ItemStack.EMPTY);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
        if(this.isValidSlot(index)) {
            this.inventory.set(index, stack);
            this.markDirty();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {
        if(!this.getEntityWorld().isRemote) {
            int amount = this.getPlayersUsing();
            amount = amount <= 0 ? 1 : amount + 1;
            this.setPlayersUsing(amount);
        }
    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {
        if(!this.getEntityWorld().isRemote) {
            int amount = this.getPlayersUsing();
            amount = amount <= 1 ? 0 : amount - 1;
            this.setPlayersUsing(amount);
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
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
        this.inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
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
