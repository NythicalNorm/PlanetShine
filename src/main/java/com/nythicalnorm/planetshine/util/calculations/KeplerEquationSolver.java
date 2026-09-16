package com.nythicalnorm.planetshine.util.calculations;

public class KeplerEquationSolver {
    public static final int MAX_ITERATIONS_ELLIPTICAL = 50;
    // Hyperbolic orbits take more iterations than elliptical orbits, increase this value if your state vectors are increasing to infinity.
    public static final int MAX_ITERATIONS_HYPERBOLIC = 500;
    public static final double TOLERANCE = 1e-14d;

    // reference: https://academic.oup.com/mnras/article/467/2/1702/2929272
    public static double ellipticalEccentricAnomaly(double x, double e) {
        double eccentricAnomaly;
        double y;

        boolean flipped = false;

        if (x < 0.0d) {
            x = (2 * Math.PI) + x;
        }

        if (x > Math.PI) {
            x = (2 * Math.PI) - x;
            flipped = true;
        }

        if (e < 0.995d) {
            y = x + e * Math.sin(x);//x + e * Math.sin(x) + 0.5d * e * e * Math.sin(2 * x);

            int i = 1;

            while (true) {
                double sinY = Math.sin(y);
                double cosY = org.joml.Math.cosFromSin(sinY, y);

                double Yn = y - e * sinY - x;
                double Ynd = 1 - e * cosY; // Yn dash
                double Yndd = e * sinY;

                double belowTerm = Math.sqrt(Math.abs((Ynd * Ynd) - (2 * Yn * Yndd)));
                if (Ynd < 0) {
                    belowTerm = -belowTerm;
                }

                double Yd = (-2 * Yn) / (Ynd + belowTerm);

                eccentricAnomaly = y + Yd;

                if ((Math.abs(Yd) - TOLERANCE) <= 0.0f) {
                    break;
                }

                if (++i > MAX_ITERATIONS_ELLIPTICAL) {
                    break;
                }
                y = eccentricAnomaly;
            }
        } else {
            // Robust fallback: bracket + bisection
            double lo = 0.0;
            double hi = Math.PI;
            for (int i = 0; i < 60; ++i) {
                double mid = 0.5d * (lo + hi);
                double f = mid - e * Math.sin(mid) - x;
                if (f > 0.0d) {
                    hi = mid;
                } else {
                    lo = mid;
                }
            }
            eccentricAnomaly = 0.5d * (lo + hi);
        }

        if (flipped) {
            eccentricAnomaly = (2 * Math.PI) - eccentricAnomaly;
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

            if ((Math.abs(Yd) - TOLERANCE) <= 0.0f) {
                break;
            }

            if (++i > MAX_ITERATIONS_HYPERBOLIC) {
                break;
            }
            y = eccentricAnomaly;
        }

        return eccentricAnomaly;
    }
}
