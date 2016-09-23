package com.infinityraider.boatifull.item;

import com.google.common.collect.ImmutableList;
import com.infinityraider.boatifull.boatlinking.BoatIdProvider;
import com.infinityraider.boatifull.boatlinking.BoatLinker;
import com.infinityraider.boatifull.boatlinking.IBoatId;
import com.infinityraider.boatifull.boatlinking.IBoatLink;
import com.infinityraider.boatifull.entity.EntityBoatLink;
import com.infinityraider.infinitylib.item.ItemDebuggerBase;
import com.infinityraider.infinitylib.utility.RayTraceHelper;
import com.infinityraider.infinitylib.utility.debug.DebugMode;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.List;

public class ItemDebugger extends ItemDebuggerBase {
    public ItemDebugger() {
        super();
        this.setCreativeTab(CreativeTabs.MISC);
    }

    @Override
    protected List<DebugMode> getDebugModes() {
        return ImmutableList.of(new DebugModeBoatLinkData());
    }

    public static class DebugModeBoatLinkData extends DebugMode {
        @Override
        public String debugName() {
            return "boat link data";
        }

        @Override
        public void debugActionBlockClicked(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

        }

        @Override
        public void debugActionClicked(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
            RayTraceResult target = RayTraceHelper.getTargetEntityOrBlock(player, 16);
            if(target != null && target.typeOfHit == RayTraceResult.Type.ENTITY && target.entityHit != null) {
                Entity entity = target.entityHit;
                if(entity instanceof EntityBoatLink) {
                    entity = ((EntityBoatLink) entity).getFollower();
                }
                if(entity instanceof EntityBoat) {
                    EntityBoat boat = (EntityBoat) entity;
                    IBoatId boatId = BoatIdProvider.getBoatIdData(boat);

                    player.addChatComponentMessage(new TextComponentString("Boat data for " + (world.isRemote ? "CLIENT:" : "SERVER:")));
                    player.addChatComponentMessage(new TextComponentString(" - Entity id: " + boat.getEntityId()));
                    if (boatId == null) {
                        player.addChatComponentMessage(new TextComponentString(" - Error: no id data found"));
                    } else {
                        player.addChatComponentMessage(new TextComponentString(" - Unique boat id: " + boatId.getId()));
                    }

                    IBoatLink link = BoatLinker.getInstance().getBoatLink(boat);
                    if (link != null) {
                        player.addChatComponentMessage(new TextComponentString(" - This boat has a leading boat"));
                        EntityBoat leader = link.getLeader();
                        player.addChatComponentMessage(new TextComponentString(" - Leading boat entity id: " + (leader == null ? "null" : leader.getEntityId())));

                        IBoatId leadingId = BoatIdProvider.getBoatIdData(leader);
                        if(leadingId == null) {
                            player.addChatComponentMessage(new TextComponentString(" - Error: no id data found for leader"));
                        } else {
                            player.addChatComponentMessage(new TextComponentString(" -Leading boat unique boat id: " + leadingId.getId()));
                        }
                    } else {
                        player.addChatComponentMessage(new TextComponentString(" - This boat has no leading boat"));
                    }
                }
            }
        }

        @Override
        public void debugActionEntityClicked(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {

        }
    }
}
