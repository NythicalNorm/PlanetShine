package com.nythicalnorm.voxelspaceprogram.solarsystem.orbits;

import com.nythicalnorm.voxelspaceprogram.util.calculations.TimeCalc;
import net.minecraft.util.Mth;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class OrbitalElements {
    public static final int MAX_ITERATIONS_ELLIPTICAL = 100;
    // Hyperbolic orbits take more iterations than elliptical orbits, increase this value if your state vectors are increasing to infinity.
    public static final int MAX_ITERATIONS_HYPERBOLIC = 500;
    public static final double TOLERANCE = 1e-15d;
    public static final double UniversalGravitationalConstant = 6.6743E-11d;

    private double SemiMajorAxis;
    private double Eccentricity;
    private long periapsisTime;

    private double Inclination;
    private double ArgumentOfPeriapsis;
    private double LongitudeOfAscendingNode;

    private double Mu;
    private double MeanAngularMotion;
    private Quaterniond orbitRotation;

    private static final double twoPI = 2 * Math.PI;

    public OrbitalElements(double semimajoraxis, double eccentricity,  long periapsisTime,
                           double inclination, double argumentOfperiapsis, double longitudeOfAscendingNode, double parentBodyMass) {
        this(semimajoraxis, eccentricity, periapsisTime, inclination, argumentOfperiapsis, longitudeOfAscendingNode);
        setOrbitalPeriod(parentBodyMass);
    }

    public OrbitalElements(OrbitalElements orbitalElements) {
        set(orbitalElements);
    }

    // called with rotational orbital elements set from fromCartesian function, the negation is kinda wierd maybe need to change at the source
    public void set(OrbitalElements orbitalElements) {
        this.SemiMajorAxis = orbitalElements.SemiMajorAxis;
        this.Eccentricity = orbitalElements.Eccentricity;
        this.periapsisTime = orbitalElements.periapsisTime;

        this.Inclination = orbitalElements.Inclination;
        this.ArgumentOfPeriapsis = orbitalElements.ArgumentOfPeriapsis;
        this.LongitudeOfAscendingNode = orbitalElements.LongitudeOfAscendingNode;

        this.Mu = orbitalElements.Mu;
        this.MeanAngularMotion = orbitalElements.MeanAngularMotion;
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
        double e0 = Math.log((2.0 * meanAnomaly) / (eccentricity + 1.8));

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

    // Reference: https://space.stackexchange.com/questions/8911/determining-orbital-position-at-a-future-point-in-time
    public Vector3d[] ToCartesian(long timeElapsed) {
        Vector3d[] stateVectors = new Vector3d[2];
        double a = this.SemiMajorAxis;
        double e = this.Eccentricity;
        boolean isElliptical = e < 1;

        double M = this.MeanAngularMotion * (getModulusCurrentTime(timeElapsed));

        //Eccentric anomaly also this works for circular orbits I think
        double Anomaly = isElliptical ? ellipticalEccentricAnomaly(M, e) : hyperbolicEccentricAnomaly(M, e);

        double semiMinorAxis = (isElliptical) ? a * Math.sqrt(1 - (e*e)) : -a * Math.sqrt((e*e) - 1);

        double sinAnomaly =  (isElliptical) ? Math.sin(Anomaly) : Math.sinh(Anomaly);
        double cosAnomaly =  (isElliptical) ?  org.joml.Math.cosFromSin(sinAnomaly, Anomaly) : Math.cosh(Anomaly);

        double P = a * (cosAnomaly - e);
        double Q = -semiMinorAxis * sinAnomaly;
        stateVectors[0] = perifocalToEquatorial(P, Q); //, this.ArgumentOfPeriapsis, this.Inclination, this.LongitudeOfAscendingNode);

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

        stateVectors[1] = perifocalToEquatorial(vP, vQ); //, this.ArgumentOfPeriapsis, this.Inclination, this.LongitudeOfAscendingNode);

        return stateVectors;
    }

    private double getModulusCurrentTime(long timeElapsed) {
        long diff = timeElapsed - this.periapsisTime;
        if (this.Eccentricity < 1) {
            long orbitalPeriod = TimeCalc.timeDoubleToLong((2*Math.PI) / this.MeanAngularMotion);
            diff = diff % orbitalPeriod;
        }

        return TimeCalc.timeLongToDouble(diff);
    }

    public Quaterniond getOrbitRotation() {
        return orbitRotation;
    }

    private void setOrbitRotationFromElements (double argumentOfPeriapsis, double inclination, double longitudeOfAscendingNode) {
        this.orbitRotation = this.orbitRotation.identity().rotateY(longitudeOfAscendingNode).rotateX(inclination).rotateY(argumentOfPeriapsis);
        this.orbitRotation.normalize();
    }

    private Vector3d perifocalToEquatorial(double P, double Q) {
        Vector3d stateVec = new Vector3d(P, 0d, Q);
        return orbitRotation.transform(stateVec);
    }


    /**
     * @param position The Relative position of the orbital body w.r.t to its parent body
     * @param velocity The Relative velocity of the orbital body w.r.t to its parent body
     * @param TimeElapsed The Time in solar system ticks from the start of the world.
     * @see <a href="https://downloads.rene-schwarz.com/download/M002-Cartesian_State_Vectors_to_Keplerian_Orbit_Elements.pdf">Paper</a>
     * @see <a href="https://space.stackexchange.com/questions/65465/orbit-determination-from-position-and-velocityf">Space stack exchange</a>
     */
    public void fromCartesian(Vector3dc position, Vector3dc velocity, long TimeElapsed) {
        double PosMagnitude = position.length();
        double VelMagnitude = velocity.length();

        // incredibly jank to use velocity.negate but i don't know what the problem is...
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
            double E = 2 * Math.atan2(Math.tan(trueAnomoly * 0.5d), Math.sqrt((1 + Eccentricity) / (1 - Eccentricity)));

            this.MeanAngularMotion = Math.sqrt(Mu / (SemiMajorAxis * SemiMajorAxis * SemiMajorAxis));
            double timeDiffTerm = (E - Eccentricity * Math.sin(E)) / this.MeanAngularMotion;
            this.periapsisTime = TimeElapsed - TimeCalc.timeDoubleToLong(timeDiffTerm);
        } else {
            double cosTrueAnomoly = Math.cos(trueAnomoly);
            double H = invCosh((Eccentricity + cosTrueAnomoly) / (1 + Eccentricity * cosTrueAnomoly));

            this.MeanAngularMotion = Math.sqrt(Mu / -(SemiMajorAxis * SemiMajorAxis * SemiMajorAxis));
            double timeDiffTerm = (Eccentricity * Math.sinh(H) - H) / this.MeanAngularMotion;
            this.periapsisTime = TimeElapsed - TimeCalc.timeDoubleToLong(timeDiffTerm);
        }
    }

    private double invCosh(double x) {
        if (x < 1.0) {
            return Double.NaN;
        }
        return Math.log(x + Math.sqrt(x*x - 1));
    }

    //Called for the first time on planet load don't use this
    public void initCalcs(double parentMass) {
        setOrbitalPeriod(parentMass);
    }

    private void setOrbitalPeriod(double parentMass) {
        Mu = UniversalGravitationalConstant * parentMass;

        if (SemiMajorAxis >= 0) {
            this.MeanAngularMotion = Math.sqrt(Mu/(SemiMajorAxis * SemiMajorAxis * SemiMajorAxis));
        } else {
            this.MeanAngularMotion = Math.sqrt(Mu/-(SemiMajorAxis * SemiMajorAxis * SemiMajorAxis));
        }
    }

    public double getSemiMajorAxis() {
        return SemiMajorAxis;
    }

    public double getEccentricity() {
        return Eccentricity;
    }

    public long getPeriapsisTime() {
        return periapsisTime;
    }

    public double getInclination() {
        return Inclination;
    }

    public double getArgumentOfPeriapsis() {
        return ArgumentOfPeriapsis;
    }

    public double getLongitudeOfAscendingNode() {
        return LongitudeOfAscendingNode;
    }

    public double getParentMass() {
        return Mu / UniversalGravitationalConstant;
    }

    public double getOrbitalPeriod() {
        return (2*Math.PI)/this.MeanAngularMotion;
    }
}
