package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.PlayerOrbitAccessor;
import com.nythicalnorm.planetshine.util.Calc;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2ic;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.util.GameTickOnly;
import org.valkyrienskies.core.api.util.PhysTickOnly;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class OrbitHostSpace implements OrbitHostAccessor {
    protected final OrbitId orbitIdOfHost;
    protected final Vector2ic originPos;
    protected final EntityOrbitBody hostBody;

    @GameTickOnly
    protected final ConcurrentLinkedQueue<Entity> nonHostEntities;
    protected final ConcurrentLinkedQueue<ServerPlayer> nonHostPlayers;

    protected final ConcurrentLinkedQueue<Vector3dc> velocityForLastGameTick;

    public OrbitHostSpace(OrbitId orbitIdOfHost, Vector2ic originPos, EntityOrbitBody entityOrbitBody) {
        this.orbitIdOfHost = orbitIdOfHost;
        this.originPos = originPos;
        this.nonHostEntities = new ConcurrentLinkedQueue<>();
        this.nonHostPlayers = new ConcurrentLinkedQueue<>();
        this.hostBody = entityOrbitBody;
        this.velocityForLastGameTick = new ConcurrentLinkedQueue<>();
    }

    @Override
    public EntityOrbitBody getHostBody() {
        return hostBody;
    }

    @Override
    public Vector3d getOriginPos() {
        return new Vector3d(originPos.x(), 128, originPos.y());
    }

    @Override
    public OrbitId getOrbitIdOfHost() {
        return orbitIdOfHost;
    }

    public void OnGameTick() {
        Vector3d velocity = Calc.pollVectorQueue(velocityForLastGameTick);

        if (velocity.x() == 0.0d && velocity.y() == 0.0d && velocity.z() == 0.0d) {
            return;
        }

        Iterator<Entity> entityIterator = this.nonHostEntities.iterator();

        while (entityIterator.hasNext()) {
            Entity entity = entityIterator.next();
            if (PSServer.get().getHostSpaceManager().getHostSpacePos(entity.position()).equals(this.originPos.x(), this.originPos.y())
                    && !entity.isPassenger()) {
                Vec3 ogVel = entity.getDeltaMovement();
                entity.setDeltaMovement(ogVel.x - velocity.x(), ogVel.y - velocity.y(),
                        ogVel.z - velocity.z());
            } else {
                removeEntityFromHostSpace(entity);
            }
        }
    }

    @PhysTickOnly
    public void onPhysTick() {
    }

    public void hostLeft() {
        for (Entity entity : nonHostEntities) { // doesn't work yet.
            if (!(entity instanceof Player)) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
        PSServer.get().getHostSpaceManager().removeHostSpace(this);
    }

    public Vector2ic getOriginPos2I() {
        return originPos;
    }

    public void addEntityToHostSpace(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            addPlayerToHostSpace(player);
        }
        nonHostEntities.add(entity);
    }

    public void addPlayerToHostSpace(ServerPlayer player) {
        if (((PlayerOrbitAccessor) player).getOrbitalBody() != null) {
            ((PlayerOrbitAccessor) player).getOrbitalBody().setHostOrbitSpace(this);
        }
        if (! this.hostBody.getOrbitId().getUUID().equals(player.getUUID())) {
            this.nonHostPlayers.add(player);
        }
    }

    public void removeEntityFromHostSpace(Entity entity) {
        nonHostEntities.remove(entity);
    }

    public void applyHostVelocity(Vector3dc addedVel) {
        this.velocityForLastGameTick.add(addedVel);
    }
}
