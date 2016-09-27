package com.infinityraider.boatifull.handler;

import com.infinityraider.infinitylib.utility.LogHelper;
import com.infinityraider.infinitylib.utility.text.ItemStackParser;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationHandler {
    private static final ConfigurationHandler INSTANCE = new ConfigurationHandler();

    public static ConfigurationHandler getInstance() {
        return INSTANCE;
    }

    private static final Item DEFAULT_LINK_ITEM = Items.LEAD;
    private static final int DEFAULT_LINK_META = 0;

    private Configuration config;

    private String[] linkKeyItemString;

    private boolean allowChestBoat;

    public void init(FMLPreInitializationEvent event) {
        if (config == null) {
            config = new Configuration(event.getSuggestedConfigurationFile());
        }
        loadConfiguration();
        if (config.hasChanged()) {
            config.save();
        }
        LogHelper.debug("Configuration Loaded");
    }

    public List<ItemStack> getLinkKeyItems() {
        List<ItemStack> items = new ArrayList<>();
        for (String aLinkKeyItemString : linkKeyItemString) {
            ItemStack item = ItemStackParser.parseItemStack(aLinkKeyItemString);
            if (item != null) {
                items.add(item);
            }
        }
        if(items.size() <= 0) {
            items.add(new ItemStack(DEFAULT_LINK_ITEM, 1, DEFAULT_LINK_META));
        }
        return items;
    }

    public boolean allowChestBoat() {
        return this.allowChestBoat;
    }

    private void loadConfiguration() {
        this.linkKeyItemString = config.getStringList("Link key items", Categories.GENERAL.getName(),
                new String[] {DEFAULT_LINK_ITEM.getRegistryName().toString() + ":" + DEFAULT_LINK_META},
                "A list of all items which can be used to link boats together, metadata is optional and will be fuzzy if not specified. The first entry will act as the default in case of error");
        this.allowChestBoat = config.getBoolean("Enable chest boat", Categories.GENERAL.getName(),
                !Loader.isModLoaded("opentransport"),
                "Set to false to disable chest boats");
    }

    public enum Categories {
        GENERAL("general");

        private final String name;

        Categories(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
