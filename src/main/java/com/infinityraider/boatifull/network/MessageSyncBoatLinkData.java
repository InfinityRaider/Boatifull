package com.infinityraider.boatifull.network;

import com.infinityraider.boatifull.boatlinking.BoatLinkProvider;
import com.infinityraider.boatifull.boatlinking.IBoatLinkData;
import com.infinityraider.infinitylib.network.MessageBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageSyncBoatLinkData extends MessageBase<IMessage> {
    private EntityBoat boat;
    private NBTTagCompound data;

    public MessageSyncBoatLinkData() {
        super();
    }

    public MessageSyncBoatLinkData(EntityBoat boat) {
        this();
        this.boat = boat;
        IBoatLinkData linkData = BoatLinkProvider.getLinkedBoats(boat);
        this.data = linkData == null ? null : linkData.writeToNBT();
    }

    @Override
    public Side getMessageHandlerSide() {
        return Side.CLIENT;
    }

    @Override
    protected void processMessage(MessageContext ctx) {
        if(ctx.side == Side.CLIENT && this.boat != null && this.data != null) {
            IBoatLinkData linkData = BoatLinkProvider.getLinkedBoats(this.boat);
            if(linkData != null) {
                linkData.readFromNBT(this.data);
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
