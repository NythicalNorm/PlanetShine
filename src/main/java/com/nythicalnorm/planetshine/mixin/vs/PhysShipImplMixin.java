package com.nythicalnorm.planetshine.mixin.vs;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.spacecraft.spaceship.ServerSpaceshipBody;
import com.nythicalnorm.planetshine.util.Calc;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import java.util.ArrayDeque;

@Mixin(PhysShipImpl.class)
public abstract class PhysShipImplMixin {
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
    public abstract double getMass();

    @Shadow
    public abstract long getId();

    @Inject(method = "applyQueuedForces", at = @At(value = "HEAD"), remap = false)
    public void applyForces(CallbackInfo ci) {
        if (PSServer.get() != null) {
            ServerSpaceshipBody serverSpaceshipBody = (ServerSpaceshipBody) PSServer.get().getSolarSystem().getSpaceshipFromVSId(getId());

            if (serverSpaceshipBody != null && serverSpaceshipBody.isHostOfItsSpace()) {
                double forceThreshold = 0.1 * getMass();
                double forceThresholdSquared = forceThreshold * forceThreshold;

                Vector3d invForcesTotal = Calc.pollVectorQueue(invForces);
                if (invForcesTotal.lengthSquared() > forceThresholdSquared) {
                    serverSpaceshipBody.addVelocityForUpdate(invForcesTotal.div(getMass()));
                }

                Vector3d rotForcesTotal = Calc.pollVectorQueue(rotForces);
                if (rotForcesTotal.lengthSquared() > forceThresholdSquared) {
                    rotForcesTotal = rotateForceToWorld(rotForcesTotal);
                    rotForcesTotal.div(getMass());

                    serverSpaceshipBody.addVelocityForUpdate(rotForcesTotal);
                }

                while(!invPosForces.isEmpty()) {
                    Vector3dc invPosPosition = invPosPositions.removeFirst(); // need to figure out the moment of inertia rotation shit
                    Vector3dc invPosForce = invPosForces.removeFirst();
                    if (invPosForce.lengthSquared() > forceThresholdSquared) {
                        serverSpaceshipBody.addVelocityForUpdate(new Vector3d(invPosForce).div(getMass() * TimeCalc.PhysTickPerSec));
                    }
                }

                invPosPositions.clear();
                invPosForces.clear();
            }
        }
    }
}
