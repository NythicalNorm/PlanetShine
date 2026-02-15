package com.nythicalnorm.voxelspaceprogram.network.time;

import com.nythicalnorm.voxelspaceprogram.PSServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class ServerboundTimeWarpChange {
    private final long ProposedSetTimeWarpSpeed;

    public ServerboundTimeWarpChange(long proposedSetTimeWarpSpeed)
    {
        this.ProposedSetTimeWarpSpeed = proposedSetTimeWarpSpeed;
    }

    public ServerboundTimeWarpChange(FriendlyByteBuf friendlyByteBuf) {
        this.ProposedSetTimeWarpSpeed = friendlyByteBuf.readLong();
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeLong(this.ProposedSetTimeWarpSpeed);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER ) {
            NetworkEvent.Context context = contextSupplier.get();
            PSServer.getInstance().ifPresent(psServer ->
                    context.enqueueWork(() -> psServer.ChangeTimeWarp(ProposedSetTimeWarpSpeed, contextSupplier.get().getSender())));
            context.setPacketHandled(true);
        }
    }
}
