package com.infinityraider.boatifull.network;

import com.infinityraider.boatifull.boatlinking.BoatIdProvider;
import com.infinityraider.boatifull.boatlinking.IBoatId;
import com.infinityraider.infinitylib.network.MessageBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
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
        IBoatId boatId = BoatIdProvider.getBoatIdData(boat);
        this.data = boatId == null ? null : boatId.writeToNBT();
    }

    @Override
    public Side getMessageHandlerSide() {
        return Side.CLIENT;
    }

    @Override
    protected void processMessage(MessageContext ctx) {
        if(ctx.side == Side.CLIENT && this.boat != null && this.data != null) {
            IBoatId boatId = BoatIdProvider.getBoatIdData(this.boat);
            if(boatId != null) {
                boatId.readFromNBT(this.data);
            }
        }
    }

    @Override
    protected IMessage getReply(MessageContext ctx) {
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        Entity entity = this.readEntityFromByteBuf(buf);
        if(entity instanceof EntityBoat) {
            this.boat = (EntityBoat) entity;
        }
        if(buf.readBoolean()) {
            this.data = ByteBufUtils.readTag(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        this.writeEntityToByteBuf(buf, this.boat);
        buf.writeBoolean(this.data != null);
        if(this.data != null) {
            ByteBufUtils.writeTag(buf, this.data);
        }
    }
}
