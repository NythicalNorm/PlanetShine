package com.nythicalnorm.planetshine.solarsystem.orbits;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.util.calculations.KeplerEquationSolver;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.OptionalLong;

public class OrbitalElements implements OrbitalElementsc {
    public static final double UniversalGravitationalConstant = 6.6743E-11d;

    private double SemiMajorAxis;
    private double Eccentricity;
    private long periapsisTime;

    private double Inclination;
    private double ArgumentOfPeriapsis;
    private double LongitudeOfAscendingNode;

    private Quaterniond orbitRotation;
    private double Mu;
    private double MeanAngularMotion;

    private static final double twoPI = 2 * Math.PI;

    public OrbitalElements(double semimajoraxis, double eccentricity, long periapsisTime,
                           double inclination, double argumentOfperiapsis, double longitudeOfAscendingNode, double parentBodyMass) {
        this(semimajoraxis, eccentricity, periapsisTime, inclination, argumentOfperiapsis, longitudeOfAscendingNode);
        setOrbitalPeriod(parentBodyMass);
    }

    public OrbitalElements(OrbitalElementsc orbitalElements) {
        set(orbitalElements);
    }

    // called with rotational orbital elements set from fromCartesian function, the negation is kinda wierd maybe need to change at the source
    public void set(OrbitalElementsc orbitalElements) {
        this.SemiMajorAxis = orbitalElements.getSemiMajorAxis();
        this.Eccentricity = orbitalElements.getEccentricity();
        this.periapsisTime = orbitalElements.getPeriapsisTime();

        this.ArgumentOfPeriapsis = orbitalElements.getArgumentOfPeriapsis();
        this.Inclination = orbitalElements.getInclination();
        this.LongitudeOfAscendingNode = orbitalElements.getLongitudeOfAscendingNode();

        this.Mu = orbitalElements.getMu();
        this.MeanAngularMotion = orbitalElements.getMeanAngularMotion();

        this.orbitRotation = new Quaterniond();
        setOrbitRotationFromElements(this.ArgumentOfPeriapsis, this.Inclination, this.LongitudeOfAscendingNode);
    }

    public OrbitalElements(Vector3d pos, Vector3d vel, long TimeElapsed, double parentMass) {
        this.orbitRotation = new Quaterniond();
        Mu = UniversalGravitationalConstant * parentMass;
        fromCartesian(pos, vel, TimeElapsed);
    }

    // Don't use this, this is only used when constructing the planet data pack on the server side when you don't know its parent body that its orbiting.
    public OrbitalElements(double semimajoraxis, double eccentricity,  long periapsisTime,
                           double inclination, double argumentOfperiapsis, double longitudeOfAscendingNode) {
        this.SemiMajorAxis = semimajoraxis;
        this.Eccentricity = eccentricity;
        this.periapsisTime = periapsisTime;

        this.Inclination = inclination;
        this.ArgumentOfPeriapsis = argumentOfperiapsis;
        this.LongitudeOfAscendingNode = longitudeOfAscendingNode;
        this.orbitRotation = new Quaterniond();

        setOrbitRotationFromElements(argumentOfperiapsis, inclination, longitudeOfAscendingNode);
    }

