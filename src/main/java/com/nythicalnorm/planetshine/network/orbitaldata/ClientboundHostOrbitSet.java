package com.nythicalnorm.planetshine.network.orbitaldata;

import com.nythicalnorm.planetshine.network.ClientPacketHandler;
import com.nythicalnorm.planetshine.network.NetworkEncoders;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;

import java.util.function.Supplier;

public class ClientboundHostOrbitSet {
    private final OrbitId spaceHostOrbitId;
    private final Vector3d originPos;

    public ClientboundHostOrbitSet(OrbitId spaceHostOrbitId, Vector3d originPos) {
        this.spaceHostOrbitId = spaceHostOrbitId;
        this.originPos = originPos;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        if (spaceHostOrbitId != null) {
            friendlyByteBuf.writeBoolean(true);
            spaceHostOrbitId.encodeToBuffer(friendlyByteBuf);
            NetworkEncoders.writeVector3d(friendlyByteBuf, originPos);
        } else {
            friendlyByteBuf.writeBoolean(false);
        }
    }

    public ClientboundHostOrbitSet(FriendlyByteBuf friendlyByteBuf) {
        if (friendlyByteBuf.readBoolean()) {
            this.spaceHostOrbitId = new OrbitId(friendlyByteBuf);
            this.originPos = NetworkEncoders.readVector3d(friendlyByteBuf);
        } else {
            this.spaceHostOrbitId = null;
            this.originPos = null;
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT ) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientPacketHandler.hostOrbitSet(this.spaceHostOrbitId, this.originPos)));
            context.setPacketHandled(true);
        }
    }
}
