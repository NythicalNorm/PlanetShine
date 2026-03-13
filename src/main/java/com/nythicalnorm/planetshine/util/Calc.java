package com.nythicalnorm.planetshine.util;

import org.joml.Quaterniond;
import org.joml.Quaternionf;
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

    public static Quaterniond mcRotationToQuaterniond(float yRot, float xRot) {
        return new Quaterniond()
                .rotateY(Math.toRadians(yRot))
                .rotateX(Math.toRadians(xRot));
    }

    public static Quaternionf mcRotationToQuaternionf(float yRot, float xRot) {
        return new Quaternionf()
                .rotateY((float) Math.toRadians(yRot))
                .rotateX((float) Math.toRadians(xRot));
    }
}
