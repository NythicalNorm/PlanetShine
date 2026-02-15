package com.nythicalnorm.voxelspaceprogram.network.spacecraft;

import com.nythicalnorm.voxelspaceprogram.PSServer;
import com.nythicalnorm.voxelspaceprogram.network.NetworkEncoders;
import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;

import java.util.function.Supplier;

public class ServerboundPlayerHostVelUpdate {
    private final OrbitId playerBodyID;
    private final Vector3d addedVel;

    public ServerboundPlayerHostVelUpdate(OrbitId playerBody, Vector3d addedVel) {
        this.playerBodyID = playerBody;
        this.addedVel = addedVel;
    }

    public ServerboundPlayerHostVelUpdate(FriendlyByteBuf friendlyByteBuf) {
        this.playerBodyID = new OrbitId(friendlyByteBuf);
        this.addedVel = NetworkEncoders.readVector3d(friendlyByteBuf);
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        playerBodyID.encodeToBuffer(friendlyByteBuf);
        NetworkEncoders.writeVector3d(friendlyByteBuf, addedVel);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_SERVER ) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> PSServer.get().getEntityShipManager().handleHostPlayerMove(context.getSender(), playerBodyID, addedVel));
            context.setPacketHandled(true);
        }
    }
}
