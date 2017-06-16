package com.infinityraider.boatifull.boatlinking;

import com.google.common.collect.ImmutableList;
import com.infinityraider.boatifull.Boatifull;
import com.infinityraider.boatifull.entity.EntityBoatLink;
import com.infinityraider.boatifull.handler.ConfigurationHandler;
import com.infinityraider.infinitylib.modules.entitylistener.IEntityLeaveOrJoinWorldListener;
import com.infinityraider.infinitylib.modules.entitylistener.ModuleEntityListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public class BoatLinker implements IBoatLinker, IEntityLeaveOrJoinWorldListener {
    private static final BoatLinker INSTANCE = new BoatLinker();

    public static BoatLinker getInstance() {
        return INSTANCE;
    }

    public static int LINK_RANGE = 3;

    private List<ItemStack> linkKeyItems;

    private final Map<EntityPlayer, EntityBoat> linkingPlayerToBoat;
    private final Map<EntityBoat, EntityPlayer> linkingBoatToPlayer;

    private final Map<EntityBoat, EntityBoatLink> boatLinks;

    private BoatLinker() {
        this.linkingPlayerToBoat = new IdentityHashMap<>();
        this.linkingBoatToPlayer = new IdentityHashMap<>();
        this.boatLinks = new IdentityHashMap<>();
        ModuleEntityListener.getInstance().registerListener(this);
    }

    @Override
    public ItemStack getDefaultKeyStack() {
        return getLinkKeyStacks().get(0);
    }

    @Override
    public List<ItemStack> getLinkKeyStacks() {
        if(this.linkKeyItems == null) {
            this.linkKeyItems = ImmutableList.copyOf(ConfigurationHandler.getInstance().getLinkKeyItems());
            StringBuilder buffer = new StringBuilder();
            buffer.append("Found boat linking items:");
            for(ItemStack stack : this.linkKeyItems) {
                buffer.append("\n").append(stack.getItem().getRegistryName().toString()).append(":").append(stack.getItemDamage());
            }
            Boatifull.instance.getLogger().debug(buffer.toString());
        }
        return this.linkKeyItems;
    }

    @Override
    public boolean isValidLinkKey(ItemStack stack) {
        if(stack == null) {
            return false;
        }
        for(ItemStack linkStack : this.getLinkKeyStacks()) {
            if(linkStack.getItem() == stack.getItem()) {
                if(linkStack.getItemDamage() == OreDictionary.WILDCARD_VALUE || linkStack.getItemDamage() == stack.getItemDamage()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public EnumBoatLinkResult canStartBoatLink(EntityPlayer player, EntityBoat boat) {
        if(linkingPlayerToBoat.containsKey(player)) {
            return EnumBoatLinkResult.FAIL_PLAYER_ALREADY_LINKING;
        }
        if(linkingBoatToPlayer.containsKey(boat)) {
            return EnumBoatLinkResult.FAIL_BOAT_ALREADY_LINKING;
        }
        return EnumBoatLinkResult.SUCCESS_START;
    }

    @Override
    public boolean cancelBoatLink(EntityPlayer player) {
        if(linkingPlayerToBoat.containsKey(player)) {
            removeLinkingProgress(player);
            return true;
        }
        return false;
    }

    @Override
    public EnumBoatLinkResult startBoatLink(EntityPlayer player, EntityBoat boat) {
        EnumBoatLinkResult result = canStartBoatLink(player, boat);
        if(result.isOk()) {
            linkingPlayerToBoat.put(player, boat);
            linkingBoatToPlayer.put(boat, player);
        }
        return result;
    }

    @Override
    public EnumBoatLinkResult canFinishBoatLink(EntityPlayer player, EntityBoat boat) {
        if(linkingBoatToPlayer.containsKey(boat)) {
            return EnumBoatLinkResult.FAIL_BOAT_ALREADY_LINKING;
        }
        if(!linkingPlayerToBoat.containsKey(player)) {
            return EnumBoatLinkResult.FAIL_NOT_LINKING;
        }
        return canLinkBoats(linkingPlayerToBoat.get(player), boat);
    }

    @Override
    public EnumBoatLinkResult finishBoatLink(EntityPlayer player, EntityBoat boat) {
        EnumBoatLinkResult result = canFinishBoatLink(player, boat);
        if(result.isOk()) {
            EntityBoat leader = linkingPlayerToBoat.get(player);
            linkingPlayerToBoat.remove(player);
            linkingBoatToPlayer.remove(leader);
            result = linkBoats(leader, boat, player.inventory.getCurrentItem());
        }
        return result;
    }

    @Override
    public EnumBoatLinkResult canLinkBoats(EntityBoat leader, EntityBoat follower) {
        IBoatLink followerLink = getBoatLink(follower);
        if(!areBoatsCloseEnough(leader, follower)) {
            return EnumBoatLinkResult.FAIL_TOO_FAR;
        }
        if(followerLink != null) {
            return EnumBoatLinkResult.FAIL_ALREADY_HAS_LEADER;
        }
        if(checkForLinkLoopRecursive(leader, follower)) {
            return EnumBoatLinkResult.FAIL_LINK_LOOP;
        }
        return EnumBoatLinkResult.SUCCESS_FINISH;
    }

    @Override
    public boolean areBoatsCloseEnough(EntityBoat a, EntityBoat b) {
        return a != b && a.getEntityWorld() == b.getEntityWorld() && a.getDistanceSqToEntity(b) <= LINK_RANGE * LINK_RANGE;
    }

    private boolean checkForLinkLoopRecursive(EntityBoat leader, EntityBoat follower) {
        IBoatLink leaderLink = this.getBoatLink(leader);
        if(leaderLink != null) {
            EntityBoat leadingBoat = leaderLink.getLeader();
            if(leadingBoat == null) {
                return true;
            } else if(leadingBoat == follower) {
                return true;
            }
            return checkForLinkLoopRecursive(leadingBoat, follower);
        }
        return false;
    }

    @Override
    public EnumBoatLinkResult linkBoats(EntityBoat leader, EntityBoat follower, ItemStack linkItem) {
        EnumBoatLinkResult result = canLinkBoats(leader, follower);
        if(result.isOk()) {
            removeLinkingProgress(leader);
            removeLinkingProgress(follower);
            EntityBoatLink boatLink = new EntityBoatLink(leader, follower, linkItem);
            this.boatLinks.put(follower, boatLink);
            leader.getEntityWorld().spawnEntity(boatLink);
            boatLink.mountFollower();
        }
        return result;
    }

    @Override
    public void unlinkBoat(EntityBoat follower) {
        if(boatLinks.containsKey(follower)) {
            IBoatLink link = getBoatLink(follower);
            boatLinks.remove(follower);
            link.breakLink();
            EntityItem item = new EntityItem(follower.getEntityWorld(), follower.posX, follower.posY, follower.posZ, link.getLinkItem());
            follower.getEntityWorld().spawnEntity(item);
        } else {
            boatLinks.remove(follower);
        }
    }

    @Override
    public EntityBoatLink getBoatLink(EntityBoat boat) {
        return this.boatLinks.get(boat);
    }

    @Override
    public List<EntityBoat> getBoatsLinkedToBoat(EntityBoat boat) {
        Iterator<Map.Entry<EntityBoat, EntityBoatLink>> iterator = this.boatLinks.entrySet().iterator();
        List<EntityBoat> linkedBoats = new ArrayList<>();
        while(iterator.hasNext()) {
            Map.Entry<EntityBoat, EntityBoatLink> entry = iterator.next();
            if(entry.getValue() == null || entry.getKey() == null) {
                iterator.remove();
            } else if(entry.getValue().getLeader() == boat) {
                linkedBoats.add(entry.getKey());
            }
        }
        return linkedBoats;
    }

    public boolean validateBoatLink(EntityBoatLink link) {
        EntityBoat follower = link.getFollower();
        if (follower == null) {
            return false;
        } else {
            this.boatLinks.put(follower, link);
            return true;
        }
    }

    private void removeLinkingProgress(EntityBoat boat) {
        if(linkingBoatToPlayer.containsKey(boat)) {
            linkingPlayerToBoat.remove(linkingBoatToPlayer.get(boat));
            linkingBoatToPlayer.remove(boat);
        }
    }

    private void removeLinkingProgress(EntityPlayer player) {
        if(linkingPlayerToBoat.containsKey(player)) {
            linkingBoatToPlayer.remove(linkingPlayerToBoat.get(player));
            linkingPlayerToBoat.remove(player);
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onPlayerDeath(LivingDeathEvent event) {
        if(!event.getEntity().getEntityWorld().isRemote && event.getEntity() instanceof EntityPlayer) {
            removeLinkingProgress((EntityPlayer) event.getEntity());
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if(!event.player.getEntityWorld().isRemote) {
            removeLinkingProgress(event.player);
        }
    }

    @Override
    public void onEntityJoinWorld(Entity entity) {}

    @Override
    public void onEntityLeaveWorld(Entity entity) {
        if(!entity.getEntityWorld().isRemote) {
            if(entity instanceof EntityBoat) {
                EntityBoat boat = (EntityBoat) entity;
                if(linkingBoatToPlayer.containsKey(boat)) {
                    removeLinkingProgress(boat);
                }
                if(this.boatLinks.containsKey(boat)) {
                    this.unlinkBoat(boat);
                }
                getBoatsLinkedToBoat(boat).forEach(this::unlinkBoat);
            } else if(entity instanceof EntityPlayer) {
                this.removeLinkingProgress((EntityPlayer) entity);
            }
        }
    }
}
