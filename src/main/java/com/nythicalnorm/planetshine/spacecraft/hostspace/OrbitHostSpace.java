package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.util.PhysTickOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class OrbitHostSpace {
    protected final OrbitId orbitIdOfHost;
    protected final Vector3d originPos;
    protected final EntityOrbitBody hostBody;
    protected final List<Entity> nonHostEntities;
    private final AtomicReference<Vector3d> velocityForLastGameTick;
    private final AtomicReference<Vector3d> velocityForLastPhysTick;

    public OrbitHostSpace(OrbitId orbitIdOfHost, Vector3d originPos, EntityOrbitBody entityOrbitBody) {
        this.orbitIdOfHost = orbitIdOfHost;
        this.originPos = originPos;
        nonHostEntities = new ArrayList<>();
        this.hostBody = entityOrbitBody;
        this.velocityForLastGameTick = new AtomicReference<>(new Vector3d());
        this.velocityForLastPhysTick = new AtomicReference<>(new Vector3d());
    }

    public void OnGameTick() {
        Vector3dc velocity = velocityForLastGameTick.get();

        if (velocity.x() == 0.0d && velocity.y() == 0.0d && velocity.z() == 0.0d) {
            return;
        }

        for (Entity entity : nonHostEntities) {
            Vec3 ogVel = entity.getDeltaMovement();
            entity.setDeltaMovement(ogVel.x - velocity.x(), ogVel.y - velocity.y(),
                    ogVel.z - velocity.z());
        }

        velocityForLastGameTick.set(new Vector3d());
    }

    @PhysTickOnly
    public void onPhysTick() {

    }

    public Vector3d getOriginPos() {
        return originPos;
    }

    public OrbitId getOrbitIdOfHost() {
        return orbitIdOfHost;
    }

    public void addEntityToHostSpace(Entity entity) {
        if (entity instanceof Player player
                && hostBody.getOrbitId().getUUID().equals(player.getUUID())) {
            return;
        }
        nonHostEntities.add(entity);
    }

    public void removeEntityFromHostSpace(Entity entity) {
        nonHostEntities.remove(entity);
    }

    public void applyHostVelocity(Vector3d addedVel) {
        Vector3d gameTickVel = new Vector3d(velocityForLastGameTick.get());
        velocityForLastGameTick.set(gameTickVel.add(addedVel));

        Vector3d physTickVel = new Vector3d(velocityForLastPhysTick.get());
        velocityForLastPhysTick.set(physTickVel.add(addedVel));
    }
}
