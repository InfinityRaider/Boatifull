package com.infinityraider.boatifull.item;

import com.google.common.collect.ImmutableList;
import com.infinityraider.boatifull.boatlinking.BoatLinkProvider;
import com.infinityraider.boatifull.boatlinking.IBoatLinkData;
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
                if(entity instanceof EntityBoat) {
                    EntityBoat boat = (EntityBoat) entity;
                    IBoatLinkData data = BoatLinkProvider.getLinkedBoats(boat);

                    player.addChatComponentMessage(new TextComponentString("Boat data for " + (world.isRemote ? "CLIENT:" : "SERVER:")));
                    player.addChatComponentMessage(new TextComponentString(" - Entity id: " + boat.getEntityId()));
                    if(data == null) {
                        player.addChatComponentMessage(new TextComponentString(" - Error: no link data found"));
                    } else {
                        if(data.hasLeadingBoat()) {
                            player.addChatComponentMessage(new TextComponentString(" - This boat has a leading boat"));
                            EntityBoat leader = data.getLeadingBoat();
                            player.addChatComponentMessage(new TextComponentString(" - Leading boat is: " + (leader == null ? "null" : leader.getEntityId())));
                        } else {
                            player.addChatComponentMessage(new TextComponentString(" - This boat has no leading boat"));
                        }
                    }
                }
            }
        }

        @Override
        public void debugActionEntityClicked(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {

        }
    }
}
