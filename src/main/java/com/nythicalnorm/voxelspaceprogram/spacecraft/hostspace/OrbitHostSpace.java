package com.nythicalnorm.voxelspaceprogram.spacecraft.hostspace;

import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitId;
import com.nythicalnorm.voxelspaceprogram.spacecraft.EntityOrbitBody;
import com.nythicalnorm.voxelspaceprogram.spacecraft.player.AbstractPlayerOrbitBody;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.util.PhysTickOnly;

import java.util.ArrayList;
import java.util.List;

public abstract class OrbitHostSpace {
    protected final OrbitId orbitIdOfHost;
    protected final Vector3d originPos;
    protected final EntityOrbitBody hostBody;
    protected final List<Entity> nonHostEntities;
    private final Vector3d velocityForLastGameTick;
    private final Vector3d velocityForLastPhysTick;

    public OrbitHostSpace(OrbitId orbitIdOfHost, Vector3d originPos, EntityOrbitBody entityOrbitBody) {
        this.orbitIdOfHost = orbitIdOfHost;
        this.originPos = originPos;
        nonHostEntities = new ArrayList<>();
        this.hostBody = entityOrbitBody;
        this.velocityForLastGameTick = new Vector3d();
        this.velocityForLastPhysTick = new Vector3d();
    }

    public void OnGameTick() {
        if (velocityForLastGameTick.x == 0.0d && velocityForLastGameTick.y == 0.0d && velocityForLastGameTick.z == 0.0d) {
            return;
        }

        for (Entity entity : nonHostEntities) {
            Vec3 ogVel = entity.getDeltaMovement();
            entity.setDeltaMovement(ogVel.x - velocityForLastGameTick.x, ogVel.y - velocityForLastGameTick.y,
                    ogVel.z - velocityForLastGameTick.z);
        }
        velocityForLastGameTick.zero();
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
                && hostBody instanceof AbstractPlayerOrbitBody playerOrbitBody
                && playerOrbitBody.getPlayerEntity().equals(player)) {
            return;
        }
        nonHostEntities.add(entity);
    }

    public void removeEntityToHostSpace(Entity entity) {
        nonHostEntities.remove(entity);
    }

    public synchronized void applyHostVelocity(Vector3d addedVel) {
        velocityForLastGameTick.add(addedVel);
        velocityForLastPhysTick.add(addedVel);
    }
}
