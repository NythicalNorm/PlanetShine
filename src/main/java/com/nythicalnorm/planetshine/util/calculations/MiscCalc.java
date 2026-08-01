package com.nythicalnorm.planetshine.util.calculations;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.Ship;

import java.util.Queue;

public class MiscCalc {
    public static Vector3d pollVectorQueue(Queue<Vector3dc> vectors) {
        Vector3d total = new Vector3d();
        Vector3dc impulse;

        while ((impulse = vectors.poll()) != null) {
            total.add(impulse);
        }

        return total;
    }

    // wraps degrees from - pi to positive pi
    public static double wrapRadians(double angle) {
        double pi = Math.PI;
        while (angle > pi)
            angle -= 2 * pi;
        while (angle < -pi)
            angle += 2 * pi;
        return angle;
    }

    public static double wrapDegrees(double degrees) {
        degrees = (degrees + 180.0) % 360.0;
        if (degrees < 0) {
            degrees += 360.0;
        }
        return degrees - 180.0;
    }

    public static int getShipVolume(@Nullable Ship ship) {
        if (ship == null){
            return 0;
        }
        AABBic aabb = ship.getShipAABB();
        if (aabb == null) {
            return 0;
        }

        return (aabb.maxX() - aabb.minX()) * (aabb.maxY() - aabb.minY()) * (aabb.maxZ() - aabb.minZ());
    }

    public static int getShipMaxLength(@Nullable Ship ship) {
        if (ship == null){
            return 0;
        }
        AABBic aabb = ship.getShipAABB();
        if (aabb == null) {
            return 0;
        }

        int xSpan = aabb.maxX() - aabb.minX();
        int ySpan = aabb.maxY() - aabb.minY();
        int zSpan = aabb.maxZ() - aabb.minZ();

        return Math.max(xSpan, Math.max(ySpan, zSpan));
    }

    public static float[] getRGBAFloats(int val, float alpha) {
        float[] rgbaColor = new float[4];

        int red = (val >> 16) & 0xFF;
        int green = (val >> 8) & 0xFF;
        int blue = (val >> 0) & 0xFF;

        rgbaColor[0] = ((float)red)/255f;
        rgbaColor[1] = ((float)green)/255f;
        rgbaColor[2] = ((float)blue)/255f;
        rgbaColor[3] = alpha;

        return rgbaColor;
    }
}
