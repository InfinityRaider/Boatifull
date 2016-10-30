package com.infinityraider.boatifull.handler;

import com.infinityraider.boatifull.boatlinking.BoatLinker;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBoat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.stream.Collectors;

import static net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST;

@SideOnly(Side.CLIENT)
public class TooltipHandler {
    private static final TooltipHandler INSTANCE = new TooltipHandler();

    public static TooltipHandler getInstance() {
        return INSTANCE;
    }

    private TooltipHandler() {}

    @SubscribeEvent(priority = LOWEST)
    @SuppressWarnings("unused")
    public void onToolTipEvent(ItemTooltipEvent event) {
        if(event.getItemStack() ==null) {
            return;
        }
        if(ConfigurationHandler.getInstance().tooltipOnLinkItems && BoatLinker.getInstance().isValidLinkKey(event.getItemStack())) {
            this.addBoatLinkTooltip(event.getToolTip());
        }
        if(ConfigurationHandler.getInstance().tooltipOnChests && event.getItemStack().getItem() == Item.getItemFromBlock(Blocks.CHEST)) {
            this.addChestBoatTooltip(event.getToolTip());
        }
        if(ConfigurationHandler.getInstance().tooltipOnBoats && event.getItemStack().getItem() instanceof ItemBoat) {
            this.addBoatTooltip(event.getToolTip());
        }
    }

    private void addBoatLinkTooltip(List<String> tooltip) {
        tooltip.add("");
        tooltip.add(ChatFormatting.GRAY + I18n.translateToLocal("boatifull.tooltip.link_item"));
        this.addAdvancedTooltip(tooltip);
    }

    private void addChestBoatTooltip(List<String> tooltip) {
        if(ConfigurationHandler.getInstance().allowChestBoat()) {
            tooltip.add(ChatFormatting.GRAY + I18n.translateToLocal("boatifull.tooltip.create_chest_boat"));
        }
    }

    private void addBoatTooltip(List<String> tooltip) {
        tooltip.add("");
        tooltip.add(ChatFormatting.GRAY + I18n.translateToLocal("boatifull.tooltip.link_boats"));
        this.addAdvancedTooltip(tooltip);
        if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            tooltip.add("");
            tooltip.add(ChatFormatting.DARK_GRAY  + "" + ChatFormatting.ITALIC + I18n.translateToLocal("boatifull.tooltip.linking_items"));
            tooltip.addAll(BoatLinker.getInstance().getLinkKeyStacks().stream().map(ItemStack::getDisplayName).collect(Collectors.toList()));
        }
    }

    private void addAdvancedTooltip(List<String> tooltip) {
        if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            tooltip.add("");
            tooltip.add(ChatFormatting.DARK_GRAY  + "" + ChatFormatting.ITALIC + I18n.translateToLocal("boatifull.tooltip.link_boat_first"));
            tooltip.add(ChatFormatting.DARK_GRAY  + "" + ChatFormatting.ITALIC + I18n.translateToLocal("boatifull.tooltip.link_boat_second"));
            tooltip.add(ChatFormatting.DARK_GRAY  + "" + ChatFormatting.ITALIC + I18n.translateToLocal("boatifull.tooltip.cancel_link"));
            tooltip.add("");
            tooltip.add(ChatFormatting.DARK_GRAY  + "" + ChatFormatting.ITALIC + I18n.translateToLocal("boatifull.tooltip.link_info"));
        } else {
            tooltip.add(ChatFormatting.DARK_GRAY  + "" + ChatFormatting.ITALIC + I18n.translateToLocal("boatifull.tooltip.more_info"));
        }
    }
}
