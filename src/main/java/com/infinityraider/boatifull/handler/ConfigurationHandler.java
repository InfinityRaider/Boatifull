package com.infinityraider.boatifull.handler;

import com.infinityraider.infinitylib.utility.LogHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.oredict.OreDictionary;

public class ConfigurationHandler {
    private static final ConfigurationHandler INSTANCE = new ConfigurationHandler();

    public static ConfigurationHandler getInstance() {
        return INSTANCE;
    }

    private static final Item DEFAULT_LINK_ITEM = Items.LEAD;
    private static final int DEFAULT_LINK_META = 0;

    private Configuration config;

    private String linkKeyItemString;
    private int linkKeyMeta;

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

    public Item getLinkKeyItem() {
        Item item = Item.REGISTRY.getObject(new ResourceLocation(this.linkKeyItemString));
        return item == null ? DEFAULT_LINK_ITEM : item;
    }

    public int getLinkKeyMeta() {
        return linkKeyMeta < 0 ? DEFAULT_LINK_META : this.linkKeyMeta;
    }

    private void loadConfiguration() {
        this.linkKeyItemString = config.getString("Link key item", Categories.GENERAL.getName(), DEFAULT_LINK_ITEM.getRegistryName().toString(), "The registry id for the link key item");
        this.linkKeyMeta = config.getInt("Link key meta", Categories.GENERAL.getName(), DEFAULT_LINK_META, 0, OreDictionary.WILDCARD_VALUE, "The metadata for the link key item, use " + OreDictionary.WILDCARD_VALUE + " for fuzzy metadata");
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
