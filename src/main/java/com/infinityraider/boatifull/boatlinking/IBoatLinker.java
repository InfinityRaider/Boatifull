package com.infinityraider.boatifull.boatlinking;

import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

/**
 * Class to aid the linking of boats together,
 * by default boats are linked by the player by consecutively clicking first the leader boat and then the the following boat
 */
public interface IBoatLinker {
    /**
     * Gets the item required to link two boats together
     * @return the linking key item
     */
    Item getLinkKeyItem();

    /**
     * Checks if a player can start a boat link, does not check if the player is holding the key item,
     * can fail if the player has already initiated a link on a boat, or if another player has initiated a link on this boat.
     *
     * @param player the player wanting to start the link
     * @param boat the boat from which the link is being started
     * @return boat link result
     */
    EnumBoatLinkResult canStartBoatLink(EntityPlayer player, EntityBoat boat);

    /**
     * Performs the start of a boat link
     *
     * @param player player starting the link
     * @param boat boat from which the link is being started
     * @return boat link result
     */
    EnumBoatLinkResult startBoatLink(EntityPlayer player, EntityBoat boat);

    /**
     * Checks if a player can finish a boat link, does not check if the player is holding the key item,
     * Can fail if the player has not initiated a link on a boat, or if another player has started a link on this boat.
     * Can also fail if the two boats are too far from each other.
     * A final possibility to fail is if the linking of the boats would introduce a linking loop.
     *
     * This method calls canLinkBoats(EntityBoat leader, EntityBoat follower) internally.
     *
     * @param player the player wanting to finish the linking
     * @param boat the boat being linked to a previously selected boat
     * @return boat link result
     */
    EnumBoatLinkResult canFinishBoatLink(EntityPlayer player, EntityBoat boat);

    /**
     * Finishes the linking of two boats
     *
     * @param player the player finishing the link
     * @param boat the boat being linked
     * @return boat link result
     */
    EnumBoatLinkResult finishBoatLink(EntityPlayer player, EntityBoat boat);

    /**
     * Checks if two boats can be linked together as leader and follower,
     * Fails if:
     *  - the follower is already linked to a leader, a boat can only have one leader at a time.
     *  - the leader is linked in a chain to the follower, allowing this link would introduce a linking loop
     *  - the two boats are too far away
     *
     * this method internally calls areBoatsCloseEnough(EntityBoat a, EntityBoat b)
     *
     * @param leader the first boat of the link, acting as leader
     * @param follower the second boat of the link, acting as follower
     * @return boat link result
     */
    EnumBoatLinkResult canLinkBoats(EntityBoat leader, EntityBoat follower);

    /**
     * Checks if two boats are close enough to eacother to be linked
     * @param a first boat
     * @param b second boat
     * @return true if the boats are close enough.
     */
    boolean areBoatsCloseEnough(EntityBoat a, EntityBoat b);

    /**
     * Links two boats together as leader and follower
     *
     * @param leader the leader boat in the link
     * @param follower the follower boat in the link
     * @return boat link result
     */
    EnumBoatLinkResult linkBoats(EntityBoat leader, EntityBoat follower);

    /**
     * Unlinks a follower from its leader
     * @param follower the follower to break the link for
     */
    void unlinkBoat(EntityBoat follower);

    /**
     * Gets the boat link property for a boat
     * @param boat boat object
     * @return the boat link property object
     */
    IBoatLink getBoatLink(EntityBoat boat);
}
