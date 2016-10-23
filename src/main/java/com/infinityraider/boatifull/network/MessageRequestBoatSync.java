package com.infinityraider.boatifull.network;

import com.infinityraider.boatifull.Boatifull;
import com.infinityraider.infinitylib.network.MessageBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageRequestBoatSync extends MessageBase<IMessage> {
    private EntityBoat boat;

    public MessageRequestBoatSync() {
        super();
    }

    public MessageRequestBoatSync(EntityBoat boat) {
        this();
        this.boat = boat;
    }

    @Override
    public Side getMessageHandlerSide() {
        return Side.SERVER;
    }

    @Override
    protected void processMessage(MessageContext ctx) {
        if(ctx.side == Side.SERVER && this.boat != null) {
            Boatifull.instance.getNetworkWrapper().sendTo(new MessageSyncBoatId(this.boat), ctx.getServerHandler().playerEntity);
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
    }

    @Override
    public void toBytes(ByteBuf buf) {
        this.writeEntityToByteBuf(buf, this.boat);
    }
}
