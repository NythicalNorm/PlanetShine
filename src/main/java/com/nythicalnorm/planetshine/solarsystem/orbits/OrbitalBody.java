package com.nythicalnorm.planetshine.solarsystem.orbits;

import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

public abstract class OrbitalBody {
    protected final OrbitId id;
    protected Component displayName;
    protected final Vector3d relativeOrbitalPos;
    protected final Vector3d absoluteOrbitalPos;
    protected final Vector3d relativeVelocity;

    protected @Nullable OrbitalElements orbitalElements;
    protected @Nullable CelestialBody parent; // Nullable only in the case of the sun
    protected boolean isStableOrbit;

    public OrbitalBody(OrbitalBody.Builder<?> builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.relativeOrbitalPos = builder.relativeOrbitalPos;
        this.absoluteOrbitalPos = builder.absoluteOrbitalPos;
        this.relativeVelocity = builder.relativeVelocity;
        this.orbitalElements = builder.orbitalElements;
        this.isStableOrbit = builder.isStableOrbit;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public void setDisplayName(Component displayName) {
        this.displayName = displayName;
    }

    public OrbitId getOrbitId() {
        return id;
    }

    public abstract OrbitalBodyType<? extends OrbitalBody, ? extends OrbitalBody.Builder<?>> getType();

    public boolean isStableOrbit() {
        return isStableOrbit;
    }

    public Vector3dc getRelativePos() {
        return relativeOrbitalPos;
    }

    public Vector3dc getAbsolutePos() {
        return absoluteOrbitalPos;
    }

    public Vector3dc getRelativeVelocity() {
        return relativeVelocity;
    }

    public void setParent(@Nullable CelestialBody parent) {
        this.parent = parent;
    }

    public @Nullable CelestialBody getParent() {
        return parent;
    }

    public void setStableOrbit(boolean stableOrbit) {
        isStableOrbit = stableOrbit;
    }

    public abstract void simulatePropagate(long TimeElapsed, Vector3dc parentPos, boolean isTimeWarping);

    public void setOrbitalElements(OrbitalElements orbitalElements) {
        if (this.orbitalElements != null) {
            this.orbitalElements.set(orbitalElements);
        } else {
            this.orbitalElements = new OrbitalElements(orbitalElements);
        }
    }

    public @Nullable OrbitalElements getOrbitalElements() {
        return orbitalElements;
    }

    public double getAltitude(CelestialBody parentBody) {
        if (parentBody != null) {
            return this.relativeOrbitalPos.length() + 0.5d - parentBody.getRadius();
        } else {
            return this.relativeOrbitalPos.length() + 0.5d;
        }
    }

    public void removeParent() {
        if (parent != null) {
            if (parent.hasChild(this)) {
                parent.removeChild(this.id);
                this.parent = null;
            }
        }
    }

    public abstract static class Builder<T extends OrbitalBody> {
        protected OrbitId id = null;
        protected Component displayName = Component.empty();
        protected Vector3d relativeOrbitalPos = new Vector3d();
        protected Vector3d absoluteOrbitalPos = new Vector3d();
        protected Vector3d relativeVelocity = new Vector3d();
        protected @Nullable OrbitalElements orbitalElements;
        protected @Nullable OrbitalBody parent; // Nullable only in the case of the sun
        protected boolean isStableOrbit = true;

        public void setId(OrbitId id) {
            this.id = id;
        }

        public void setDisplayName(Component displayName) {
            this.displayName = displayName;
        }

        public void setRelativeOrbitalPos(Vector3d relativeOrbitalPos) {
            this.relativeOrbitalPos = relativeOrbitalPos;
        }

        public void setAbsoluteOrbitalPos(Vector3d absoluteOrbitalPos) {
            this.absoluteOrbitalPos = absoluteOrbitalPos;
        }

        public void setRelativeVelocity(Vector3d relativeVelocity) {
            this.relativeVelocity = relativeVelocity;
        }

        public void setOrbitalElements(@Nullable OrbitalElements orbitalElements) {
            this.orbitalElements = orbitalElements;
        }

        public void setParent(@Nullable OrbitalBody parent) {
            this.parent = parent;
        }

        public void setStableOrbit(boolean stableOrbit) {
            isStableOrbit = stableOrbit;
        }

        public abstract T build();

        @OnlyIn(Dist.CLIENT)
        public abstract T buildClientSide();
    }
}