    // Reference: https://space.stackexchange.com/questions/8911/determining-orbital-position-at-a-future-point-in-time
    public double ToCartesian(long timeElapsed, Vector3d outPos, Vector3d outVel) {
        double a = this.SemiMajorAxis;
        double e = this.Eccentricity;
        boolean isElliptical = e < 1;
        double M = this.MeanAngularMotion * (getModulusCurrentTime(timeElapsed, periapsisTime, Eccentricity, MeanAngularMotion));

        //Eccentric anomaly also this works for circular orbits I think
        double anomaly = isElliptical ? KeplerEquationSolver.ellipticalEccentricAnomaly(M, e) : KeplerEquationSolver.hyperbolicEccentricAnomaly(M, e);

        double semiMinorAxis = (isElliptical) ? a * Math.sqrt(1 - (e*e)) : -a * Math.sqrt((e*e) - 1);

        double sinAnomaly =  (isElliptical) ? Math.sin(anomaly) : Math.sinh(anomaly);
        double cosAnomaly =  (isElliptical) ?  org.joml.Math.cosFromSin(sinAnomaly, anomaly) : Math.cosh(anomaly);

        double P = a * (cosAnomaly - e);
        double Q = -semiMinorAxis * sinAnomaly;
        this.perifocalToEquatorial(P, Q, outPos);

        // Velocity Calculation:
        // Determine the square root of the standard gravitational parameter divided by the semi-latus rectum.
        double sqrtSgpOverSlr = Math.sqrt(Mu / (a*(1-e*e)) );

        // atan2 divides q/p to get the true anomaly, but we are using a identity sin of arctan to get our results
        // double prearctanDiv = (Q/P);
        double prearctanDivSquareRoot = Math.sqrt(P*P+Q*Q);

        // reference https://space.stackexchange.com/questions/54596/how-to-calculate-velocity-vector-in-perifocal-coordinates
        // sin of atan2 = y/(y^2+x^2)
        double vP = sqrtSgpOverSlr*(Q/prearctanDivSquareRoot);
        // cos of atan2 = x/(y^2+x^2)
        double vQ = -sqrtSgpOverSlr*(e+P/prearctanDivSquareRoot);

        this.perifocalToEquatorial(vP, vQ, outVel);
        return anomaly;
    }

    public static double getModulusCurrentTime(long timeElapsed, long periapsisTime, double eccentricity, double meanAngularMotion) {
        long diff = timeElapsed - periapsisTime;
        if (eccentricity < 1) {
            long orbitalPeriod = TimeCalc.timeDoubleToLong((2*Math.PI) / meanAngularMotion);
            diff = diff % orbitalPeriod;
        }

        return TimeCalc.timeLongToDouble(diff);
    }

    public Quaterniondc getOrbitRotation() {
        return orbitRotation;
    }

    private void setOrbitRotationFromElements (double argumentOfPeriapsis, double inclination, double longitudeOfAscendingNode) {
        this.orbitRotation = this.orbitRotation.identity().rotateY(longitudeOfAscendingNode).rotateX(inclination).rotateY(argumentOfPeriapsis);
        this.orbitRotation.normalize();
    }

    private void perifocalToEquatorial(double P, double Q, Vector3d vector) {
        vector.set(P, 0d, Q);
        this.orbitRotation.transform(vector);
    }


