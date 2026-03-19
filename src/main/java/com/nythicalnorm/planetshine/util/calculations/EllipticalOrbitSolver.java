package com.nythicalnorm.planetshine.util.calculations;

public class EllipticalOrbitSolver {
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
}
