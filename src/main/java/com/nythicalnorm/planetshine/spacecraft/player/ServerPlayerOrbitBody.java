package com.nythicalnorm.planetshine.spacecraft.player;

import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundHostOrbitSet;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerPlayerOrbitBody extends AbstractPlayerOrbitBody {
    public ServerPlayerOrbitBody(PlayerOrbitBuilder playerSpacecraftBuilder) {
        super(playerSpacecraftBuilder, false);
        this.velocityApplyQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void setHostSpaceId(OrbitId hostSpace) {
        super.setHostSpaceId(hostSpace);
        if (this.player != null) {
            PacketHandler.sendToPlayer(new ClientboundHostOrbitSet(hostSpace), (ServerPlayer) this.player);
        }
    }

    @Override
    public boolean isPlayerLoggedIn() {
        return this.player != null;
    }

}