    /**
     * @param position The Relative position of the orbital body w.r.t to its parent body
     * @param velocity The Relative velocity of the orbital body w.r.t to its parent body
     * @param TimeElapsed The Time in solar system ticks from the start of the world.
     * @see <a href="https://downloads.rene-schwarz.com/download/M002-Cartesian_State_Vectors_to_Keplerian_Orbit_Elements.pdf">Paper</a>
     * @see <a href="https://space.stackexchange.com/questions/65465/orbit-determination-from-position-and-velocityf">Space stack exchange</a>
     */
    public double fromCartesian(Vector3dc position, Vector3dc velocity, long TimeElapsed) {
        double PosMagnitude = position.length();
        double VelMagnitude = velocity.length();

        // incredibly sus to use velocity.negate but i don't know what the problem is...
        Vector3d negatedVelocity = velocity.negate(new Vector3d());
        Vector3d momentumVectorH = new Vector3d(position).cross(negatedVelocity);
        Vector3d eccentricityVector = negatedVelocity.cross(momentumVectorH).div(Mu);
        eccentricityVector.sub(position.x() / PosMagnitude, position.y() / PosMagnitude, position.z() / PosMagnitude);

        // Matrix multiplication of (0, 1, 0)^T cross momentum vector, since y is the up axis in mc. the paper linked has a different formula because it has z as up axis.
        Vector3d pointingAscendingNode = new Vector3d(momentumVectorH.z, 0, -momentumVectorH.x);
        this.Eccentricity = eccentricityVector.length();
        double pointAscendingNodeLength = pointingAscendingNode.length();

        double trueAnomalyAcosVar = eccentricityVector.dot(position)/(Eccentricity*PosMagnitude);

        double trueAnomoly = Math.acos(Mth.clamp(trueAnomalyAcosVar, -1, 1));
        // This is flipped from the paper because of Minecraft's x-z coordinate system where the z is upside down relative to the x
        trueAnomoly = position.dot(velocity) < 0 ? twoPI - trueAnomoly : trueAnomoly;
        // All the acos functions are clamped because of imprecision, even though they're doubles they have values >1 & <-1 in a few cases which gives a NaN value.
        this.Inclination = twoPI - Math.acos(Mth.clamp(-momentumVectorH.y/momentumVectorH.length(), -1, 1));

        this.LongitudeOfAscendingNode = Math.acos(Mth.clamp(pointingAscendingNode.x/pointAscendingNodeLength, -1, 1));
        this.LongitudeOfAscendingNode = pointingAscendingNode.z > 0 ? twoPI - LongitudeOfAscendingNode : LongitudeOfAscendingNode;

        this.ArgumentOfPeriapsis = Math.acos(Mth.clamp(pointingAscendingNode.dot(eccentricityVector)/
                (pointAscendingNodeLength*Eccentricity), -1, 1));

        // For equatorial orbits where longitude of the ascending node and argument of periapsis are undefined.
        if (Double.isNaN(this.LongitudeOfAscendingNode) || Double.isNaN(this.ArgumentOfPeriapsis) || Double.isNaN(this.Inclination)) {
            this.LongitudeOfAscendingNode = 0;
            this.Inclination = 0;
            this.ArgumentOfPeriapsis = Math.atan2(eccentricityVector.z, eccentricityVector.x);
        }

        this.ArgumentOfPeriapsis = eccentricityVector.y > 0 ? twoPI - ArgumentOfPeriapsis : ArgumentOfPeriapsis;

        // need to negate the input rotation so that when transforming the pos,vel in the positionInOrbit method it applies the rotation correctly
        setOrbitRotationFromElements(this.ArgumentOfPeriapsis, this.Inclination, this.LongitudeOfAscendingNode);

        // vis viva equation
        this.SemiMajorAxis = 1 / ((2 / PosMagnitude) - (VelMagnitude * VelMagnitude) / Mu);

        if (Eccentricity < 1) {
            double anomaly = 2 * Math.atan2(Math.tan(trueAnomoly * 0.5d), Math.sqrt((1 + Eccentricity) / (1 - Eccentricity)));

            this.MeanAngularMotion = Math.sqrt(Mu / (SemiMajorAxis * SemiMajorAxis * SemiMajorAxis));
            double timeDiffTerm = (anomaly - Eccentricity * Math.sin(anomaly)) / this.MeanAngularMotion;
            this.periapsisTime = TimeElapsed - TimeCalc.timeDoubleToLong(timeDiffTerm);
            if (this.periapsisTime > TimeElapsed) {
                this.periapsisTime = this.periapsisTime + getOrbitalPeriodLong();
            }
            return anomaly;
        } else {
            double cosTrueAnomoly = Math.cos(trueAnomoly);
            double anomaly = OrbitalCalc.invCosh((Eccentricity + cosTrueAnomoly) / (1 + Eccentricity * cosTrueAnomoly));
            anomaly = (trueAnomoly > Math.PI) ? -anomaly : anomaly;

            this.MeanAngularMotion = Math.sqrt(Mu / -(SemiMajorAxis * SemiMajorAxis * SemiMajorAxis));
            double timeDiffTerm = (Eccentricity * Math.sinh(anomaly) - anomaly) / this.MeanAngularMotion;
            this.periapsisTime = TimeElapsed - TimeCalc.timeDoubleToLong(timeDiffTerm);
            return anomaly;
        }
    }

    private void setOrbitalPeriod(double parentMass) {
        Mu = UniversalGravitationalConstant * parentMass;

        if (SemiMajorAxis >= 0) {
            this.MeanAngularMotion = Math.sqrt(Mu/(SemiMajorAxis * SemiMajorAxis * SemiMajorAxis));
        } else {
            this.MeanAngularMotion = Math.sqrt(Mu/-(SemiMajorAxis * SemiMajorAxis * SemiMajorAxis));
        }
    }

