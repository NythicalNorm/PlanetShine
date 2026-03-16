package com.nythicalnorm.planetshine.util.calculations;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OrbitalCalc {
    public static final int MAX_ITERATIONS_ELLIPTICAL = 100;
    // Hyperbolic orbits take more iterations than elliptical orbits, increase this value if your state vectors are increasing to infinity.
    public static final int MAX_ITERATIONS_HYPERBOLIC = 500;
    public static final double TOLERANCE = 1e-15d;
    public static final double ACCELERATION_DUE_TO_GRAVITY_EARTH = 9.80665d;
    public static long tickTime = 0L;

    public static Vector3d getNewtonAcceleration(double parentMass, Vector3dc relativeOrbitalPos) {
        double distance = relativeOrbitalPos.length();
        Vector3d angleVector = new Vector3d(relativeOrbitalPos).div(distance);
        double F = -(OrbitalElements.UniversalGravitationalConstant * parentMass) / (distance*distance);
        F = F / TimeCalc.PhysTickPerSec; // basically calculating it per phys tick, if the physics tick slow down then time slows down so no need for delta time
        return angleVector.mul(F);
    }

    /**
     * @see <a href="https://ntrs.nasa.gov/api/citations/19720016564/downloads/19720016564.pdf">NTRS paper, page 18</a>
     * @see <a href="https://en.wikipedia.org/wiki/Kepler%27s_equation#Inverse_Kepler_equation">Inverse Kepler's Equation - Wikipedia</a>
     * @param meanAnomaly Mean Anomaly.
     * @param eccentricity Eccentricity.
     * @return Estimation of a solution to Kepler's equation.
     */
    public static double ellipticalEccentricAnomaly(double meanAnomaly, double eccentricity) {
        double eccentricAnomaly;

        if (meanAnomaly == 0.0) {
            return meanAnomaly;
        }

        double e0 = meanAnomaly + eccentricity * Math.sin(meanAnomaly);

        int i = 1;

        while (true) {
            double f = e0 - eccentricity * Math.sin(e0) - meanAnomaly;
            double d = 1.0f - eccentricity * Math.cos(e0);
            eccentricAnomaly = e0 - f/d;
            if ((Math.abs(e0-eccentricAnomaly) - TOLERANCE) <= 0.0f) break;
            if (++i > MAX_ITERATIONS_ELLIPTICAL) break;
            e0 = eccentricAnomaly;
        }

        return eccentricAnomaly % (2 * Math.PI);
    }

    /**
     * <p>Note: requires much more iterations than elliptical orbits</p>
     * @see <a href="https://control.asu.edu/Classes/MAE462/462Lecture05.pdf">reference (page 12)</a>
     * @param meanAnomaly Mean Anomaly.
     * @param eccentricity Eccentricity.
     * @return Estimation of a solution to Kepler's hyperbolic equation.
     */
    public static double hyperbolicEccentricAnomaly(double meanAnomaly, double eccentricity) {
        double eccentricAnomaly;

        if (meanAnomaly == 0.0) {
            return meanAnomaly;
        }

        // I don't get this equation, but it cuts the no. of iterations from over 700 to 4 in a few cases.
        // Reference: https://arxiv.org/html/2411.15374v1#S4.F2
        double e0 = Math.log((2.0 * Math.abs(meanAnomaly)) / (eccentricity + 1.8));

        int i = 1;

        while (true) {
            double f = (eccentricity * Math.sinh(e0)) - e0 - meanAnomaly;
            double d = (eccentricity * Math.cosh(e0)) - 1.0d;
            eccentricAnomaly = e0 - f/d;
            if ((Math.abs(e0-eccentricAnomaly) - TOLERANCE) <= 0.0f) break;
            if (++i > MAX_ITERATIONS_HYPERBOLIC) break;
            e0 = eccentricAnomaly;
        }

        return eccentricAnomaly;
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
            H = (trueAnomaly > Math.PI) ? -H : H;

            double timeDiffTerm = (eccentricity * Math.sinh(H) - H) / meanAngularMotion;
            return (lastPeriapsisTime + TimeCalc.timeDoubleToLong(timeDiffTerm));
        }
    }

    public static double getTrueAnomalyFromStateVectors(Vector3dc position, Vector3dc velocity, double Mu) {
        double PosMagnitude = position.length();

        // incredibly jank to use velocity.negate but i don't know what the problem is...
        Vector3d negatedVelocity = velocity.negate(new Vector3d());
        Vector3d momentumVectorH = new Vector3d(position).cross(negatedVelocity);
        Vector3d eccentricityVector = negatedVelocity.cross(momentumVectorH).div(Mu);
        eccentricityVector.sub(position.x() / PosMagnitude, position.y() / PosMagnitude, position.z() / PosMagnitude);

        double trueAnomalyAcosVar = eccentricityVector.dot(position)/(eccentricityVector.length()*PosMagnitude);
        double trueAnomaly = Math.acos(Mth.clamp(trueAnomalyAcosVar, -1, 1));
        trueAnomaly = position.dot(velocity) < 0 ? (2 * Math.PI) - trueAnomaly : trueAnomaly;

        return trueAnomaly;
    }

    public static double getTrueAnomalyFromEccentricAnomaly(double eccentricityAnomaly, double eccentricity) {
        if (eccentricity < 1.0d) {
            return 2 * Math.atan(Math.sqrt((1 + eccentricity) / (1 - eccentricity)) * Math.tan(eccentricityAnomaly / 2) );
        } else {
            return 2 * Math.atan(Math.sqrt((eccentricity + 1) / (eccentricity - 1)) * Math.tanh(eccentricityAnomaly / 2) );
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
                double soi = planet.getSphereOfInfluence(); // * planet.getSphereOfInfluence());

                PlanetInterceptCandidate candidate = new PlanetInterceptCandidate(planet.getOrbitId(),
                        planetSimpleElements, soi, planet.getMinInterceptDistance(), planet.getMaxInterceptDistance());
                planetInterceptCandidateList.add(candidate);
            }
        });
        SimpleOrbit entityOrbit = new SimpleOrbit(orbitBody.getOrbitalElements());
        double startingAnomaly = getTrueAnomalyFromEccentricAnomaly(orbitBody.getOrbitalElements().getEccentricityAnomaly(),
                orbitBody.getOrbitalElements().getEccentricity());
        return calculateFutureForNextOrbit(entityOrbit, startingAnomaly, orbitBody.getOrbitalElements(), planetInterceptCandidateList, timeElapsed);
    }

    private static SOIIntercept calculateFutureForNextOrbit(SimpleOrbit entityOrbit, double startingAnomaly, OrbitalElementsc originalOrbit,
                                                            List<PlanetInterceptCandidate> planetInterceptCandidates, long timeElapsed) {
        boolean calculatedThisTime;
        double timeChange = 0.12083048667d;
        double maxAnomaly = startingAnomaly + (2 * Math.PI);
        Vector3d distanceCheckVector = new Vector3d();

        for (double trueAnomoly = startingAnomaly; trueAnomoly <= maxAnomaly; trueAnomoly += timeChange) {
            calculatedThisTime = false;
            double radius = entityOrbit.getRadius(trueAnomoly);

            for (PlanetInterceptCandidate planetIntersect : planetInterceptCandidates) {
                if (radius > planetIntersect.minIntersect && radius < planetIntersect.maxIntersect) {
                    if (!calculatedThisTime) {
                        entityOrbit.calculateCurrentPos(radius, trueAnomoly);
                        calculatedThisTime = true;
                    }

                    long time = getTimeStampFromTrueAnomaly(entityOrbit.MeanAngularMotion, trueAnomoly,
                            entityOrbit.Eccentricity, originalOrbit.getLastPeriapsisTime(timeElapsed));

                    planetIntersect.orbitalElements().ToCartesianRot(time);

                    distanceCheckVector.set(entityOrbit.getRelativePosition());
                    double dist = distanceCheckVector.distanceSquared(planetIntersect.orbitalElements.getRelativePosition());

                    // very arbitrary calculation but should cover most cases i think
                    // double planetInterceptRange = (entityOrbit.getMeanAngularMotion() * 2 * Math.PI * entityOrbit.SemiMajorAxis) * 52;

                    if (dist < (planetIntersect.Soi() * planetIntersect.Soi() * 9)) {
                        tickTime = Util.getNanos() - tickTime;
                        long prevIterTime = getTimeStampFromTrueAnomaly(entityOrbit.MeanAngularMotion, (trueAnomoly - (2 * timeChange)),
                                entityOrbit.Eccentricity, originalOrbit.getLastPeriapsisTime(timeElapsed));

                        long nextIterTime = time + (originalOrbit.getOrbitalPeriodLong() / 52);

                        SOIIntercept intercept = convergeOnIntercept(entityOrbit, prevIterTime, nextIterTime, planetIntersect);
                        if (intercept != null) {
                            PlanetShine.log("Holy Shit, I am cooking; Time: " + time);
                            return intercept;
                        }
                    }
                }
            }
        }

        tickTime = Util.getNanos() - tickTime;
        return null;
    }

    private static SOIIntercept convergeOnIntercept(SimpleOrbit entityOrbit, long startingTime, long endTime,
                                            PlanetInterceptCandidate interceptCandidate) {
        long currentTestTime = startingTime;
        SimplePlanetOrbit planetOrbit = interceptCandidate.orbitalElements;
        long timeStep = TimeCalc.timeDoubleToLong(2048d);

        while (currentTestTime < endTime) {
            currentTestTime += timeStep;
            entityOrbit.toEntityCartesian(currentTestTime);
            planetOrbit.ToCartesianRot(currentTestTime);

            double dist = entityOrbit.getRelativePosition().distance(planetOrbit.getRelativePosition()) - interceptCandidate.Soi;
             if (dist <= 0) {
                if (timeStep <= TimeCalc.timeDoubleToLong(1d)) {
                    double trueAnomaly = getTrueAnomalyFromEccentricAnomaly(entityOrbit.getEccentricAnomaly(currentTestTime), entityOrbit.Eccentricity);

                    return new SOIIntercept(trueAnomaly, currentTestTime, interceptCandidate.orbitId(), false);
                }
                currentTestTime = currentTestTime - (2 * timeStep);
                timeStep = timeStep / 2;
            }
        }
        return null;
    }

    public record SOIIntercept(double trueAnomaly, long timeElapsed, OrbitId interceptingBody, boolean isEscape) {}

    private record PlanetInterceptCandidate(OrbitId orbitId, SimplePlanetOrbit orbitalElements, double Soi, double minIntersect, double maxIntersect) {}

    private static class SimpleOrbit {
        protected final double SemiMajorAxis;
        protected final double SemiMinorAxis;
        protected final double Eccentricity;
        protected final long periapsisTime;

        protected final double MeanAngularMotion;

        protected final Vector3d relativePosition = new Vector3d();

        public SimpleOrbit(OrbitalElementsc elements) {
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

            double Anomaly = isElliptical ? ellipticalEccentricAnomaly(M, Eccentricity) : hyperbolicEccentricAnomaly(M, Eccentricity);
            double sinAnomaly =  (isElliptical) ? Math.sin(Anomaly) : Math.sinh(Anomaly);
            double cosAnomaly =  (isElliptical) ?  org.joml.Math.cosFromSin(sinAnomaly, Anomaly) : Math.cosh(Anomaly);

            this.relativePosition.set(this.SemiMajorAxis * (cosAnomaly - this.Eccentricity), 0d, -this.SemiMinorAxis * sinAnomaly);
        }

        public double getEccentricAnomaly(long timeElapsed) {
            boolean isElliptical = Eccentricity < 1;
            double M = this.MeanAngularMotion * (OrbitalElements.getModulusCurrentTime(timeElapsed, periapsisTime, this.Eccentricity, MeanAngularMotion));
            return isElliptical ? ellipticalEccentricAnomaly(M, Eccentricity) : hyperbolicEccentricAnomaly(M, Eccentricity);
        }

        public double getRadius(double trueAnomaly) {
            double semiLatus = Eccentricity < 1 ? SemiMajorAxis * (1 - Eccentricity * Eccentricity) :
                    Math.abs(SemiMajorAxis) * (Eccentricity * Eccentricity - 1);

            return semiLatus / (1 + Eccentricity * Math.cos(trueAnomaly));
        }

        public void calculateCurrentPos(double radius, double trueAnomaly) {
            double sinVal = Math.sin(trueAnomaly);
            double cosVal = org.joml.Math.cosFromSin(sinVal, trueAnomaly);
            this.relativePosition.set(radius * cosVal, 0d, radius * sinVal);
        }

        public double getMeanAngularMotion() {
            return MeanAngularMotion;
        }

        public Vector3d getRelativePosition() {
            return relativePosition;
        }
    }

    private static class SimplePlanetOrbit extends SimpleOrbit {
        private final Quaterniondc relativeRotation;

        public SimplePlanetOrbit(OrbitalElementsc elements, Quaterniondc relativeRotation) {
            super(elements);
            this.relativeRotation = relativeRotation;
        }

        // only works for elliptical planet orbits, hyperbolic orbits for planets don't work anyway.
        public void ToCartesianRot(long timeElapsed) {
            double M = this.MeanAngularMotion * OrbitalElements.getModulusCurrentTime(timeElapsed, periapsisTime, Eccentricity, MeanAngularMotion);

            //Eccentric anomaly also this works for circular orbits I think
            double Anomaly = ellipticalEccentricAnomaly(M, this.Eccentricity);
            double sinAnomaly = Math.sin(Anomaly);
            double cosAnomaly = org.joml.Math.cosFromSin(sinAnomaly, Anomaly);

            this.relativePosition.set(this.SemiMajorAxis * (cosAnomaly - this.Eccentricity), 0d, -this.SemiMinorAxis * sinAnomaly);
            this.relativeRotation.transform(this.relativePosition);
        }
    }
}