package com.nythicalnorm.planetshine.spacecraft.player;

import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundHostOrbitSet;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitChange;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ServerPlayerOrbitBody extends AbstractPlayerOrbitBody {

    public ServerPlayerOrbitBody(PlayerOrbitBuilder playerSpacecraftBuilder) {
        super(playerSpacecraftBuilder, false);
        velocityApplyQueue = new ConcurrentLinkedDeque<>();
    }

    public UUID getUUid() {
        return player.getUUID();
    }

    @Override
    public void setHostSpace(OrbitId hostSpace) {
        super.setHostSpace(hostSpace);
        if (this.player != null) {
            PacketHandler.sendToPlayer(new ClientboundHostOrbitSet(hostSpace), (ServerPlayer) this.player);
        }
    }

    @Override
    protected void sendOrbitUpdateToRelevantPlayers() {
        if (this.player != null) {
            PacketHandler.sendToPlayer(new ClientboundOrbitChange(this.id, this.orbitalElements), (ServerPlayer) this.player);
        }
    }
}
