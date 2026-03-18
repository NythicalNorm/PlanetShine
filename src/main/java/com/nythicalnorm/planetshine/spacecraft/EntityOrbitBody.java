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
    protected @Nullable OrbitalCalc.SOIIntercept nextOrbitIntercept = null;
    protected double lastCalculatedEccentricAnomaly;

    // server side only
    protected boolean isInterceptsCalculated; // basically whether the next planet intercept of escape or intersection is calculated yet.
    protected @Nullable Long nextPeriapsisTime = 0L;

    public EntityOrbitBody(OrbitalBody.Builder<?> orbitalBuilder, @Nullable OrbitId hostSpaceID, @Nullable OrbitalCalc.SOIIntercept soiIntercept, boolean isClientSide) {
        super(orbitalBuilder);
        this.hostSpaceID = new AtomicReference<>();
        this.orbitHostSpace = new AtomicReference<>();
        this.hostSpaceID.set(hostSpaceID);
        this.isClientSide = isClientSide;
        this.isInterceptsCalculated = false;
        this.nextOrbitIntercept = soiIntercept;
    }

    @GameTickOnly
    public void init() {
        if (!this.isClientSide) {
            resetIntercepts(PSServer.get().getCurrentTime());
        }
    }

    @PhysTickOnly
    public void simulate(long TimeElapsed, boolean isTimeWarping) {
        if (this.orbitalElements == null || this.parent == null || this.hostSpaceID.get() == null) {
            // PlanetShine.logError("Entity Orbit of " + this.getDisplayName().getString() + "is Not in a state for Orbital Calculations");
            return;
        }
        if (!isClientSide && this.nextPeriapsisTime != null && TimeElapsed > this.nextPeriapsisTime) {
            this.completedOneOrbit(TimeElapsed);
        }
        // checking if it's time for the predicted SOI change and doing it
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
            this.setStateVectorsFromHostBody(originPos, this.getHostSpaceAccess().getHostBody(), TimeElapsed);
        } else { // if its not accelerating do the normal simulation
            if (velocityApplyQueue == null || velocityApplyQueue.isEmpty()) {
                this.simulateFromKeplerian(TimeElapsed);
            } else if (!isClientSide) { // if it is accelerating do the special calc for this tick
                this.simulateNonTimeWarp();
                this.lastCalculatedEccentricAnomaly = this.orbitalElements.fromCartesian(this.relativeOrbitalPos, this.relativeVelocity, TimeElapsed);
                this.sendOrbitUpdateToRelevantPlayers();
                this.resetIntercepts(TimeElapsed);
                this.nextPeriapsisTime = this.orbitalElements.getNextPeriapsisTime(TimeElapsed);
            }
        }

        this.absoluteOrbitalPos.set(this.parent.getAbsolutePos()).add(this.relativeOrbitalPos);
    }

    protected void simulateFromKeplerian(long timeElapsed) {
        this.lastCalculatedEccentricAnomaly = this.orbitalElements.ToCartesian(timeElapsed, this.relativeOrbitalPos, this.relativeVelocity);
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

    protected void setStateVectorsFromHostBody(Vector3dc originPos, EntityOrbitBody hostBody, long TimeElapsed) {
        Vector3dc mcVelocity = this.getMcVelocity();
        Vector3dc mcPosition = this.getMcPosition();

        if (mcVelocity == null || mcPosition == null) {
            return;
        }

        Vector3d relativePos = new Vector3d();
        Vector3d relativeVel = new Vector3d(mcVelocity);
        mcPosition.sub(originPos, relativePos);
        this.nextOrbitIntercept = hostBody.getNextOrbitIntercept();

        // need to change this so this isn't as janky.
        if (relativePos.lengthSquared() > 1 || relativeVel.lengthSquared() > 1) {
            this.relativeOrbitalPos.set(relativePos.add(hostBody.getRelativePos()));
            this.relativeVelocity.set(relativeVel.add(this.getHostSpaceAccess().getHostBody().getRelativeVelocity()));
            this.lastCalculatedEccentricAnomaly = this.orbitalElements.fromCartesian(this.relativeOrbitalPos, this.relativeVelocity, TimeElapsed);
        } else {
            this.relativeOrbitalPos.set(hostBody.getRelativePos());
            this.relativeVelocity.set(hostBody.getRelativeVelocity());
            this.orbitalElements.set(hostBody.getOrbitalElements());
            this.lastCalculatedEccentricAnomaly = hostBody.getEccentricAnomaly();
        }
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

    public @Nullable OrbitalCalc.SOIIntercept getNextOrbitIntercept() {
        return nextOrbitIntercept;
    }

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
        if (!this.isClientSide && this.orbitHostSpace.get() != null) {
            this.orbitHostSpace.get().removeOrbitBody(this);
        }
        this.hostSpaceID.set(null);
        this.orbitHostSpace.set(null);
    }

    // function called every time the body completes one revolution around its host body, if a body just started orbiting that orbit doesn't count.
    protected void completedOneOrbit(long TimeElapsed) {
        this.resetIntercepts(TimeElapsed);
        this.calculateIntercepts(TimeElapsed);
    }

    @PhysTickOnly
    private @Nullable CelestialBody calculateSOIChange(OrbitalCalc.SOIIntercept nextOrbitIntercept) {
        //this is basically making sure that the change happens in the right place
        this.simulateFromKeplerian(this.nextOrbitIntercept.timeElapsed());

        if (nextOrbitIntercept.isEscape()) {
            CelestialBody newParent = this.getParent().getParent();
            Vector3d newParentPos = new Vector3d();
            Vector3d newParentVel = new Vector3d();
            this.getParent().getOrbitalElements().ToCartesian(getNextOrbitIntercept().timeElapsed(), newParentPos, newParentVel);

            if (newParent != null) {
                Vector3d escapeRelPos = new Vector3d(newParentPos).add(this.getRelativePos());
                Vector3d escapeRelVel = new Vector3d(newParentVel).add(this.getRelativeVelocity());
                this.orbitalElements = new OrbitalElements(escapeRelPos, escapeRelVel, this.nextOrbitIntercept.timeElapsed(), newParent.getMass());
                return newParent;
            } else {
                // you are going to the end dimension my friend.
                return null;
            }
        } else {
            CelestialBody newParent = this.getParent().getPlanetChild(nextOrbitIntercept.interceptingBody());
            Vector3d newParentPos = new Vector3d();
            Vector3d newParentVel = new Vector3d();

            if (newParent != null) {
                newParent.getOrbitalElements().ToCartesian(getNextOrbitIntercept().timeElapsed(), newParentPos, newParentVel);
                Vector3d escapeRelPos = new Vector3d(this.getRelativePos()).sub(newParentPos);
                Vector3d escapeRelVel = new Vector3d(this.getRelativeVelocity()).sub(newParentVel);
                this.orbitalElements = new OrbitalElements(escapeRelPos, escapeRelVel, this.nextOrbitIntercept.timeElapsed(), newParent.getMass());
            }
            return newParent;
        }
    }

    @PhysTickOnly
    public @Nullable OrbitalCalc.SOIIntercept calculateIntercepts(long elapsedTime) {
        if (this.orbitalElements == null || this.parent == null) {
            PlanetShine.logError("Invalid state for EntityOrbitBody : " + this.getDisplayName());
            return null;
        }
        // first calculate intercept with planets with the same parent
        if (!this.parent.getPlanetChildren().isEmpty()) {
            this.nextOrbitIntercept = OrbitalCalc.findAllRelativePlanetIntercepts(this, elapsedTime, this.parent.getPlanetChildren());
        }

        // if that fails than start checking that escape Intercepts fail too:
        if (this.nextOrbitIntercept == null) {
            this.nextOrbitIntercept = this.orbitalElements.findOrbitEscapeIntercept(this.parent, elapsedTime);
        }
        this.isInterceptsCalculated = true;
        return this.nextOrbitIntercept;
    }

    public void resetIntercepts(long currentTime) {
        this.isInterceptsCalculated = false;
        this.nextOrbitIntercept = null;
        this.nextPeriapsisTime = this.orbitalElements.getNextPeriapsisTime(currentTime);
    }

    // client-side only
    public void setIntercept(OrbitalCalc.@Nullable SOIIntercept soiIntercept) {
        this.nextOrbitIntercept = soiIntercept;
    }

    //client-side
    public void calculateEscapeOnly(long elapsedTime) {
        assert this.orbitalElements != null;
        assert this.parent != null;
        if (this.nextOrbitIntercept == null) {
            this.nextOrbitIntercept = this.orbitalElements.findOrbitEscapeIntercept(this.parent, elapsedTime);
            this.isInterceptsCalculated = true;
        }
    }

    public boolean isOrbitInterceptsCalculated() {
        return isInterceptsCalculated;
    }

    public double getEccentricAnomaly() {
        return lastCalculatedEccentricAnomaly;
    }

    public Optional<OrbitId> getHostSpaceID() {
        OrbitId currentHostSpace = this.hostSpaceID.get();
        if (currentHostSpace != null) {
            return Optional.of(currentHostSpace);
        } else {
            return Optional.empty();
        }
    }

    public boolean isClientSide() {
        return isClientSide;
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
            velocityApplyQueue.clear();
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
