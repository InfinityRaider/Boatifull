package com.infinityraider.boatifull.boatlinking;

import net.minecraft.entity.item.EntityBoat;
import net.minecraft.nbt.NBTTagCompound;

/**
 * This is necessary for persistent boat links between server restarts
 * Boats seem to change entity id when the server restarts, this adds a fixed id to all boats in order to identify each boat
 */
public interface IBoatId {
    /**
     * sets the boat of which this is the property,
     * internal use only, do not call this method
     */
    IBoatId setBoat(EntityBoat boat);

    /**
     * @return The boat to which this link data is applied
     */
    EntityBoat getBoat();

    /**
     * @return the unique boat id for this boat
     */
    int getId();

    /**
     * Writes this boat link data to nbt
     * @return the nbt tag holding the data serialized from this object
     */
    NBTTagCompound writeToNBT();

    /**
     * Reads this boat link data from nbt
     * @param tag an nbt tag holding the data serialized for this object
     */
    void readFromNBT(NBTTagCompound tag);
}
