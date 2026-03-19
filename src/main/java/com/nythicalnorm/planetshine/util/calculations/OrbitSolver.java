package com.nythicalnorm.planetshine.util.calculations;

public class OrbitSolver {
    // reference: https://academic.oup.com/mnras/article/467/2/1702/2929272
    public static double ellipticalEccentricAnomaly(double x, double e) {
        double eccentricAnomaly;
        double y;

        y = x + e * Math.sin(x);

        int i = 1;

        while (true) {
            double sinY = Math.sin(y);
            double cosY = org.joml.Math.cosFromSin(sinY, y);

            double Yn = y - e * sinY - x;
            double Ynd = 1 - e * cosY; // Yn dash
            double Yndd = e * sinY;

            double belowTerm = Math.sqrt(Math.abs((Ynd*Ynd) - (2 * Yn * Yndd)));
            if (Ynd < 0) {
                belowTerm = - belowTerm;
            }

            double Yd = (-2*Yn) / (Ynd + belowTerm);

            eccentricAnomaly = y + Yd;

            if ((Math.abs(Yd) - OrbitalCalc.TOLERANCE) <= 0.0f) {
                break;
            }

            if (++i > OrbitalCalc.MAX_ITERATIONS_ELLIPTICAL) {
                break;
            }
            y = eccentricAnomaly;
        }

        return eccentricAnomaly % (2 * Math.PI);
    }

    public static double hyperbolicEccentricAnomaly(double x, double e) {
        double eccentricAnomaly;

        // I don't get this equation, but it cuts the no. of iterations from over 700 to 4 in a few cases.
        // Reference: https://arxiv.org/html/2411.15374v1#S4.F2
        double y = Math.log((2.0 * Math.abs(x)) / (e + 1.8));

        int i = 1;

        while (true) {
            double sinY = Math.sinh(y);
            double cosY = Math.cosh(y);

            double Yn = e * sinY - y - x;
            double Ynd = e * cosY - 1.0d; // Yn dash
            double Yndd = e * sinY; // Yn double dash

            double belowTerm = Math.sqrt(Math.abs((Ynd*Ynd) - (2 * Yn * Yndd)));
            if (Ynd < 0) {
                belowTerm = - belowTerm;
            }

            double Yd = (-2*Yn) / (Ynd + belowTerm);

            eccentricAnomaly = y + Yd;

            if ((Math.abs(Yd) - OrbitalCalc.TOLERANCE) <= 0.0f) {
                break;
            }

            if (++i > OrbitalCalc.MAX_ITERATIONS_HYPERBOLIC) {
                break;
            }
            y = eccentricAnomaly;
        }

        return eccentricAnomaly;
    }
}
