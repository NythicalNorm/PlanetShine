package com.nythicalnorm.planetshine.util;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Queue;

public class Calc {
    public static Vector3d pollVectorQueue(Queue<Vector3dc> vectors) {
        Vector3d total = new Vector3d();
        Vector3dc impulse;

        while ((impulse = vectors.poll()) != null) {
            total.add(impulse);
        }

        return total;
    }

    // wraps degrees from - pi to positive pi
    public static double wrapDegrees(double angle) {
        double pi = Math.PI;
        while (angle > pi)
            angle -= 2 * pi;
        while (angle < -pi)
            angle += 2 * pi;
        return angle;
    }
}
