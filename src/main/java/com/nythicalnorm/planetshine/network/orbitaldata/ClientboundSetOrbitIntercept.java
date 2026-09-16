package com.nythicalnorm.planetshine.network.orbitaldata;

import com.nythicalnorm.planetshine.network.ClientPacketHandler;
import com.nythicalnorm.planetshine.network.NetworkEncoders;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ClientboundSetOrbitIntercept {
    private final OrbitId spacecraftID;
    private final @Nullable OrbitalCalc.SOIIntercept soiIntercept;

    public ClientboundSetOrbitIntercept(OrbitId spacecraftID, @Nullable OrbitalCalc.SOIIntercept soiIntercept) {
        this.spacecraftID = spacecraftID;
        this.soiIntercept = soiIntercept;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        spacecraftID.encodeToBuffer(friendlyByteBuf);
        if (this.soiIntercept != null) {
            friendlyByteBuf.writeBoolean(true);
            NetworkEncoders.writeOrbitIntercept(friendlyByteBuf, this.soiIntercept);
        } else {
            friendlyByteBuf.writeBoolean(false);
        }
    }

    public ClientboundSetOrbitIntercept(FriendlyByteBuf friendlyByteBuf) {
        this.spacecraftID = new OrbitId(friendlyByteBuf);
        if (friendlyByteBuf.readBoolean()) {
            this.soiIntercept = NetworkEncoders.readOrbitIntercept(friendlyByteBuf);
        } else {
            this.soiIntercept = null;
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT ) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientPacketHandler.setOrbitIntercept(this.spacecraftID, this.soiIntercept)));
            context.setPacketHandled(true);
        }
    }
}