    //Called for the first time on planet load don't use this
    @Override
    public void initCalcs(double parentMass) {
        setOrbitalPeriod(parentMass);
    }

    @Override
    public double getSemiMajorAxis() {
        return SemiMajorAxis;
    }

    @Override
    public double getEccentricity() {
        return Eccentricity;
    }

    @Override
    public long getPeriapsisTime() {
        return periapsisTime;
    }

    @Override
    public double getMeanAngularMotion() {
        return MeanAngularMotion;
    }

    @Override
    public double getInclination() {
        return Inclination;
    }

    @Override
    public double getArgumentOfPeriapsis() {
        return ArgumentOfPeriapsis;
    }

    @Override
    public double getLongitudeOfAscendingNode() {
        return LongitudeOfAscendingNode;
    }

    @Override
    public double getParentMass() {
        return Mu / UniversalGravitationalConstant;
    }

    @Override
    public double getMu() {
        return Mu;
    }

    @Override
    public double getOrbitalPeriod() {
        return (2*Math.PI)/this.MeanAngularMotion;
    }

    @Override
    public long getOrbitalPeriodLong() {
        return TimeCalc.timeDoubleToLong(this.getOrbitalPeriod());
    }

    @Override
    public long getLastPeriapsisTime(long elapsedTime) { // doesn't work properly for elliptical orbits before periapsis
        if (this.isHyperbolic()) {
            return elapsedTime - (elapsedTime - this.periapsisTime);
        } else {
            long orbitalPeriod = TimeCalc.timeDoubleToLong(this.getOrbitalPeriod());
            long lastCalculatedPeriapsisTime = elapsedTime - this.periapsisTime;
            return elapsedTime - (lastCalculatedPeriapsisTime % orbitalPeriod);
        }
    }

    @Override
    public OptionalLong getNextPeriapsisTime(long elapsedTime) {
        if (this.isHyperbolic()) {
            if (elapsedTime > this.periapsisTime) {
                return OptionalLong.empty();
            }
            return OptionalLong.of(elapsedTime - (elapsedTime - this.periapsisTime));
        } else {
            long orbitalPeriod = TimeCalc.timeDoubleToLong(this.getOrbitalPeriod());
            long lastCalculatedPeriapsisTime = elapsedTime - this.periapsisTime;
            long lastPeriapsisTime = elapsedTime - (lastCalculatedPeriapsisTime % orbitalPeriod);
            return OptionalLong.of(lastPeriapsisTime + orbitalPeriod);
        }
    }

    @Override
    public boolean isHyperbolic() {
        return this.Eccentricity >= 1;
    }

    @Override
    public double getApoapsis() {
        if (this.isHyperbolic()) {
            // Hyperbolic orbits have no defined apoapsis but returning positive infinity behaves consistently with the math
            return Double.POSITIVE_INFINITY;
        } else {
            return this.SemiMajorAxis * (1 + Eccentricity);
        }
    }

    @Override
    public double getPeriapsis() {
        return this.SemiMajorAxis * (1 - Eccentricity);
    }

    @Override
    public @Nullable OrbitalCalc.SOIIntercept findOrbitEscapeIntercept(CelestialBody body, long elapsedTime) {
        double soiWithBuffer = body.getSphereOfInfluence() * 1.01d; // bit of an extra buffer so that you don't immediately get sucked back in.

        if (this.getApoapsis() < soiWithBuffer) {
            return null;
        }

        double semiLatusRectum = SemiMajorAxis * (1 - (Eccentricity * Eccentricity));
        double value = (semiLatusRectum - soiWithBuffer) / (Eccentricity * soiWithBuffer);
        double trueAnomaly = Math.acos(value);

        if (Double.isNaN(trueAnomaly)) {
            return null;
        }

        long escapeTime = OrbitalCalc.getTimeStampFromTrueAnomaly(
                MeanAngularMotion,
                trueAnomaly,
                Eccentricity,
                this.getLastPeriapsisTime(elapsedTime)
        );

        return new OrbitalCalc.SOIIntercept(trueAnomaly, escapeTime, body.getParent().
                getOrbitId(), true);
    }
}