package com.infinityraider.boatifull.handler;

import com.infinityraider.boatifull.boatlinking.BoatLinker;
import com.infinityraider.boatifull.boatlinking.EnumBoatLinkResult;
import com.infinityraider.boatifull.boatlinking.IBoatLinker;
import com.infinityraider.boatifull.entity.EntityBoatChest;
import com.infinityraider.boatifull.entity.EntityBoatLink;
import com.infinityraider.infinitylib.network.MessageSetEntityDead;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
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
            if(BoatLinker.getInstance().isValidLinkKey(stack)) {
                this.performBoatLinkAction(player, boat, stack, boatLinker);
                this.cancelEvent(event);
            } else if(ConfigurationHandler.getInstance().allowChestBoat()  && boat.getClass() == EntityBoat.class && stack != null  && stack.getItem() == Item.getItemFromBlock(Blocks.CHEST)) {
                this.createChestBoat(player, boat, stack);
                this.cancelEvent(event);
            }
        }
    }

    private void performBoatLinkAction(EntityPlayer player, EntityBoat boat, ItemStack stack, IBoatLinker boatLinker) {
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
                this.reduceStackSize(player, stack);
            }
        } else {
            player.addChatComponentMessage(new TextComponentTranslation("boatifull.message." + startResult.toString().toLowerCase()));
        }
    }

    private void createChestBoat(EntityPlayer player, EntityBoat boat, ItemStack stack) {
        World world = boat.getEntityWorld();
        EntityBoatChest chestBoat = new EntityBoatChest(boat);
        boat.setDead();
        new MessageSetEntityDead(boat).sendToAll();
        world.spawnEntityInWorld(chestBoat);
        this.reduceStackSize(player, stack);
    }

    private void reduceStackSize(EntityPlayer player, ItemStack stack) {
        if(!player.capabilities.isCreativeMode) {
            stack.stackSize = stack.stackSize - 1;
            if (stack.stackSize <= 0) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
        }
    }

    private void cancelEvent(Event event) {
        event.setCanceled(true);
        event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent(priority = HIGHEST)
    @SuppressWarnings("unused")
    public void onPlayerInteraction(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        if(player.getEntityWorld().isRemote || !player.isSneaking()) {
            return;
        }
        ItemStack stack = player.inventory.getStackInSlot(player.inventory.currentItem);
        IBoatLinker boatLinker = BoatLinker.getInstance();
        if(boatLinker.isValidLinkKey(stack) && boatLinker.cancelBoatLink(player)) {
            player.addChatComponentMessage(new TextComponentTranslation("boatifull.message.cancel_link"));
        }
    }
}
