package com.nythicalnorm.voxelspaceprogram.spacecraft.hostspace;

import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitId;
import com.nythicalnorm.voxelspaceprogram.spacecraft.EntityOrbitBody;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class PlayerHostSpace extends OrbitHostSpace {
    private Vector3d velocityForLastGameTick = new Vector3d();

    public PlayerHostSpace(OrbitId orbitIdOfHost, Vector3d originPos, EntityOrbitBody entityOrbitBody) {
        super(orbitIdOfHost, originPos, entityOrbitBody);
    }

    @Override
    public void OnGameTick() {
        super.OnGameTick();
        for (Entity entity : nonHostEntities) {
            Vec3 ogVel = entity.getDeltaMovement();
            entity.setDeltaMovement(ogVel.x + velocityForLastGameTick.x, ogVel.y + velocityForLastGameTick.y,
                    ogVel.z + velocityForLastGameTick.z);
        }
        velocityForLastGameTick.zero();
    }

    @Override
    public void applyHostVelocity(Vector3d addedVel) {
        super.applyHostVelocity(addedVel);
        velocityForLastGameTick.add(addedVel);
    }
}
