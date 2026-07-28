package com.nythicalnorm.planetshine.network.orbitaldata;

import com.nythicalnorm.planetshine.network.ClientPacketHandler;
import com.nythicalnorm.planetshine.network.NetworkEncoders;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundOrbitChange {
    private final OrbitId spacecraftID;
    private final OrbitalElementsc orbitalElements;

    public ClientboundOrbitChange(OrbitId spacecraftID, OrbitalElementsc elements) {
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
