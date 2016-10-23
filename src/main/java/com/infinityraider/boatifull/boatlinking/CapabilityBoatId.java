package com.infinityraider.boatifull.boatlinking;

import com.infinityraider.boatifull.reference.Reference;
import com.infinityraider.infinitylib.capability.ICapabilityImplementation;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class CapabilityBoatId implements ICapabilityImplementation<EntityBoat, IBoatId> {
    private static final CapabilityBoatId INSTANCE = new CapabilityBoatId();
    
    public static CapabilityBoatId getInstance() {
        return INSTANCE;
    }
    
    public static final ResourceLocation KEY = new ResourceLocation(Reference.MOD_ID, "boat_link");

    @CapabilityInject(IBoatId.class)
    public static Capability<IBoatId> CAPABILITY_BOAT_ID = null;
    
    private CapabilityBoatId() {}

    @Override
    public Capability<IBoatId> getCapability() {
        return CAPABILITY_BOAT_ID;
    }

    @Override
    public boolean shouldApplyCapability(EntityBoat carrier) {
        return true;
    }

    @Override
    public IBoatId onValueAddedToCarrier(IBoatId value, EntityBoat carrier) {
        return value.setBoat(carrier);
    }

    @Override
    public ResourceLocation getCapabilityKey() {
        return KEY;
    }

    @Override
    public Class<EntityBoat> getCarrierClass() {
        return EntityBoat.class;
    }

    @Override
    public Class<IBoatId> getCapabilityClass() {
        return IBoatId.class;
    }

    @Override
    public IBoatId call() throws Exception {
        return new BoatId();
    }

    public static int getBoatId(EntityBoat boat) {
        IBoatId id = getBoatIdData(boat);
        return id == null ? -1 : id.getId();
    }

    public static IBoatId getBoatIdData(EntityBoat boat) {
        return boat.hasCapability(CAPABILITY_BOAT_ID, null) ? boat.getCapability(CAPABILITY_BOAT_ID, null) : null;
    }

    public static EntityBoat getBoatFromId(int id) {
        return BoatId.getBoatFromId(id);
    }
}
