package com.nythicalnorm.planetshine.util.calculations;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OrbitalCalc {
    public static final double ACCELERATION_DUE_TO_GRAVITY_EARTH = 9.80665d;
    public static long tickTime = 0L;

    public static Vector3d getNewtonAcceleration(double parentMass, Vector3dc relativeOrbitalPos) {
        double distance = relativeOrbitalPos.length();
        Vector3d angleVector = new Vector3d(relativeOrbitalPos).div(distance);
        double F = -(OrbitalElements.UniversalGravitationalConstant * parentMass) / (distance*distance);
        F = F / TimeCalc.PhysTickPerSec; // basically calculating it per phys tick, if the physics tick slow down then time slows down so no need for delta time
        return angleVector.mul(F);
    }

    // copy of a method in Orbital Elements, this is more suited for SOI calcs.
    public static long getTimeStampFromTrueAnomaly(double meanAngularMotion, double trueAnomaly, double eccentricity,
                                                    long lastPeriapsisTime) {
        if (eccentricity < 1) {
            double E = 2 * Math.atan2(Math.tan(trueAnomaly * 0.5d), Math.sqrt((1 + eccentricity) / (1 - eccentricity)));

            double timeDiffTerm = (E - eccentricity * Math.sin(E)) / meanAngularMotion;
            return (lastPeriapsisTime + TimeCalc.timeDoubleToLong(timeDiffTerm));
        } else {
            double cosTrueAnomoly = Math.cos(trueAnomaly);
            double H = invCosh((eccentricity + cosTrueAnomoly) / (1 + eccentricity * cosTrueAnomoly));

            double timeDiffTerm = (eccentricity * Math.sinh(H) - H) / meanAngularMotion;
            return (lastPeriapsisTime + TimeCalc.timeDoubleToLong(timeDiffTerm));
        }
    }

    public static double invCosh(double x) {
        if (x < 1.0) {
            return Double.NaN;
        }
        return Math.log(x + Math.sqrt(x*x - 1));
    }

    public static @Nullable SOIIntercept findAllRelativePlanetIntercepts(EntityOrbitBody orbitBody,
                                                                         long timeElapsed, Collection<CelestialBody> planetChildren) {
        tickTime = Util.getNanos();
        double entityApoapsis = orbitBody.getOrbitalElements().getApoapsis();
        // hyperbolic orbits don't circle back, so the current pos can be taken as the minimum it will ever be
        double entityPeriapsis = orbitBody.getOrbitalElements().isHyperbolic() ? orbitBody.getRelativePos().length() : orbitBody.getOrbitalElements().getPeriapsis();

        List<PlanetInterceptCandidate> planetInterceptCandidateList = new ArrayList<>();

        Quaterniond reverseRotation = new Quaterniond(orbitBody.getOrbitalElements().getOrbitRotation()).invert();

        planetChildren.forEach(planet -> {
            if (Math.max(entityPeriapsis, planet.getMinInterceptDistance()) <= Math.min(entityApoapsis, planet.getMaxInterceptDistance())) {
                Quaterniond combinedRelativeRotation = new Quaterniond(planet.getOrbitalElements().getOrbitRotation());
                combinedRelativeRotation.mul(reverseRotation);
                SimplePlanetOrbit planetSimpleElements = new SimplePlanetOrbit(planet.getOrbitalElements(), combinedRelativeRotation);
                double soiSquare = (planet.getSphereOfInfluence() * planet.getSphereOfInfluence()) * 1.2d;

                PlanetInterceptCandidate candidate = new PlanetInterceptCandidate(planet.getOrbitId(),
                        planetSimpleElements, soiSquare, planet.getMinInterceptDistance(), planet.getMaxInterceptDistance());
                planetInterceptCandidateList.add(candidate);
            }
        });
        SimpleOrbit entityOrbit = new SimpleOrbit(orbitBody.getOrbitalElements());

        return calculateFutureForNextOrbit(entityOrbit, planetInterceptCandidateList, timeElapsed);
    }


    private static SOIIntercept calculateFutureForNextOrbit(SimpleOrbit entityOrbit,
                                                    List<PlanetInterceptCandidate> planetInterceptCandidates, long timeElapsed) {
        boolean calculatedThisTime;
        double orbitalPeriod = (2*Math.PI)/entityOrbit.MeanAngularMotion;
        long maxTime = timeElapsed + TimeCalc.timeDoubleToLong(orbitalPeriod);
        long timeChange = TimeCalc.timeDoubleToLong(orbitalPeriod) / 52;
        Vector3d distanceCheckVector = new Vector3d();

        for (long time = timeElapsed; time <= maxTime; time += timeChange) {
            entityOrbit.toEntityCartesian(time);

            for (PlanetInterceptCandidate planetIntersect : planetInterceptCandidates) {
                planetIntersect.orbitalElements().ToCartesianRot(time);

                distanceCheckVector.set(entityOrbit.getRelativePosition());
                double dist = distanceCheckVector.distanceSquared(planetIntersect.orbitalElements.getRelativePosition());

                if (dist < planetIntersect.SoiSquare()) {
                    PlanetShine.log("Holy Shit, I am cooking; Time: " + time);
                    tickTime = Util.getNanos() - tickTime;
                    return new SOIIntercept(0d, time, planetIntersect.orbitId(), false);
                }
            }
        }

        tickTime = Util.getNanos() - tickTime;
        return null;
    }

    public record SOIIntercept(double trueAnomaly, long timeElapsed, OrbitId interceptingBody, boolean isEscape) {}

    private record PlanetInterceptCandidate(OrbitId orbitId, SimplePlanetOrbit orbitalElements, double SoiSquare, double minIntersect, double maxIntersect) {}

    private static class SimpleOrbit {
        protected final double SemiMajorAxis;
        protected final double SemiMinorAxis;
        protected final double Eccentricity;
        protected final long periapsisTime;

        protected final double MeanAngularMotion;

        protected final Vector3d relativePosition = new Vector3d();

        public SimpleOrbit(OrbitalElements elements) {
            this.SemiMajorAxis = elements.getSemiMajorAxis();
            this.Eccentricity = elements.getEccentricity();
            this.periapsisTime = elements.getPeriapsisTime();
            this.MeanAngularMotion = elements.getMeanAngularMotion();
            this.SemiMinorAxis = (Eccentricity < 1) ? SemiMajorAxis * Math.sqrt(1 - (Eccentricity*Eccentricity)) :
                    -SemiMajorAxis * Math.sqrt((Eccentricity*Eccentricity) - 1);
        }

        public void toEntityCartesian(long timeElapsed) {
            boolean isElliptical = Eccentricity < 1;
            double M = this.MeanAngularMotion * (OrbitalElements.getModulusCurrentTime(timeElapsed, periapsisTime, this.Eccentricity, MeanAngularMotion));

            //Eccentric anomaly also this works for circular orbits I think
            double Anomaly = isElliptical ? OrbitalElements.ellipticalEccentricAnomaly(M, Eccentricity) : OrbitalElements.hyperbolicEccentricAnomaly(M, Eccentricity);
            double sinAnomaly =  (isElliptical) ? Math.sin(Anomaly) : Math.sinh(Anomaly);
            double cosAnomaly =  (isElliptical) ?  org.joml.Math.cosFromSin(sinAnomaly, Anomaly) : Math.cosh(Anomaly);

            this.relativePosition.set(this.SemiMajorAxis * (cosAnomaly - this.Eccentricity), 0d, -this.SemiMinorAxis * sinAnomaly);
        }

        public double getRadius(double trueAnomaly) {
            double semiLatus = Eccentricity < 1 ? SemiMajorAxis * (1 - Eccentricity * Eccentricity) :
                    Math.abs(SemiMajorAxis) * (Eccentricity * Eccentricity - 1);

            return semiLatus / (1 + Eccentricity * Math.cos(trueAnomaly));
        }

//
//        public void calculateCurrentPos(double radius, double trueAnomaly) {
//            double sinVal = Math.sin(trueAnomaly);
//            double cosVal = org.joml.Math.cosFromSin(sinVal, trueAnomaly);
//            this.relativePosition.set(radius * cosVal, 0d, radius * sinVal);
//        }

        public Vector3d getRelativePosition() {
            return relativePosition;
        }
    }

    private static class SimplePlanetOrbit extends SimpleOrbit {
        private final Quaterniondc relativeRotation;

        public SimplePlanetOrbit(OrbitalElements elements, Quaterniondc relativeRotation) {
            super(elements);
            this.relativeRotation = relativeRotation;
        }

        // only works for elliptical planet orbits
        public void ToCartesianRot(long timeElapsed) {
            double M = this.MeanAngularMotion * OrbitalElements.getModulusCurrentTime(timeElapsed, periapsisTime, Eccentricity, MeanAngularMotion);

            //Eccentric anomaly also this works for circular orbits I think
            double Anomaly = OrbitalElements.ellipticalEccentricAnomaly(M, this.Eccentricity);
            double sinAnomaly = Math.sin(Anomaly);
            double cosAnomaly = org.joml.Math.cosFromSin(sinAnomaly, Anomaly);

            this.relativePosition.set(this.SemiMajorAxis * (cosAnomaly - this.Eccentricity), 0d, -this.SemiMinorAxis * sinAnomaly);
            this.relativeRotation.transform(this.relativePosition);
        }
    }
}