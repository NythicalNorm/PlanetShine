package com.nythicalnorm.voxelspaceprogram.network.orbitaldata;

import com.nythicalnorm.voxelspaceprogram.network.ClientPacketHandler;
import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundHostOrbitSet {
    private final OrbitId spaceHostOrbitId;

    public ClientboundHostOrbitSet(OrbitId spaceHostOrbitId) {
        this.spaceHostOrbitId = spaceHostOrbitId;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        if (spaceHostOrbitId != null) {
            friendlyByteBuf.writeBoolean(true);
            spaceHostOrbitId.encodeToBuffer(friendlyByteBuf);
        } else {
            friendlyByteBuf.writeBoolean(false);
        }
    }

    public ClientboundHostOrbitSet(FriendlyByteBuf friendlyByteBuf) {
        if (friendlyByteBuf.readBoolean()) {
            this.spaceHostOrbitId = new OrbitId(friendlyByteBuf);
        } else {
            this.spaceHostOrbitId = null;
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT ) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientPacketHandler.hostOrbitSet(this.spaceHostOrbitId)));
            context.setPacketHandled(true);
        }
    }
}
