package com.infinityraider.boatifull.boatlinking;

import com.infinityraider.boatifull.Boatifull;
import com.infinityraider.boatifull.network.MessageSyncBoatLinkData;
import com.infinityraider.boatifull.reference.Names;
import com.infinityraider.infinitylib.network.NetworkWrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.nbt.NBTTagCompound;

public class BoatLinkData implements IBoatLinkData {
    //TODO: manually keep track of boat id registry, because vanilla mc seems to reset the ids on server resets

    /** The boat owning this property */
    private EntityBoat owner;

    /** The boat which this boat is tied to */
    private int leaderId;
    private EntityBoat leader;

    @Override
    public IBoatLinkData setBoat(EntityBoat boat) {
        this.owner = boat;
        this.leaderId = -1;
        return this;
    }

    @Override
    public EntityBoat getBoat() {
        return this.owner;
    }

    @Override
    public boolean hasLeadingBoat() {
        return this.leaderId >= 0;
    }

    @Override
    public EntityBoat getLeadingBoat() {
        if(this.hasLeadingBoat()) {
            if(this.leader == null) {
                Entity entity = Boatifull.proxy.getEntityById(getBoat().getEntityWorld(), this.leaderId);
                if(entity instanceof EntityBoat) {
                    this.leader = (EntityBoat) entity;
                }
            }
        } else {
            this.leader = null;
        }
        return this.leader;
    }

    @Override
    public IBoatLinkData setLeadingBoat(EntityBoat leader) {
        if(leader == null) {
            this.leaderId = -1;
        } else {
            this.leaderId = leader.getEntityId();
        }
        this.leader = leader;
        this.markDirty();
        return this;
    }

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(Names.NBT.LEADER, this.leaderId);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        this.leaderId = tag.getInteger(Names.NBT.LEADER);
    }

    @Override
    public void markDirty() {
        if(!this.getBoat().getEntityWorld().isRemote) {
            NetworkWrapper.getInstance().sendToAll(new MessageSyncBoatLinkData(this.getBoat()));
        }
    }
}
