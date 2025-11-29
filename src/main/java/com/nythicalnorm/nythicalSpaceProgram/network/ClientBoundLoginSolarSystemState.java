package com.nythicalnorm.nythicalSpaceProgram.network;

import com.nythicalnorm.nythicalSpaceProgram.NythicalSpaceProgram;
import com.nythicalnorm.nythicalSpaceProgram.common.PlayerOrbitalData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientBoundLoginSolarSystemState {
    private final PlayerOrbitalData playerData;

    public ClientBoundLoginSolarSystemState(PlayerOrbitalData playerData) {
        this.playerData = playerData;
    }

    public ClientBoundLoginSolarSystemState(FriendlyByteBuf friendlyByteBuf) {
        this.playerData = new PlayerOrbitalData();
        playerData.decode(friendlyByteBuf);
    }

    public void encode(FriendlyByteBuf friendlyByteBuf) {
        playerData.encode(friendlyByteBuf);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        if (contextSupplier.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> NythicalSpaceProgram.startClient(playerData));
            context.setPacketHandled(true);
        }
    }
}
