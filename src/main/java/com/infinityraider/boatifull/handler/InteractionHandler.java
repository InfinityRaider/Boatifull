package com.infinityraider.boatifull.handler;

import com.infinityraider.boatifull.boatlinking.BoatLinker;
import com.infinityraider.boatifull.boatlinking.EnumBoatLinkResult;
import com.infinityraider.boatifull.boatlinking.IBoatLinker;
import com.infinityraider.boatifull.entity.EntityBoatLink;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST;

public class InteractionHandler {
    private static final InteractionHandler INSTANCE = new InteractionHandler();

    public static InteractionHandler getInstance() {
        return INSTANCE;
    }

    private InteractionHandler() {}

    @SubscribeEvent(priority = HIGHEST)
    @SuppressWarnings("unused")
    public void onPlayerInteraction(PlayerInteractEvent.EntityInteractSpecific event) {
        if(event.getEntityPlayer().getEntityWorld().isRemote) {
            return;
        }
        EntityBoat boat = null;
        if(event.getTarget() instanceof EntityBoatLink) {
            boat = ((EntityBoatLink) event.getTarget()).getFollower();
        } else if(event.getTarget() instanceof EntityBoat) {
            boat = (EntityBoat) event.getTarget();
        }
        if(boat != null) {
            EntityPlayer player = event.getEntityPlayer();
            if(!player.isSneaking()) {
                return;
            }
            ItemStack stack = player.inventory.getStackInSlot(player.inventory.currentItem);
            IBoatLinker boatLinker = BoatLinker.getInstance();
            if(stack != null && stack.getItem() == boatLinker.getLinkKeyItem()) {
                EnumBoatLinkResult startResult = boatLinker.startBoatLink(player, boat);
                if(!startResult.isOk()) {
                    EnumBoatLinkResult finishResult = boatLinker.finishBoatLink(player, boat);
                    if(!finishResult.isOk()) {
                        if(startResult.isOk()) {
                            player.addChatComponentMessage(new TextComponentTranslation("boatifull.message." + startResult.toString().toLowerCase()));
                        } else {
                            player.addChatComponentMessage(new TextComponentTranslation("boatifull.message." + finishResult.toString().toLowerCase()));
                        }
                    } else {
                        player.addChatComponentMessage(new TextComponentTranslation("boatifull.message." + finishResult.toString().toLowerCase()));
                        if(!player.capabilities.isCreativeMode) {
                            stack.stackSize = stack.stackSize - 1;
                            if (stack.stackSize <= 0) {
                                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                            }
                        }
                    }
                } else {
                    player.addChatComponentMessage(new TextComponentTranslation("boatifull.message." + startResult.toString().toLowerCase()));
                }
            }
            event.setCanceled(true);
            event.setResult(Event.Result.DENY);
        }
    }
}
