package com.nythicalnorm.planetshine.spacecraft.player;

import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundHostOrbitSet;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostAccessor;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerPlayerOrbitBody extends AbstractPlayerOrbitBody {
    public ServerPlayerOrbitBody(PlayerOrbitBuilder playerSpacecraftBuilder) {
        super(playerSpacecraftBuilder, false);
        this.velocityApplyQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public OrbitHostAccessor getHostSpaceAccess() {
        return this.orbitHostSpace.get();
    }

    @Override
    public void OnRemove() {
        super.OnRemove();
        if (this.body != null) {
            PacketHandler.sendToPlayer(new ClientboundHostOrbitSet(null, null), (ServerPlayer) this.body);
        }
    }
}
