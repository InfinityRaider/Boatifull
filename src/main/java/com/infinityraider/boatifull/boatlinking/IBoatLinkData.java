package com.infinityraider.boatifull.boatlinking;

import net.minecraft.entity.item.EntityBoat;
import net.minecraft.nbt.NBTTagCompound;

public interface IBoatLinkData {
    /**
     * sets the boat of which this is the property,
     * internal use only, do not call this method
     */
    IBoatLinkData setBoat(EntityBoat boat);

    /**
     * @return The boat to which this link data is applied
     */
    EntityBoat getBoat();

    /**
     * @return if this boat is following another boat
     */
    boolean hasLeadingBoat();

    /**
     * when tied to another boat, this boat will follow that boat.
     * The boat this boat has been tied to is the leader for this boat.
     * @return the boat leading this boat, can be null
     */
    EntityBoat getLeadingBoat();

    /**
     * sets the leading boat for this link data
     * @return this
     */
    IBoatLinkData setLeadingBoat(EntityBoat leader);

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

    /**
     * Call on the server to sync the link data to the client, used to render the links between boats
     */
    void markDirty();
}
