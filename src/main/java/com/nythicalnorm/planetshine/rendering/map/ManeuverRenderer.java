package com.nythicalnorm.planetshine.rendering.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.rendering.renderTypes.MapRenderable;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

import java.util.Map;

public class ManeuverRenderer {
    private static final int ORBIT_PREDICTION_DEPTH = 3;

    public static void renderFromBody(@Nullable EntityOrbitBody controllingBody, Map<OrbitId, MapRenderable> renderableMap,
                                      PoseStack poseStack, Matrix4f projectionMatrix) {
        if (controllingBody == null || controllingBody.getNextOrbitIntercept() == null || controllingBody.getOrbitalElements() == null) {
            return;
        }
        RenderSystem.enableBlend();
        OrbitalCalc.SOIIntercept intercept = controllingBody.getNextOrbitIntercept();
        OrbitalElements newOrbit = new OrbitalElements(controllingBody.getOrbitalElements());
        CelestialBody lastParent = controllingBody.getParent();

        for (int i = 0; i < ORBIT_PREDICTION_DEPTH; i ++) {
            if (intercept == null) {
                break;
            }
            lastParent = OrbitalCalc.calculateSOIChange(intercept, lastParent, newOrbit, newOrbit);
            MapRenderable mapRenderable = renderableMap.get(intercept.interceptingBody());

            double startingAnomaly = OrbitalCalc.getTrueAnomalyAtTime(newOrbit, intercept.timeElapsed());

            // now calculating the next next orbit after soiChange
            intercept = OrbitalCalc.calculateIntercepts(newOrbit, startingAnomaly, lastParent, intercept.timeElapsed());

            poseStack.pushPose();
            if (mapRenderable != null && lastParent != null) {
                Vector3fc bodyPos = mapRenderable.getMapPos();
                poseStack.translate(bodyPos.x(), bodyPos.y(), bodyPos.z());
                OrbitDrawer.drawFutureOrbit(newOrbit, startingAnomaly, intercept, poseStack, projectionMatrix);
            }
            poseStack.popPose();
        }
        RenderSystem.disableBlend();
    }
}
