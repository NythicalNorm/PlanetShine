package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.util.Calc;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2ic;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.util.GameTickOnly;
import org.valkyrienskies.core.api.util.PhysTickOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class OrbitHostSpace {
    protected final OrbitId orbitIdOfHost;
    protected final Vector2ic originPos;
    protected final EntityOrbitBody hostBody;

    @GameTickOnly
    protected final List<Entity> nonHostEntities;
    protected final List<ServerPlayer> nonHostPlayers;

    private final ConcurrentLinkedQueue<Vector3dc> velocityForLastGameTick;
    private final ConcurrentLinkedQueue<Vector3dc> velocityForLastPhysTick;

    public OrbitHostSpace(OrbitId orbitIdOfHost, Vector2ic originPos, EntityOrbitBody entityOrbitBody) {
        this.orbitIdOfHost = orbitIdOfHost;
        this.originPos = originPos;
        this.nonHostEntities = new ArrayList<>();
        this.nonHostPlayers = new ArrayList<>();
        this.hostBody = entityOrbitBody;
        this.velocityForLastGameTick = new ConcurrentLinkedQueue<>();
        this.velocityForLastPhysTick = new ConcurrentLinkedQueue<>();
    }

    public void OnGameTick() {
        Vector3d velocity = Calc.pollVectorQueue(velocityForLastGameTick);

        if (velocity.x() == 0.0d && velocity.y() == 0.0d && velocity.z() == 0.0d) {
            return;
        }

        for (Entity entity : nonHostEntities) {
            if (! PSServer.get().getEntityShipManager().getHostSpacePos(entity.position()).equals(this.originPos.x(), this.originPos.y())) {
                removeEntityFromHostSpace(entity);
                continue;
            }
            Vec3 ogVel = entity.getDeltaMovement();
            entity.setDeltaMovement(ogVel.x - velocity.x(), ogVel.y - velocity.y(),
                    ogVel.z - velocity.z());
        }
    }

    @PhysTickOnly
    public void onPhysTick() {
        velocityForLastPhysTick.clear();
    }

    public void hostLeft() {
//        for (Entity entity : nonHostEntities) { // doesn't work yet.
//            if (!(entity instanceof Player)) {
//                entity.remove(Entity.RemovalReason.DISCARDED);
//            }
//        }
        PSServer.get().getEntityShipManager().removeHostSpace(this);
    }

    public Vector3d getOriginPos() {
        return new Vector3d(originPos.x(), 128, originPos.y());
    }

    public Vector2ic getOriginPos2I() {
        return originPos;
    }

    public OrbitId getOrbitIdOfHost() {
        return orbitIdOfHost;
    }

    public void addEntityToHostSpace(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            if (hostBody.getOrbitId().getUUID().equals(player.getUUID())) {
                return;
            } else {
                this.nonHostPlayers.add(player);
            }
        }
        nonHostEntities.add(entity);
    }

    public void removeEntityFromHostSpace(Entity entity) {
        nonHostEntities.remove(entity);
    }

    public void applyHostVelocity(Vector3d addedVel) {
        this.velocityForLastGameTick.add(addedVel);
        this.velocityForLastPhysTick.add(addedVel);
    }
}
