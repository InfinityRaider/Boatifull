package com.infinityraider.boatifull.boatlinking;

import net.minecraft.entity.item.EntityBoat;

public interface IBoatLink {
    EntityBoat getFollower();

    EntityBoat getLeader();

    void breakLink();
}
