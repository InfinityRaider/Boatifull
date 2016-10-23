package com.infinityraider.boatifull.boatlinking;

import com.google.common.primitives.Ints;
import com.infinityraider.boatifull.Boatifull;
import com.infinityraider.boatifull.network.MessageRequestBoatSync;
import com.infinityraider.boatifull.reference.Names;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;

public class BoatId implements IBoatId {
    private static int largestId = -1;
    private static final Set<Integer> FREE_IDS = new HashSet<>();
    private static final Map<Integer, EntityBoat> BOATS = new HashMap<>();

    /** The boat owning this property */
    private EntityBoat owner;

    /** The persistent id for this boat */
    private int id = -1;

    @Override
    public IBoatId setBoat(EntityBoat boat) {
        if(this.owner == null) {
            this.owner = boat;
            if (!boat.getEntityWorld().isRemote) {
                this.id = getNextId();
                BOATS.put(this.getId(), this.getBoat());
            } else {
                Boatifull.instance.getNetworkWrapper().sendToServer(new MessageRequestBoatSync(this.owner));
            }
        }
        return this;
    }

    @Override
    public EntityBoat getBoat() {
        return this.owner;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(Names.NBT.ID, this.getId());
        tag.setInteger(Names.NBT.LEADER, largestId);
        tag.setIntArray(Names.NBT.OWNER, Ints.toArray(FREE_IDS));
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        this.id = tag.getInteger(Names.NBT.ID);
        largestId = tag.getInteger(Names.NBT.LEADER);
        int[] freeIds = tag.getIntArray(Names.NBT.OWNER);
        FREE_IDS.clear();
        for(int id : freeIds) {
            FREE_IDS.add(id);
        }
        BOATS.put(this.getId(), this.getBoat());
    }

    private static int getNextId() {
        int id;
        if(FREE_IDS.isEmpty()) {
            id = largestId + 1;
            largestId = id;
        } else {
            Iterator<Integer> iterator = FREE_IDS.iterator();
            id = iterator.next();
            FREE_IDS.remove(id);
        }
        return id;
    }

    public static EntityBoat getBoatFromId(int id) {
        return BOATS.get(id);
    }

    public static void onBoatRemoved(EntityBoat boat) {
        if(boat != null && !boat.getEntityWorld().isRemote) {
            FREE_IDS.add(CapabilityBoatId.getBoatId(boat));
        }
    }
}
