package com.nythicalnorm.planetshine.util;

import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
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
}
