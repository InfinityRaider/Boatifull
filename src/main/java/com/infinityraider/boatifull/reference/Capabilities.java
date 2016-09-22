package com.infinityraider.boatifull.reference;

import com.infinityraider.boatifull.boatlinking.IBoatLinkData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class Capabilities {
    @CapabilityInject(IBoatLinkData.class)
    public static Capability<IBoatLinkData> CAPABILITY_LINKED_BOATS = null;
}
