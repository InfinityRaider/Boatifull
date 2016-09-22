package com.infinityraider.boatifull.handler;

import com.infinityraider.boatifull.boatlinking.BoatLinker;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        ItemStack stack = event.getItemStack();
        if(stack != null && stack.getItem() == BoatLinker.getInstance().getLinkKeyItem()) {
            event.getToolTip().add("");
            event.getToolTip().add(ChatFormatting.GRAY + I18n.translateToLocal("boatifull.tooltip.link_item"));
        }
    }

}
