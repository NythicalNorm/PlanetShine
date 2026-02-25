package com.nythicalnorm.planetshine.network.orbitaldata;

import com.nythicalnorm.planetshine.network.ClientPacketHandler;
import com.nythicalnorm.planetshine.network.NetworkEncoders;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundEntityBodyJoinOrbital {
    private final EntityOrbitBody entityOrbitBody;
    private final OrbitId orbitParent;

    public ClientboundEntityBodyJoinOrbital(EntityOrbitBody orbitBody) {
        this.entityOrbitBody = orbitBody;

        OrbitId parentID = null;
        if (entityOrbitBody != null) {
            if (entityOrbitBody.getParent() != null) {
                parentID = entityOrbitBody.getParent().getOrbitId();
            }
        }
        orbitParent = parentID;
    }

    public ClientboundEntityBodyJoinOrbital(FriendlyByteBuf friendlyByteBuf) {
        EntityOrbitBody entityorbitbody = null;
        OrbitId entityParent = null;

        OrbitalBody orbitalBody = NetworkEncoders.readOrbitalBodyClient(friendlyByteBuf);
        if (orbitalBody instanceof EntityOrbitBody spacecraftBody) {
            entityorbitbody = spacecraftBody;
            if (friendlyByteBuf.readBoolean()) {
                entityParent = new OrbitId(friendlyByteBuf);
            }
        }

        this.entityOrbitBody = entityorbitbody;
        this.orbitParent = entityParent;
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        NetworkEncoders.writeOrbitalBody(friendlyByteBuf, this.entityOrbitBody);

        if (this.entityOrbitBody.getParent() != null) {
            friendlyByteBuf.writeBoolean(true);
            this.entityOrbitBody.getParent().getOrbitId().encodeToBuffer(friendlyByteBuf);
        } else {
            friendlyByteBuf.writeBoolean(false);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientPacketHandler.entityBodyJoinOrbital(this.entityOrbitBody, this.orbitParent)));
            context.setPacketHandled(true);
        }
    }
}
