package com.nythicalnorm.planetshine.spacecraft;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.spacecraft.hostspace.HostSpaceManager;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostAccessor;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.valkyrienskies.core.api.util.GameTickOnly;
import org.valkyrienskies.core.api.util.PhysTickOnly;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public abstract class EntityOrbitBody<T> extends OrbitalBody {
    protected static final float tolerance = 1E-6f;
    protected final AtomicReference<OrbitId> hostSpaceID;
    protected final AtomicReference<OrbitHostSpace> orbitHostSpace;
    protected ConcurrentLinkedQueue<Vector3dc> velocityApplyQueue; // is only initialized on server side orbital bodies
    protected @Nullable OrbitalCalc.SOIIntercept nextOrbitIntercept;
    protected double lastCalculatedEccentricAnomaly;
    protected T body;
    protected boolean isStateVecControlled;

    // server side only
    protected boolean isInterceptsCalculated; // basically whether the next planet intercept of escape or intersection is calculated yet.
    protected OptionalLong nextPeriapsisTime = OptionalLong.empty();

    public EntityOrbitBody(OrbitalBody.Builder<?> orbitalBuilder, @Nullable OrbitId hostSpaceID,
                           @Nullable OrbitalCalc.SOIIntercept soiIntercept, boolean isClientSide) {
        super(orbitalBuilder, isClientSide);
        this.hostSpaceID = new AtomicReference<>();
        this.orbitHostSpace = new AtomicReference<>();
        this.hostSpaceID.set(hostSpaceID);
        this.isInterceptsCalculated = false;
        this.nextOrbitIntercept = soiIntercept;
        this.isStateVecControlled = false;
    }

    @GameTickOnly
    public void init() {
        if (!this.isClientSide) {
            resetIntercepts(PSServer.get().getCurrentTime());
        }
    }

    @PhysTickOnly
    public void simulate(long TimeElapsed, boolean isTimeWarping, float deltaTime) {
        if (this.orbitalElements == null || this.parent == null || this.hostSpaceID.get() == null) {
            // PlanetShine.logError("Entity Orbit of " + this.getDisplayName().getString() + "is Not in a state for Orbital Calculations");
            return;
        }

        // updating if its in atmosphere
        if (!this.isClientSide && this.isHostOfItsSpace() && this.isBodyEntityLoaded()) {
            boolean isNowInStateVec =
                    (this.getAltitude() <= this.parent.getAtmosphere().getAtmosphereHeight() && this.parent.getAtmosphere().hasAtmosphere());// ||
//                    (this.relativeVelocity.length() < (this.parent.getEscapeVelocity()) / 250.0d);

            if (isStateVecControlled && !isNowInStateVec) {
                // exiting atmosphere
                this.resetIntercepts(TimeElapsed);
                this.lastCalculatedEccentricAnomaly = this.orbitalElements.fromCartesian(this.relativeOrbitalPos, this.relativeVelocity, TimeElapsed);
                if (this.orbitalElements.getEccentricity() > 0.9999d && !this.orbitalElements.isHyperbolic()) {
                    isNowInStateVec = true;
                }
            }
            else if (isNowInStateVec && !isStateVecControlled) {
                // entering atmosphere
            }
            this.isStateVecControlled = isNowInStateVec;
        }
        if (!isStateVecControlled && !isClientSide && this.nextPeriapsisTime.isPresent() && TimeElapsed > this.nextPeriapsisTime.getAsLong()) {
            this.completedOneOrbit(TimeElapsed);
        }

        // checking if it's time for the predicted SOI change and doing it
        if (!isStateVecControlled && this.nextOrbitIntercept != null && this.nextOrbitIntercept.timeElapsed() <= TimeElapsed && this.isHostOfItsSpace() && !this.isClientSide) {
            CelestialBody newParent = OrbitalCalc.calculateSOIChange(this.nextOrbitIntercept, this.parent, this.orbitalElements, this.orbitalElements);
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
            this.setStateVectorsFromHostBody(this.getHostSpaceAccess(), TimeElapsed);
        } else { // if its not accelerating do the normal simulation
            if (velocityApplyQueue == null || velocityApplyQueue.isEmpty()) {
                if (this.isStateVecControlled) {
                    this.simulateNonTimeWarp(deltaTime);
                    PSServer.sendStateUpdateToRelevantPlayers(this);
                } else {
                    this.simulateFromKeplerian(TimeElapsed);
                }
            } else if (!isClientSide) { // if it is accelerating do the special calc for this tick
                this.simulateNonTimeWarp(deltaTime);

                this.lastCalculatedEccentricAnomaly = this.orbitalElements.fromCartesian(this.relativeOrbitalPos, this.relativeVelocity, TimeElapsed);

                if (this.isStateVecControlled) {
                    PSServer.sendStateUpdateToRelevantPlayers(this);
                } else {
                    PSServer.sendOrbitUpdateToRelevantPlayers(this);
                }
                this.resetIntercepts(TimeElapsed);
            }
        }

        this.absoluteOrbitalPos.set(this.parent.getAbsolutePos()).add(this.relativeOrbitalPos);
    }

    public void simulateFromKeplerian(long timeElapsed) {
        this.lastCalculatedEccentricAnomaly = this.orbitalElements.ToCartesian(timeElapsed, this.relativeOrbitalPos, this.relativeVelocity);
    }

    private void simulateNonTimeWarp(float deltaTime) {
        if (this.parent == null) {
            return;
        }
        if (!this.isClientSide) {
            this.applyQueuedVelocity();
        }

        Vector3dc newtonAcceleration = OrbitalCalc.getNewtonAcceleration(this.parent.getMass(), this.relativeOrbitalPos);
        this.relativeVelocity.add(newtonAcceleration);
        Vector3d velocityPerTick = this.relativeVelocity.mul(deltaTime, new Vector3d());

        this.relativeOrbitalPos.add(velocityPerTick);
    }

    protected void setStateVectorsFromHostBody(OrbitHostAccessor orbitHostAccessor, long TimeElapsed) {
        Vector3dc mcVelocity = this.getMcVelocity();
        Vector3dc mcPosition = this.getMcPosition();

        if (mcVelocity == null || mcPosition == null || this.parent == null || this.orbitalElements == null) {
            return;
        }

        Vector3d relativePos = new Vector3d();
        Vector3d relativeVel = new Vector3d(mcVelocity);
        mcPosition.sub(orbitHostAccessor.getOriginPos(), relativePos);

        EntityOrbitBody<?> hostBody = orbitHostAccessor.isUnloadedHostSpace() ? this.parent.getSolarSystem().getSpacecraftOrbit(this.hostSpaceID.get())
                : orbitHostAccessor.getHostBody();

        this.nextOrbitIntercept = hostBody.getNextOrbitIntercept();

        // need to change this so this isn't as janky.
        if ((relativePos.lengthSquared() < 1 && relativeVel.lengthSquared() < 1) || orbitHostAccessor.isUnloadedHostSpace()) {
            this.relativeOrbitalPos.set(hostBody.getRelativePos());
            this.relativeVelocity.set(hostBody.getRelativeVelocity());
            this.orbitalElements.set(hostBody.getOrbitalElements());
            this.lastCalculatedEccentricAnomaly = hostBody.getEccentricAnomaly();
            this.isStateVecControlled = hostBody.isStateVecControlled;
        } else {
            this.relativeOrbitalPos.set(relativePos.add(hostBody.getRelativePos()));
            this.relativeVelocity.set(relativeVel.add(hostBody.getRelativeVelocity()));
            this.isStateVecControlled = hostBody.isStateVecControlled;
            this.lastCalculatedEccentricAnomaly = this.orbitalElements.fromCartesian(this.relativeOrbitalPos, this.relativeVelocity, TimeElapsed);
        }
    }

    public void setStateVecControlled(boolean isAtmo) {
        this.isStateVecControlled = isAtmo;
    }



    private void applyQueuedVelocity() {
        Vector3dc impulse;

        while ((impulse = velocityApplyQueue.poll()) != null) {
            this.relativeVelocity.add(impulse);
        }
    }
    public abstract double getCrossSectionalArea(Vector3d airVelocity);

    // need to do this so it works on both client and server
    public abstract OrbitHostAccessor getHostSpaceAccess();

    public void setHostSpaceId(OrbitId hostSpace) {
        this.hostSpaceID.set(hostSpace);
    }

    public void setBody(@Nullable T body) {
        this.body = body;
    }

    public @Nullable T getBody() {
        return body;
    }

    public boolean isBodyEntityLoaded() {
        return this.body != null;
    }

    public abstract @Nullable Vector3dc getMcPosition();
    public abstract @Nullable Vector3dc getMcVelocity();
    public abstract @Nullable Quaterniondc getMCRotation();

    public @Nullable OrbitalCalc.SOIIntercept getNextOrbitIntercept() {
        return nextOrbitIntercept;
    }

    public void setHostOrbitSpace(OrbitHostSpace hostSpace) {
        if (hostSpace != null) {
            this.hostSpaceID.set(hostSpace.getOrbitIdOfHost());
            this.orbitHostSpace.set(hostSpace);
        } else {
            this.hostSpaceID.set(null);
            this.orbitHostSpace.set(null);
        }
    }

    public void removeHostSpace(boolean isTeleporting) {
        if (!this.isClientSide && this.orbitHostSpace.get() != null) {
            this.orbitHostSpace.get().removeOrbitBody(this, isTeleporting);
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
    public @Nullable OrbitalCalc.SOIIntercept calculateIntercepts(long elapsedTime) {
        double currentTrueAnomaly = OrbitalCalc.getTrueAnomalyFromEccentricAnomaly(this.getEccentricAnomaly(), orbitalElements.getEccentricity());
        this.nextOrbitIntercept = OrbitalCalc.calculateIntercepts(this.orbitalElements, currentTrueAnomaly, this.parent, elapsedTime);
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

    // called when the entity is leaving orbit and entering a another dimension
    public void OnRemove() {

    }

    public abstract void entityLoadedInSpace(T entity, HostSpaceManager hostSpaceManager);
}