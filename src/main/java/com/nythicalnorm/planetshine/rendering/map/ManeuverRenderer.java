package com.nythicalnorm.planetshine.rendering.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.rendering.renderTypes.MapRenderable;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

import java.util.Collection;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ManeuverRenderer {
    public static void renderAllSOIChanges
            (PoseStack poseStack,
             Matrix4f projectionMatrix,
             Collection<ManeuverManager.PredictedSOIChange> allIntercepts,
             Map<OrbitId, MapRenderable> renderableMap) {
        RenderSystem.enableBlend();

        for (ManeuverManager.PredictedSOIChange intercept : allIntercepts) {
            MapRenderable mapRenderable = renderableMap.get(intercept.parentPlanet());
            renderManeuver(poseStack, projectionMatrix, mapRenderable, intercept.newOrbit(), intercept.startingAnomaly(),
                    intercept.nextIntercept());
        }

        RenderSystem.disableBlend();
    }

    private static void renderManeuver(
            PoseStack poseStack,
            Matrix4f projectionMatrix,
            MapRenderable mapRenderable,
            OrbitalElementsc newOrbit,
            double startingAnomaly,
            @Nullable OrbitalCalc.SOIIntercept nextIntercept
    ) {
        poseStack.pushPose();
        Vector3fc bodyPos = mapRenderable.getMapPos();
        poseStack.translate(bodyPos.x(), bodyPos.y(), bodyPos.z());
        OrbitDrawer.drawFutureOrbit(newOrbit, startingAnomaly, nextIntercept, poseStack, projectionMatrix);
        poseStack.popPose();
    }
}
