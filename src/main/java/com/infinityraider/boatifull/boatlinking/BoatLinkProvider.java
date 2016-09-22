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

public class BoatLinkProvider implements ICapabilitySerializable<NBTTagCompound> {
    public static final ResourceLocation KEY = new ResourceLocation(Reference.MOD_ID, "boat_link");

    private IBoatLinkData linkedBoats;

    public BoatLinkProvider(EntityBoat boat) {
        this.linkedBoats = Capabilities.CAPABILITY_LINKED_BOATS != null ? Capabilities.CAPABILITY_LINKED_BOATS.getDefaultInstance().setBoat(boat) : null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return Capabilities.CAPABILITY_LINKED_BOATS != null && capability == Capabilities.CAPABILITY_LINKED_BOATS;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return hasCapability(capability, facing) ? (T) linkedBoats : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) Capabilities.CAPABILITY_LINKED_BOATS.getStorage().writeNBT(Capabilities.CAPABILITY_LINKED_BOATS, linkedBoats, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        Capabilities.CAPABILITY_LINKED_BOATS.getStorage().readNBT(Capabilities.CAPABILITY_LINKED_BOATS, linkedBoats, null, nbt);
    }

    public static class Storage implements Capability.IStorage<IBoatLinkData> {
        @Override
        public NBTBase writeNBT(Capability<IBoatLinkData> capability, IBoatLinkData instance, EnumFacing side) {
            return instance != null ? instance.writeToNBT() : null;
        }

        @Override
        public void readNBT(Capability<IBoatLinkData> capability, IBoatLinkData instance, EnumFacing side, NBTBase nbt) {
            if(instance != null && (nbt instanceof NBTTagCompound)) {
                instance.readFromNBT((NBTTagCompound) nbt);
            }
        }
    }

    public static IBoatLinkData getLinkedBoats(EntityBoat boat) {
        return boat.hasCapability(Capabilities.CAPABILITY_LINKED_BOATS, null) ? boat.getCapability(Capabilities.CAPABILITY_LINKED_BOATS, null) : null;
    }
}
