package com.nythicalnorm.planetshine.spacecraft;

import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitChange;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostAccessor;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.valkyrienskies.core.api.util.GameTickOnly;
import org.valkyrienskies.core.api.util.PhysTickOnly;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public abstract class EntityOrbitBody extends OrbitalBody {
    protected static final float tolerance = 1E-6f;
    protected final AtomicReference<OrbitId> hostSpaceID;
    protected final AtomicReference<OrbitHostSpace> orbitHostSpace;
    protected ConcurrentLinkedQueue<Vector3dc> velocityApplyQueue; // is only initialized on server side orbital bodies
    protected final boolean isClientSide;

    public EntityOrbitBody(OrbitalBody.Builder<?> orbitalBuilder, @Nullable OrbitId hostSpaceID, boolean isClientSide) {
        super(orbitalBuilder);
        this.hostSpaceID = new AtomicReference<>();
        this.orbitHostSpace = new AtomicReference<>();
        this.hostSpaceID.set(hostSpaceID);
        this.isClientSide = isClientSide;
    }

    @GameTickOnly
    public void init() { }

    @PhysTickOnly
    public void simulate(long TimeElapsed, boolean isTimeWarping) {
        if (this.orbitalElements == null || this.parent == null || this.hostSpaceID.get() == null) {
            return;
        }

        if (this.getHostSpaceAccess() != null && this.getHostSpaceAccess().getHostBody() != null && this.isBodyEntityLoaded() && !this.isHostOfItsSpace()) {
            Vector3dc originPos = this.getHostSpaceAccess().getOriginPos();
            Vector3dc hostPos = this.getHostSpaceAccess().getHostBody().getRelativePos();
            this.setStateVectorsFromHostBody(originPos, hostPos, TimeElapsed);
        } else {
            if (velocityApplyQueue == null || velocityApplyQueue.isEmpty()) {
                Vector3d[] stateVectors = orbitalElements.ToCartesian(TimeElapsed);
                this.relativeOrbitalPos.set(stateVectors[0]);
                this.relativeVelocity.set(stateVectors[1]);
            } else if (!isClientSide && !isTimeWarping) {
                simulateNonTimeWarp();
                this.orbitalElements.fromCartesian(this.relativeOrbitalPos, this.relativeVelocity, TimeElapsed);
                sendOrbitUpdateToRelevantPlayers();
            }
        }

        this.absoluteOrbitalPos.set(this.parent.getAbsolutePos()).add(this.relativeOrbitalPos);
    }

    private void simulateNonTimeWarp() {
        if (this.parent == null) {
            return;
        }
        updateVelocity();
        Vector3dc newtonAcceleration = OrbitalCalc.getNewtonAcceleration(this.parent.getMass(), this.relativeOrbitalPos);
        this.relativeVelocity.add(newtonAcceleration);
        Vector3d velocityPerTick = this.relativeVelocity.div(TimeCalc.PhysTickPerSec, new Vector3d());

        this.relativeOrbitalPos.add(velocityPerTick);
    }

    protected void setStateVectorsFromHostBody(Vector3dc originPos, Vector3dc hostPos, long TimeElapsed) {
        Vector3d relativePos = new Vector3d();
        Vector3d relativeVel = new Vector3d(this.getMcVelocity());
        this.getMcPosition().sub(originPos, relativePos);

        this.relativeOrbitalPos.set(relativePos.add(hostPos));
        this.relativeVelocity.set(relativeVel.add(this.getHostSpaceAccess().getHostBody().getRelativeVelocity()));

        this.orbitalElements.fromCartesian(this.relativeOrbitalPos, this.relativeVelocity, TimeElapsed);
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

    // need to do this so it works on both client and server
    public abstract OrbitHostAccessor getHostSpaceAccess();

    public void setHostSpaceId(OrbitId hostSpace) {
        this.hostSpaceID.set(hostSpace);
    }

    public abstract boolean isBodyEntityLoaded();
    public abstract @Nullable Vector3dc getMcPosition();
    public abstract @Nullable Vector3dc getMcVelocity();
    public abstract @Nullable Quaterniondc getMCRotation();

    public void setHostOrbitSpace(OrbitHostSpace playerHostSpace) {
        if (playerHostSpace != null) {
            this.hostSpaceID.set(playerHostSpace.getOrbitIdOfHost());
            this.orbitHostSpace.set(playerHostSpace);
        } else {
            this.hostSpaceID.set(null);
            this.orbitHostSpace.set(null);
        }
    }

    public void removeHostSpaces() {
        if (this.isHostOfItsSpace() && !this.isClientSide) {
            this.orbitHostSpace.get().hostLeft();
        }
        this.hostSpaceID.set(null);
        this.orbitHostSpace.set(null);
    }

    public Optional<OrbitId> getHostSpaceID() {
        OrbitId currentHostSpace = this.hostSpaceID.get();
        if (currentHostSpace != null) {
            return Optional.of(currentHostSpace);
        } else {
            return Optional.empty();
        }
    }

    public boolean isHostOfItsSpace() {
        OrbitId hostSpace = this.hostSpaceID.get();
        if (hostSpace == null) {
            return false;
        } else {
            return hostSpace.equals(this.id);
        }
    }

    // Thread safe, don't call this while time warping
    public void addVelocityForUpdate(Vector3dc impulse) {
        velocityApplyQueue.add(impulse);
        OrbitHostSpace hostSpace = this.orbitHostSpace.get();
        if (hostSpace != null) {
            hostSpace.applyHostVelocity(impulse);
        }
    }

    public abstract OrbitHostSpace createHostSpace(Vector2ic posNew);

    @OnlyIn(Dist.CLIENT) // kinda sus but hey it works without having generics glorp.
    public boolean drawIcon(GuiGraphics graphics, Vector2i screenPos, int size) {
        return false;
    }

    // called when the entity is leaving orbit and entering a another dimension
    public void OnRemove() {

    }
}
