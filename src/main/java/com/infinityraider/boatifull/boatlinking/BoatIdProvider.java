package com.infinityraider.boatifull.boatlinking;

import com.infinityraider.boatifull.reference.Capabilities;
import com.infinityraider.boatifull.reference.Reference;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nullable;

public class BoatIdProvider implements ICapabilitySerializable<NBTTagCompound> {
    public static final ResourceLocation KEY = new ResourceLocation(Reference.MOD_ID, "boat_link");

    private IBoatId boatId;

    public BoatIdProvider(EntityBoat boat) {
        this.boatId = Capabilities.CAPABILITY_BOAT_ID != null ? Capabilities.CAPABILITY_BOAT_ID.getDefaultInstance().setBoat(boat) : null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return Capabilities.CAPABILITY_BOAT_ID != null && capability == Capabilities.CAPABILITY_BOAT_ID;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return hasCapability(capability, facing) ? (T) boatId : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) Capabilities.CAPABILITY_BOAT_ID.getStorage().writeNBT(Capabilities.CAPABILITY_BOAT_ID, boatId, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        Capabilities.CAPABILITY_BOAT_ID.getStorage().readNBT(Capabilities.CAPABILITY_BOAT_ID, boatId, null, nbt);
    }

    public static class Storage implements Capability.IStorage<IBoatId> {
        @Override
        public NBTBase writeNBT(Capability<IBoatId> capability, IBoatId instance, EnumFacing side) {
            return instance != null ? instance.writeToNBT() : null;
        }

        @Override
        public void readNBT(Capability<IBoatId> capability, IBoatId instance, EnumFacing side, NBTBase nbt) {
            if(instance != null && (nbt instanceof NBTTagCompound)) {
                instance.readFromNBT((NBTTagCompound) nbt);
            }
        }
    }

    public static int getBoatId(EntityBoat boat) {
        IBoatId id = getBoatIdData(boat);
        return id == null ? -1 : id.getId();
    }

    public static IBoatId getBoatIdData(EntityBoat boat) {
        return boat.hasCapability(Capabilities.CAPABILITY_BOAT_ID, null) ? boat.getCapability(Capabilities.CAPABILITY_BOAT_ID, null) : null;
    }

    public static EntityBoat getBoatFromId(int id) {
        return BoatId.getBoatFromId(id);
    }
}
