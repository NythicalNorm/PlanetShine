package com.nythicalnorm.planetshine.network.orbitaldata;

import com.nythicalnorm.planetshine.network.ClientPacketHandler;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ClientboundHostSpaceOrbitIDSet {
    private final OrbitId entityID;
    private final OrbitId hostOrbitId;

    public ClientboundHostSpaceOrbitIDSet(OrbitId entityID, @Nullable OrbitId hostOrbitId) {
        this.entityID = entityID;
        this.hostOrbitId = hostOrbitId;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        this.entityID.encodeToBuffer(friendlyByteBuf);

        if (hostOrbitId != null) {
            friendlyByteBuf.writeBoolean(true);
            hostOrbitId.encodeToBuffer(friendlyByteBuf);
        } else {
            friendlyByteBuf.writeBoolean(false);
        }
    }

    public ClientboundHostSpaceOrbitIDSet(FriendlyByteBuf friendlyByteBuf) {
        this.entityID = new OrbitId(friendlyByteBuf);

        if (friendlyByteBuf.readBoolean()) {
            this.hostOrbitId = new OrbitId(friendlyByteBuf);
        } else {
            this.hostOrbitId = null;
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT ) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientPacketHandler.hostSpaceOrbitIDSet(this.entityID, this.hostOrbitId)));
            context.setPacketHandled(true);
        }
    }
}
