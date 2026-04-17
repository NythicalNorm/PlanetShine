package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.ClientPlayerOrbitBody;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3d;

@OnlyIn(Dist.CLIENT)
public class ClientHostSpace implements OrbitHostAccessor {
    private final OrbitId orbitIdOfHost;
    private final Vector3d originPos;
    private final EntityOrbitBody<?> hostBody;

    public ClientHostSpace(OrbitId orbitIdOfHost, Vector3d originPos, EntityOrbitBody<?> hostBody) {
        this.orbitIdOfHost = orbitIdOfHost;
        this.originPos = originPos;
        this.hostBody = hostBody;
    }

    @Override
    public OrbitId getOrbitIdOfHost() {
        return orbitIdOfHost;
    }

    @Override
    public Vector3d getOriginPos() {
        return originPos;
    }

    @Override
    public EntityOrbitBody<?> getHostBody() {
        if (hostBody != null) {
            return hostBody;
        } else {
            return PSClient.get().getSolarSystem().getSpacecraftOrbit(this.orbitIdOfHost);
        }
    }

    @Override
    public boolean isUnloadedHostSpace() {
        ClientPlayerOrbitBody clientPlayerOrbitBody = PSClient.get().getPlayerOrbit();

        if (clientPlayerOrbitBody.getHostSpaceID().isPresent() && this.hostBody != null &&
                clientPlayerOrbitBody.getHostSpaceID().get().equals(this.hostBody.getOrbitId())) {
            return false;
        }
        return true;
    }
}
