package com.infinityraider.boatifull.handler;

import com.infinityraider.boatifull.Boatifull;
import com.infinityraider.infinitylib.utility.text.ItemStackParser;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfigurationHandler {
    private static final ConfigurationHandler INSTANCE = new ConfigurationHandler();

    public static ConfigurationHandler getInstance() {
        return INSTANCE;
    }

    private static final Item DEFAULT_LINK_ITEM = Items.LEAD;
    private static final int DEFAULT_LINK_META = 0;

    private Configuration config;

    private String[] linkKeyItemString;

    //chest boat
    private boolean allowChestBoat;

    //tooltips
    @SideOnly(Side.CLIENT)
    public boolean tooltipOnLinkItems;
    @SideOnly(Side.CLIENT)
    public boolean tooltipOnChests;
    @SideOnly(Side.CLIENT)
    public boolean tooltipOnBoats;

    public void init(FMLPreInitializationEvent event) {
        if (config == null) {
            config = new Configuration(event.getSuggestedConfigurationFile());
        }
        loadConfiguration();
        if (config.hasChanged()) {
            config.save();
        }
        Boatifull.instance.getLogger().debug("Configuration Loaded");
    }

    @SideOnly(Side.CLIENT)
    public void initClient(FMLPreInitializationEvent event) {
        if (config == null) {
            config = new Configuration(event.getSuggestedConfigurationFile());
        }
        loadClientConfiguration();
        if (config.hasChanged()) {
            config.save();
        }
    }

    public List<ItemStack> getLinkKeyItems() {
        List<ItemStack> items = new ArrayList<>();
        for (String aLinkKeyItemString : linkKeyItemString) {
            Optional<ItemStack> item = ItemStackParser.parseItemStack(aLinkKeyItemString);
            if (item.isPresent()) {
                items.add(item.get());
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
                "A list of all items which can be used to link boats together, metadata is optional and will be fuzzy if not specified.\n" +
                "The first entry will act as the default in case of error");
        this.allowChestBoat = config.getBoolean("Enable chest boat", Categories.GENERAL.getName(),
                !Loader.isModLoaded("opentransport"),
                "Set to false to disable chest boats");
    }

    @SideOnly(Side.CLIENT)
    private void loadClientConfiguration() {
        tooltipOnLinkItems = config.getBoolean("link items", Categories.CLIENT.getName(), true,
                "set to false to not show tooltips on link items");
        tooltipOnLinkItems = config.getBoolean("chests", Categories.CLIENT.getName(), true,
                "set to false to not show tooltips on chests");
        tooltipOnBoats = config.getBoolean("boats", Categories.CLIENT.getName(), false,
                "set to true to show tooltips on boats");
    }

    public enum Categories {
        GENERAL("general"),
        CLIENT("client");

        private final String name;

        Categories(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
