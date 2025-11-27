
package com.nythicalnorm.nythicalSpaceProgram.planet;

import com.nythicalnorm.nythicalSpaceProgram.solarsystem.OrbitalElements;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3fc;

public class PlanetaryBody {
    private final OrbitalElements orbitalElements;
    private final String[] childBodies;
    private final double radius;
    private final double mass;
    private final AxisAngle4f NorthPoleDir;
    private final float RotationPeriod;
    private final PlanetAtmosphere atmoshpericEffects;
    public ResourceLocation texture; //temp val

    private Vector3d planetRelativePos;
    private Vector3d planetAbsolutePos;
    private Quaternionf planetRotation;
    private double SOI;

    public PlanetaryBody (@Nullable OrbitalElements orbitalElements, PlanetAtmosphere effects, @Nullable String[] childBody, double radius, double mass, Vector3fc normalizedNorthPoleDir, float startingRot, float rotationPeriod, ResourceLocation texture) {
        this.orbitalElements = orbitalElements;
        this.radius = radius;
        this.texture = texture;
        this.NorthPoleDir = new AxisAngle4f(startingRot, normalizedNorthPoleDir);
        this.RotationPeriod = rotationPeriod;
        this.atmoshpericEffects = effects;
        this.childBodies = childBody;
        this.mass = mass;
        planetRelativePos = new Vector3d(0d, 0d, 0d);
        planetAbsolutePos = new Vector3d(0d, 0d, 0d);
        planetRotation = new Quaternionf();
    }

    public void simulate(double TimeElapsed, Vector3d parentPos) {
        if (orbitalElements != null) {
            this.planetRelativePos = orbitalElements.ToCartesian(TimeElapsed);
            Vector3d newAbs = new Vector3d(parentPos);
            this.planetAbsolutePos = newAbs.add(planetRelativePos);
            this.NorthPoleDir.angle = (float)((2*Math.PI/RotationPeriod)*TimeElapsed);
            this.planetRotation = new Quaternionf(NorthPoleDir) ;
        }
    }

    public void simulateChildren(double TimeElapsed, Vector3d parentPos) {
        simulate(TimeElapsed, parentPos);

        if (childBodies != null) {
            for (String body : childBodies) {
                Planets.PLANETARY_BODIES.get(body).simulateChildren(TimeElapsed, planetAbsolutePos);
            }
        }
    }

    public void UpdateSOIs() {
        if (childBodies != null) {
            for (String bodyname : childBodies) {
                PlanetaryBody body =  Planets.PLANETARY_BODIES.get(bodyname);
                double soi = Math.pow(body.mass/this.mass, 0.4d);
                soi = soi * body.orbitalElements.SemiMajorAxis;
                body.setSphereOfInfluence(soi);
            }
        }
    }

    public double getRadius(){
        return radius;
    }

    public Quaternionf getPlanetRotation() {
        return planetRotation;
    }

    public PlanetAtmosphere getAtmoshpere() {
        return atmoshpericEffects;
    }

    public double getAccelerationDueToGravity() {
        double val = OrbitalElements.universalGravitationalConstant*this.mass;
        return val/(radius*radius);
    }

    public Vector3d getPlanetRelativePos() {
        return new Vector3d(planetRelativePos);
    }

    public Vector3d getPlanetAbsolutePos() {
        return new Vector3d(planetAbsolutePos);
    }

    public double getSphereOfInfluence() {
        return SOI;
    }

    public void setSphereOfInfluence(double SOI) {
        this.SOI = SOI;
    }

    public double getAtmosphereRadius() {
        if (!this.atmoshpericEffects.hasAtmosphere()) {
            return 0;
        }
        return this.atmoshpericEffects.getAtmosphereHeight() + this.radius;
    }
}
