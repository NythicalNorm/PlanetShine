package com.nythicalnorm.planetshine.spacecraft;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitChange;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
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
    // basically whether the next planet intercept of escape or intersection is calculated yet.
    private OrbitalCalc.SOIIntercept nextOrbitIntercept = null;

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
            PlanetShine.logError("Entity Orbit of " + this.getDisplayName() + "is Not in a state for Orbital Calculations");
            return;
        }
        // checking if its time for the predicted SOI change
        if (this.nextOrbitIntercept != null && this.nextOrbitIntercept.timeElapsed() <= TimeElapsed && this.isHostOfItsSpace() && !this.isClientSide) {
            CelestialBody newParent = this.calculateSOIChange(this.nextOrbitIntercept);
            this.absoluteOrbitalPos.set(this.parent.getAbsolutePos()).add(this.relativeOrbitalPos);

            OrbitHostSpace hostSpace = this.orbitHostSpace.get();
            if (hostSpace != null && newParent != null) {
                hostSpace.changeSOI(newParent.getOrbitId(), this.getOrbitalElements());
            } else {
                PlanetShine.logError("Couldn't find body / host space from the earlier intercept calculations");
            }
            this.nextOrbitIntercept = null;
        }
        // if this isn't the host of its space set the orbit based on the host.
        if (this.getHostSpaceAccess() != null && this.getHostSpaceAccess().getHostBody() != null && this.isBodyEntityLoaded() && !this.isHostOfItsSpace()) {
            Vector3dc originPos = this.getHostSpaceAccess().getOriginPos();
            Vector3dc hostPos = this.getHostSpaceAccess().getHostBody().getRelativePos();
            this.setStateVectorsFromHostBody(originPos, hostPos, TimeElapsed);
        } else { // if its not accelerating do the normal simulation
            if (velocityApplyQueue == null || velocityApplyQueue.isEmpty()) {
                this.simulateFromKeplerian(TimeElapsed);
            } else if (!isClientSide && !isTimeWarping) {
                simulateNonTimeWarp();
                this.orbitalElements.fromCartesian(this.relativeOrbitalPos, this.relativeVelocity, TimeElapsed);
                this.sendOrbitUpdateToRelevantPlayers();
                this.nextOrbitIntercept = null;
            }
        }

        this.absoluteOrbitalPos.set(this.parent.getAbsolutePos()).add(this.relativeOrbitalPos);
    }

    protected void simulateFromKeplerian(long timeElapsed) {
        Vector3d[] stateVectors = orbitalElements.ToCartesian(timeElapsed);
        this.relativeOrbitalPos.set(stateVectors[0]);
        this.relativeVelocity.set(stateVectors[1]);
    }

    private void simulateNonTimeWarp() {
        if (this.parent == null) {
            return;
        }
        this.applyQueuedVelocity();
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

    private void applyQueuedVelocity() {
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

    @PhysTickOnly
    private @Nullable CelestialBody calculateSOIChange(OrbitalCalc.SOIIntercept nextOrbitIntercept) {
        //this is basically making sure that the change happens in the right place
        this.simulateFromKeplerian(this.nextOrbitIntercept.timeElapsed());

        if (nextOrbitIntercept.isEscape()) {
            CelestialBody newParent = this.getParent().getParent();
            if (newParent != null) {
                Vector3d escapeRelPos = new Vector3d(this.getParent().getRelativePos()).add(this.getRelativePos());
                Vector3d escapeRelVel = new Vector3d(this.getParent().getRelativeVelocity()).add(this.getRelativeVelocity());
                this.orbitalElements = new OrbitalElements(escapeRelPos, escapeRelVel, this.nextOrbitIntercept.timeElapsed(), newParent.getMass());
                return newParent;
            } else {
                // you are going to the end dimension my friend.
                return null;
            }
        } else {
            CelestialBody newParent = this.getParent().getPlanetChild(nextOrbitIntercept.interceptingBody());
            if (newParent != null) {
                Vector3d escapeRelPos = new Vector3d(this.getRelativePos()).sub(newParent.getRelativePos());
                Vector3d escapeRelVel = new Vector3d(this.getRelativeVelocity()).sub(newParent.getRelativeVelocity());
                this.orbitalElements = new OrbitalElements(escapeRelPos, escapeRelVel, this.nextOrbitIntercept.timeElapsed(), newParent.getMass());
            }
            return newParent;
        }
    }

    public @Nullable OrbitalCalc.SOIIntercept calculateIntercept(long elapsedTime) {
        if (this.orbitalElements == null || this.parent == null) {
            PlanetShine.logError("Invalid state for EntityOrbitBody : " + this.getDisplayName());
            return null;
        }
        // first calculate intercept with planets with the same parent
        //this.nextOrbitIntercept = OrbitalCalc.findAllRelativePlanetIntercepts(this, elapsedTime, this.parent.getPlanetChildren());

        // if that fails than the final step is checking that escape Intercepts fail too:
        if (this.nextOrbitIntercept == null) {
            this.nextOrbitIntercept = this.orbitalElements.findOrbitEscapeIntercept(this.parent, elapsedTime);
        }
        return this.nextOrbitIntercept;
    }

    public boolean isOrbitInterceptsCalculated() {
        return this.nextOrbitIntercept != null;
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
        if (PSServer.get().isTimeWarping()) {
            return;
        }
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
