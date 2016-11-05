package com.infinityraider.boatifull.network;

import com.infinityraider.boatifull.boatlinking.CapabilityBoatId;
import com.infinityraider.boatifull.boatlinking.IBoatId;
import com.infinityraider.infinitylib.network.MessageBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageSyncBoatId extends MessageBase<IMessage> {
    private EntityBoat boat;
    private NBTTagCompound data;

    public MessageSyncBoatId() {
        super();
    }

    public MessageSyncBoatId(EntityBoat boat) {
        this();
        this.boat = boat;
        IBoatId boatId = CapabilityBoatId.getBoatIdData(boat);
        this.data = boatId == null ? null : boatId.writeToNBT();
    }

    @Override
    public Side getMessageHandlerSide() {
        return Side.CLIENT;
    }

    @Override
    protected void processMessage(MessageContext ctx) {
        if(ctx.side == Side.CLIENT && this.boat != null && this.data != null) {
            IBoatId boatId = CapabilityBoatId.getBoatIdData(this.boat);
            if(boatId != null) {
                boatId.readFromNBT(this.data);
            }
        }
    }

    @Override
    protected IMessage getReply(MessageContext ctx) {
        return null;
    }
}
