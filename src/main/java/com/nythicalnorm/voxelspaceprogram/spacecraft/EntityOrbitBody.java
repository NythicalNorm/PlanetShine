package com.nythicalnorm.voxelspaceprogram.spacecraft;

import com.nythicalnorm.voxelspaceprogram.network.PacketHandler;
import com.nythicalnorm.voxelspaceprogram.network.orbitaldata.ClientboundOrbitChange;
import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitId;
import com.nythicalnorm.voxelspaceprogram.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.voxelspaceprogram.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.voxelspaceprogram.util.calculations.OrbitalCalc;
import com.nythicalnorm.voxelspaceprogram.util.calculations.TimeCalc;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.util.PhysTickOnly;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class EntityOrbitBody extends OrbitalBody {
    protected static final float tolerance = 1E-6f;
    protected @Nullable OrbitId currentHostSpace;
    protected ConcurrentLinkedDeque<Vector3dc> velocityApplyQueue; // is only initialized on server side orbital bodies
    protected final boolean isClientSide;

    public EntityOrbitBody(OrbitalBody.Builder<?> orbitalBuilder, boolean isClientSide) {
        super(orbitalBuilder);
        this.currentHostSpace = null;
        this.isClientSide = isClientSide;
    }

    private void simulateNonTimeWarp() {
        if (this.parent == null) {
            return;
        }
        updateVelocity();
        Vector3dc newtonAcceleration = OrbitalCalc.getNewtonAcceleration(this.parent.getMass(), this.relativeOrbitalPos);
        this.relativeVelocity.add(newtonAcceleration);
        Vector3d velocityPerTick = this.relativeVelocity.div(TimeCalc.PhysTickPerSec, new Vector3d());

        this.relativeOrbitalPos = this.relativeOrbitalPos.add(velocityPerTick);
    }

    @PhysTickOnly
    public void simulatePropagate(long TimeElapsed, Vector3dc parentPos, boolean isTimeWarping) {
        if (this.orbitalElements == null) {
            return;
        }

        if (velocityApplyQueue == null || velocityApplyQueue.isEmpty()) {
            Vector3d[] stateVectors = orbitalElements.ToCartesian(TimeElapsed);
            this.relativeOrbitalPos = stateVectors[0];
            this.relativeVelocity = stateVectors[1];
        } else if (!isClientSide && !isTimeWarping) {
            simulateNonTimeWarp();
            this.orbitalElements.fromCartesian(this.relativeOrbitalPos, this.relativeVelocity, TimeElapsed);
            sendOrbitUpdateToRelevantPlayers();
        }

        absoluteOrbitalPos = this.absoluteOrbitalPos.set(parentPos).add(relativeOrbitalPos);
    }

    protected void sendOrbitUpdateToRelevantPlayers() {
        PacketHandler.sendToAllClients(new ClientboundOrbitChange(this.id, this.orbitalElements));
    }

    private void updateVelocity() {
        Vector3dc impulse;

        while ((impulse = velocityApplyQueue.poll()) != null) {
            this.relativeVelocity.add(impulse);
        }
    }

    public void setHostSpace(OrbitId hostSpace) {
        this.currentHostSpace = hostSpace;
    }

    public Optional<OrbitId> getCurrentHostSpace() {
        if (currentHostSpace != null) {
            return Optional.of(currentHostSpace);
        } else {
            return Optional.empty();
        }
    }

    public boolean isHostOfItsSpace() {
        if (this.currentHostSpace == null) {
            return false;
        } else return this.currentHostSpace.equals(this.id);
    }

    // can be called from the game tick or VS phys ticks, don't call this while time warping
    public void addVelocityForUpdate(Vector3d impulse) {
        velocityApplyQueue.add(impulse);
    }

    public abstract OrbitHostSpace createHostSpace(Vector3d posNew);
}
