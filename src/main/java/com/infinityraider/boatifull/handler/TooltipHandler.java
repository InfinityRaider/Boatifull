package com.infinityraider.boatifull.handler;

import com.infinityraider.boatifull.boatlinking.BoatLinker;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

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
        if(BoatLinker.getInstance().isValidLinkKey(event.getItemStack())) {
            event.getToolTip().add("");
            event.getToolTip().add(ChatFormatting.GRAY + I18n.translateToLocal("boatifull.tooltip.link_item"));
            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                event.getToolTip().add("");
                event.getToolTip().add(ChatFormatting.DARK_GRAY  + "" + ChatFormatting.ITALIC + I18n.translateToLocal("boatifull.tooltip.link_boat_first"));
                event.getToolTip().add(ChatFormatting.DARK_GRAY  + "" + ChatFormatting.ITALIC + I18n.translateToLocal("boatifull.tooltip.link_boat_second"));
                event.getToolTip().add(ChatFormatting.DARK_GRAY  + "" + ChatFormatting.ITALIC + I18n.translateToLocal("boatifull.tooltip.cancel_link"));
                event.getToolTip().add("");
                event.getToolTip().add(ChatFormatting.DARK_GRAY  + "" + ChatFormatting.ITALIC + I18n.translateToLocal("boatifull.tooltip.link_info"));
            } else {
                event.getToolTip().add(ChatFormatting.DARK_GRAY  + "" + ChatFormatting.ITALIC + I18n.translateToLocal("boatifull.tooltip.more_info"));
            }
        }
    }

}
