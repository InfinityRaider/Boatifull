package com.infinityraider.boatifull.network;

import com.infinityraider.infinitylib.network.MessageBase;
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
            new MessageSyncBoatId(this.boat).sendTo(ctx.getServerHandler().player);
        }
    }

    @Override
    protected IMessage getReply(MessageContext ctx) {
        return null;
    }
}
