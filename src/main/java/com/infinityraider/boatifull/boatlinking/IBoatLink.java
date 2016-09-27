package com.infinityraider.boatifull.boatlinking;

import net.minecraft.entity.item.EntityBoat;
import net.minecraft.item.ItemStack;

public interface IBoatLink {
    EntityBoat getFollower();

    EntityBoat getLeader();

    void breakLink();

    ItemStack getLinkItem();
}
