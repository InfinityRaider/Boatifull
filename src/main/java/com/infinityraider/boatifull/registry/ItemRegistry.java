package com.infinityraider.boatifull.registry;

import com.infinityraider.boatifull.item.ItemDebugger;
import net.minecraft.item.Item;

public class ItemRegistry {
    private static final ItemRegistry INSTANCE = new ItemRegistry();

    public static ItemRegistry getInstance() {
        return INSTANCE;
    }

    private ItemRegistry() {
        this.debugger = new ItemDebugger();
    }

    public final Item debugger;
}
