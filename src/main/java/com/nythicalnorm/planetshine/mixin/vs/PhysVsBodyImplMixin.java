package com.nythicalnorm.planetshine.mixin.vs;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.spacecraft.spaceship.ServerSpaceshipBody;
import com.nythicalnorm.planetshine.util.calculations.MiscCalc;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import org.joml.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.bodies.properties.BodyInertia;
import org.valkyrienskies.core.api.bodies.properties.BodyKinematics;
import org.valkyrienskies.core.impl.game.bodies.PhysVsBodyImpl;

import java.util.ArrayDeque;

@Mixin(PhysVsBodyImpl.class)
public abstract class PhysVsBodyImplMixin {
    @Shadow
    @Final
    private ArrayDeque<Vector3dc> invForces;

    @Shadow
    @Final
    private ArrayDeque<Vector3dc> rotForces;

    @Shadow
    protected abstract Vector3d rotateForceToWorld(Vector3dc $this$rotateForceToWorld);

    @Shadow
    @Final
    private ArrayDeque<Vector3dc> invPosForces;

    @Shadow
    @Final
    private ArrayDeque<Vector3dc> invPosPositions;

    @Shadow
    public abstract long getId();

    @Shadow
    public abstract void applyWorldTorque(Vector3dc torqueInWorld);

    @Shadow
    protected BodyKinematics kinematics;

    @Shadow
    private BodyInertia inertiaData;

    @Inject(method = "applyQueuedForces", at = @At(value = "HEAD"), remap = false)
    public void applyForces(CallbackInfo ci) {
        if (PSServer.get() != null) {
            ServerSpaceshipBody serverSpaceshipBody = (ServerSpaceshipBody) PSServer.get().getSolarSystem().getSpaceshipFromVSId(getId());

            if (serverSpaceshipBody != null && serverSpaceshipBody.isHostOfItsSpace()) {
                double forceThreshold = 0.1 * inertiaData.getMass();
                double forceThresholdSquared = forceThreshold * forceThreshold;

                Vector3d invForcesTotal = MiscCalc.pollVectorQueue(invForces);
                if (invForcesTotal.lengthSquared() > forceThresholdSquared) {
                    serverSpaceshipBody.addVelocityForUpdate(invForcesTotal.div(inertiaData.getMass()));
                }

                Vector3d rotForcesTotal = MiscCalc.pollVectorQueue(rotForces);
                if (rotForcesTotal.lengthSquared() > forceThresholdSquared) {
                    rotForcesTotal = rotateForceToWorld(rotForcesTotal);
                    rotForcesTotal.div(inertiaData.getMass());

                    serverSpaceshipBody.addVelocityForUpdate(rotForcesTotal);
                }

                while(!invPosForces.isEmpty()) {
                    Vector3dc invPosPosition = invPosPositions.removeFirst();
                    Vector3dc invPosForce = invPosForces.removeFirst();

                    if (invPosPosition.length() > 0.1d) {
                        Matrix3d rotationMatrix = new Matrix3d().set(kinematics.getRotation());
                        Matrix3d worldInertia = rotationMatrix.mul(inertiaData.getInertiaTensor(), new Matrix3d()).mul(rotationMatrix.transpose());
                        Vector3d angularImpulse = new Vector3d(invPosPosition).rotate(kinematics.getRotation()).cross(invPosForce);
                        Vector3d deltaOmega = worldInertia.invert().transform(angularImpulse);
                        applyWorldTorque(deltaOmega.mul(inertiaData.getMass()));

                    }

                    if (invPosForce.lengthSquared() > forceThresholdSquared) {
                        serverSpaceshipBody.addVelocityForUpdate(new Vector3d(invPosForce).div(inertiaData.getMass() * TimeCalc.PhysTickPerSec));
                    }
                }

                invPosPositions.clear();
                invPosForces.clear();
            }
        }
    }
}
