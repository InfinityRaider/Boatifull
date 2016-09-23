package com.infinityraider.boatifull.boatlinking;

import com.infinityraider.boatifull.entity.EntityBoatLink;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.IdentityHashMap;
import java.util.Map;

public class BoatLinker implements IBoatLinker {
    private static final BoatLinker INSTANCE = new BoatLinker();

    public static BoatLinker getInstance() {
        return INSTANCE;
    }

    public static int LINK_RANGE = 3;

    private Item linkKeyItem;

    private final Map<EntityPlayer, EntityBoat> linkingPlayerToBoat;
    private final Map<EntityBoat, EntityPlayer> linkingBoatToPlayer;

    private final Map<EntityBoat, EntityBoatLink> boatLinks;

    private BoatLinker() {
        this.linkKeyItem = null;
        this.linkingPlayerToBoat = new IdentityHashMap<>();
        this.linkingBoatToPlayer = new IdentityHashMap<>();
        this.boatLinks = new IdentityHashMap<>();
    }

    @Override
    public Item getLinkKeyItem() {
        if(linkKeyItem == null) {
            //TODO: make configurable
            linkKeyItem = Items.LEAD;
        }
        return linkKeyItem;
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
            result = linkBoats(leader, boat);
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
    public EnumBoatLinkResult linkBoats(EntityBoat leader, EntityBoat follower) {
        EnumBoatLinkResult result = canLinkBoats(leader, follower);
        if(result.isOk()) {
            removeLinkingProgress(leader);
            removeLinkingProgress(follower);
            EntityBoatLink boatLink = new EntityBoatLink(leader, follower);
            this.boatLinks.put(follower, boatLink);
            leader.getEntityWorld().spawnEntityInWorld(boatLink);
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
        } else {
            boatLinks.remove(follower);
        }
    }

    @Override
    public IBoatLink getBoatLink(EntityBoat boat) {
        return this.boatLinks.get(boat);
    }

    public void validateBoatLink(EntityBoatLink link) {
        this.boatLinks.put(link.getFollower(), link);
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
}
