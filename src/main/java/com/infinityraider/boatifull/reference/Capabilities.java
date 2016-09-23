package com.infinityraider.boatifull.reference;

import com.infinityraider.boatifull.boatlinking.IBoatId;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class Capabilities {
    @CapabilityInject(IBoatId.class)
    public static Capability<IBoatId> CAPABILITY_BOAT_ID = null;
}
