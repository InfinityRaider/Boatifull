package com.infinityraider.boatifull.registry;

import com.infinityraider.boatifull.entity.EntityBoatChest;
import com.infinityraider.boatifull.entity.EntityBoatLink;
import com.infinityraider.boatifull.handler.ConfigurationHandler;
import com.infinityraider.boatifull.reference.Reference;
import com.infinityraider.infinitylib.entity.EntityRegistryEntry;

public class EntityRegistry {
    private static final EntityRegistry INSTANCE = new EntityRegistry();

    public static EntityRegistry getInstance() {
        return INSTANCE;
    }

    public final EntityRegistryEntry<EntityBoatLink> entityBoatLink;
    public final EntityRegistryEntry<EntityBoatChest> entityBoatChest;

    private EntityRegistry() {
        this.entityBoatLink = new EntityRegistryEntry<>(EntityBoatLink.class, Reference.MOD_ID.toLowerCase() + ".entityBoatLink")
                .setTrackingDistance(64)
                .setVelocityUpdates(true)
                .setUpdateFrequency(1)
                .setRenderFactory(EntityBoatLink.RenderFactory.FACTORY);

        this.entityBoatChest = new EntityRegistryEntry<>(EntityBoatChest.class, Reference.MOD_ID.toLowerCase() + ".entityBoatChest")
                .setTrackingDistance(64)
                .setVelocityUpdates(true)
                .setUpdateFrequency(1)
                .setRenderFactory(EntityBoatChest.RenderFactory.FACTORY)
                .enable(ConfigurationHandler.getInstance().allowChestBoat());
    }
}
