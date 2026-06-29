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

public class ClientboundStateVectorChange {
    private final OrbitId spacecraftID;
    private final Vector3d relativePosition;
    private final Vector3d relativeVelocity;

    public ClientboundStateVectorChange(OrbitId spacecraftID, Vector3d relativePosition, Vector3d relativeVelocity) {
        this.spacecraftID = spacecraftID;
        this.relativePosition = relativePosition;
        this.relativeVelocity = relativeVelocity;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        spacecraftID.encodeToBuffer(friendlyByteBuf);
        NetworkEncoders.writeVector3d(friendlyByteBuf, relativePosition);
        NetworkEncoders.writeVector3d(friendlyByteBuf, relativeVelocity);
    }

    public ClientboundStateVectorChange(FriendlyByteBuf friendlyByteBuf) {
        this.spacecraftID = new OrbitId(friendlyByteBuf);
        this.relativePosition = NetworkEncoders.readVector3d(friendlyByteBuf);
        this.relativeVelocity = NetworkEncoders.readVector3d(friendlyByteBuf);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT ) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientPacketHandler.stateVectorChange(this.spacecraftID, this.relativePosition, this.relativeVelocity)));
            context.setPacketHandled(true);
        }
    }
}
