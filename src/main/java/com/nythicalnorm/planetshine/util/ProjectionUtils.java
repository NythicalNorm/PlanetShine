package com.nythicalnorm.planetshine.util;

import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;

@OnlyIn(Dist.CLIENT)
public class ProjectionUtils {
    public static @Nullable Vector2i worldToScreenCoordinate(Vector3f pos, Matrix4f poseStack,
                                                             Matrix4f projectionMatrix, int width, int height) {
        Matrix4f clip_Pos = new Matrix4f(projectionMatrix).mul(poseStack);
        Vector4f clipVec = new Vector4f(pos.x, pos.y, pos.z, 1f).mul(clip_Pos);
        float x = clipVec.x/ clipVec.w;
        float y = -clipVec.y/ clipVec.w;

        int pixelX = Math.round((x+1)*0.5f*width);
        int pixelY = (int) Math.floor((y+1)*0.5f*height);
        if (clipVec.z > 0f) {
            return new Vector2i(pixelX, pixelY);
        } else {
            return null;
        }
    }

    public static Vector3d screenToWorldRay (
            float mouseX, float mouseY,
            int width, int height,
            Matrix4f projection,
            Matrix4f view
    ) {
        // NDC
        float x = (2.0f * mouseX) / width - 1.0f;
        float y = 1.0f - (2.0f * mouseY) / height;

        Vector4f rayClip = new Vector4f(x, y, -1.0f, 1.0f);

        // Eye space
        Matrix4f invProj = new Matrix4f(projection).invert();
        Vector4f rayEye = invProj.transform(rayClip);
        rayEye.z = -1.0f;
        rayEye.w = 0.0f;

        // World space
        Matrix4f invView = new Matrix4f(view).invert();
        Vector4f rayWorld = invView.transform(rayEye);

        return new Vector3d(rayWorld.x, rayWorld.y, rayWorld.z).normalize();
    }

    public static CelestialBody raycastPlanets(Vector3d cameraPos, Vector3d rayDir, SolarSystem solarSystem) {
        for (CelestialBody celestialBody : solarSystem.getAllPlanetaryBodies().values()) {
            if (intersectRaySphere(cameraPos, rayDir, celestialBody.getAbsolutePos(), celestialBody.getRadius())) {
                return celestialBody;
            }
        }

        return null;
    }

    public static boolean intersectRaySphere(
            Vector3d rayOrigin,
            Vector3d rayDir,      // MUST be normalized
            Vector3dc sphereCenter,
            double radius
    ) {
        Vector3d oc = new Vector3d(rayOrigin).sub(sphereCenter);

        double a = rayDir.dot(rayDir); // usually 1
        double b = 2.0f * rayDir.dot(oc);
        double c = oc.dot(oc) - radius * radius;

        double discriminant = b * b - 4 * a * c;

        return discriminant >= 0;
    }

    public static Vector3d intersectLinePlane (
            Vector3dc lineStart,
            Vector3dc lineDir,
            Vector3dc planePos,
            Quaterniondc planeRot
    ) {
        // Plane normal = plane's local +Y direction rotated into world space
        Vector3d planeNormal = new Vector3d(0, 1, 0).rotate(planeRot);

        // denominator = lineDir · planeNormal
        double denom = lineDir.dot(planeNormal);

        // Line is parallel to plane
        if (Math.abs(denom) < 1e-6f) {
            return null;
        }

        // t = (planePos - lineStart) · planeNormal
        //     ------------------------------------
        //          lineDir · planeNormal
        double t = new Vector3d(planePos)
                .sub(lineStart)
                .dot(planeNormal) / denom;

        // Intersection point = lineStart + lineDir * t
        return new Vector3d(lineDir)
                .mul(t)
                .add(lineStart);
    }

    public static Vector2d closestPointOnHyperbola(
            double a,
            double b,
            Vector2d target
    ) {
        a = Math.abs(a);
        b = Math.abs(b);
        // Try both branches and return the closest result.
        Vector2d right = closestOnBranch(a, b, target, false);
        Vector2d left  = closestOnBranch(a, b, target, true);

        double rightDist = right.distanceSquared(target);
        double leftDist  = left.distanceSquared(target);

        return rightDist < leftDist ? right : left;
    }

    private static Vector2d closestOnBranch(
            double a,
            double b,
            Vector2d target,
            boolean leftBranch
    ) {
        /*
         * Initial guess.
         *
         * A reasonable starting point is based on the direction
         * from the hyperbola's center toward the target.
         */
        double t;

        double xSign = leftBranch ? -1.0f : 1.0f;

        double normalizedY = target.y / b;

        t = OrbitalCalc.aSinh(
                Math.max(-1.0, Math.min(1.0, normalizedY))
        );

        // Newton-Raphson
        for (int i = 0; i < 60; i++) {
            double sinh = Math.sinh(t);
            double cosh = Math.cosh(t);

            double x = xSign * a * cosh;
            double y = b * sinh;

            double dx = xSign * a * sinh;
            double dy = b * cosh;

            double ddx = xSign * a * cosh;
            double ddy = b * sinh;

            // f(t) = (P(t) - target) · P'(t)
            double f =
                    (x - target.x) * dx +
                            (y - target.y) * dy;

            // f'(t)
            double df =
                    dx * dx +
                            (x - target.x) * ddx +
                            dy * dy +
                            (y - target.y) * ddy;

            if (Math.abs(df) < 1e-14f)
                break;

            double delta = f / df;

            t -= delta;

            if (Math.abs(delta) < 1e-14f)
                break;
        }

        double cosh = Math.cosh(t);
        double sinh = Math.sinh(t);

        return new Vector2d(
                xSign * a * cosh,
                b * sinh
        );
    }
}
