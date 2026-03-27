package com.nythicalnorm.planetshine.network.time;

import com.nythicalnorm.planetshine.PSServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class ServerboundTimeWarpChange {
    private final long ProposedSetTimeWarpSpeed;
    private final boolean allowOnPlanet;

    public ServerboundTimeWarpChange(long proposedSetTimeWarpSpeed, boolean allowOnPlanet)
    {
        this.ProposedSetTimeWarpSpeed = proposedSetTimeWarpSpeed;
        this.allowOnPlanet = allowOnPlanet;
    }

    public ServerboundTimeWarpChange(FriendlyByteBuf friendlyByteBuf) {
        this.ProposedSetTimeWarpSpeed = friendlyByteBuf.readLong();
        this.allowOnPlanet = friendlyByteBuf.readBoolean();
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeLong(this.ProposedSetTimeWarpSpeed);
        friendlyByteBuf.writeBoolean(this.allowOnPlanet);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER ) {
            NetworkEvent.Context context = contextSupplier.get();
            PSServer.getInstance().ifPresent(psServer ->
                    context.enqueueWork(() -> psServer.getTimeWarpManager().TryChangeTimeWarp(ProposedSetTimeWarpSpeed, allowOnPlanet, contextSupplier.get().getSender())));
            context.setPacketHandled(true);
        }
    }
}
