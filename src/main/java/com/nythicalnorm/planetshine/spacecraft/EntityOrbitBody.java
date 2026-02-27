package com.nythicalnorm.planetshine.spacecraft;

import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitChange;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.util.GameTickOnly;
import org.valkyrienskies.core.api.util.PhysTickOnly;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public abstract class EntityOrbitBody extends OrbitalBody {
    protected static final float tolerance = 1E-6f;
    protected final AtomicReference<OrbitId> currentHostSpace;
    protected final AtomicReference<OrbitHostSpace> orbitHostSpace;
    protected ConcurrentLinkedQueue<Vector3dc> velocityApplyQueue; // is only initialized on server side orbital bodies
    protected final boolean isClientSide;

    public EntityOrbitBody(OrbitalBody.Builder<?> orbitalBuilder, @Nullable OrbitId currentHostSpace, boolean isClientSide) {
        super(orbitalBuilder);
        this.currentHostSpace = new AtomicReference<>();
        this.orbitHostSpace = new AtomicReference<>();
        this.currentHostSpace.set(currentHostSpace);
        this.isClientSide = isClientSide;
    }

    @GameTickOnly
    public void init() { }

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

    @PhysTickOnly
    public void simulate(long TimeElapsed, boolean isTimeWarping) {
        if (this.orbitalElements == null || this.parent == null) {
            return;
        }

        if (velocityApplyQueue == null || velocityApplyQueue.isEmpty()) {
            Vector3d[] stateVectors = orbitalElements.ToCartesian(TimeElapsed);
            this.relativeOrbitalPos.set(stateVectors[0]);
            this.relativeVelocity.set(stateVectors[1]);
        } else if (!isClientSide && !isTimeWarping) {
            simulateNonTimeWarp();
            this.orbitalElements.fromCartesian(this.relativeOrbitalPos, this.relativeVelocity, TimeElapsed);
            sendOrbitUpdateToRelevantPlayers();
        }

        this.absoluteOrbitalPos.set(this.parent.getAbsolutePos()).add(relativeOrbitalPos);
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

    public void setHostSpaceId(OrbitId hostSpace) {
        this.currentHostSpace.set(hostSpace);
    }

    public void setHostOrbitSpace(OrbitHostSpace playerHostSpace) {
        this.orbitHostSpace.set(playerHostSpace);
    }

    public void removeHostSpaces() {
        if (this.isHostOfItsSpace() && !this.isClientSide) {
            this.orbitHostSpace.get().hostLeft();
        }
        this.currentHostSpace.set(null);
        this.orbitHostSpace.set(null);
    }

    public Optional<OrbitId> getCurrentHostSpace() {
        OrbitId currentHostSpace = this.currentHostSpace.get();
        if (currentHostSpace != null) {
            return Optional.of(currentHostSpace);
        } else {
            return Optional.empty();
        }
    }

    public boolean isHostOfItsSpace() {
        OrbitId hostSpace = this.currentHostSpace.get();
        if (hostSpace == null) {
            return false;
        } else {
            return hostSpace.equals(this.id);
        }
    }

    // Thread safe, don't call this while time warping
    public void addVelocityForUpdate(Vector3d impulse) {
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
}
