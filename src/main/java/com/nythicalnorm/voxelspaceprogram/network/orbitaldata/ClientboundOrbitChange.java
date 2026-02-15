package com.nythicalnorm.voxelspaceprogram.network.orbitaldata;

import com.nythicalnorm.voxelspaceprogram.network.ClientPacketHandler;
import com.nythicalnorm.voxelspaceprogram.network.NetworkEncoders;
import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitId;
import com.nythicalnorm.voxelspaceprogram.solarsystem.orbits.OrbitalElements;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundOrbitChange {
    private final OrbitId spacecraftID;
    private final OrbitalElements orbitalElements;

    public ClientboundOrbitChange(OrbitId spacecraftID, OrbitalElements elements) {
        this.spacecraftID = spacecraftID;
        this.orbitalElements = elements;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        spacecraftID.encodeToBuffer(friendlyByteBuf);
        NetworkEncoders.writeOrbitalElements(friendlyByteBuf, orbitalElements);
    }

    public ClientboundOrbitChange(FriendlyByteBuf friendlyByteBuf) {
        this.spacecraftID = new OrbitId(friendlyByteBuf);
        this.orbitalElements = NetworkEncoders.readOrbitalElements(friendlyByteBuf);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT ) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientPacketHandler.orbitChange(this.spacecraftID, this.orbitalElements)));
            context.setPacketHandled(true);
        }
    }
}
