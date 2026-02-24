package com.nythicalnorm.planetshine.network.orbitaldata;

import com.nythicalnorm.planetshine.network.ClientPacketHandler;
import com.nythicalnorm.planetshine.network.NetworkEncoders;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class ClientboundLoginEntityBodiesList {
    private final List<EntityOrbitBody> entityOrbitBodyList;
    private List<NetworkEncoders.TempEntityOrbitHolder> entityOrbitHolders;

    public ClientboundLoginEntityBodiesList(List<EntityOrbitBody> entityOrbitBodyList) {
        this.entityOrbitBodyList = entityOrbitBodyList;
        this.entityOrbitHolders = null;
    }

    public ClientboundLoginEntityBodiesList(FriendlyByteBuf friendlyByteBuf) {
        this.entityOrbitBodyList = null;
        this.entityOrbitHolders = NetworkEncoders.readEntityBodyList(friendlyByteBuf);
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        NetworkEncoders.writeEntityBodyList(friendlyByteBuf, entityOrbitBodyList);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT && this.entityOrbitHolders != null) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientPacketHandler.entityBodyList(this.entityOrbitHolders)));
            context.setPacketHandled(true);
        }
    }
}
