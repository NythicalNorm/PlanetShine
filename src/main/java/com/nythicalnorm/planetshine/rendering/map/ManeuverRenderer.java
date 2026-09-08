package com.nythicalnorm.planetshine.rendering.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.rendering.renderTypes.MapRenderable;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.util.ProjectionUtils;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.Collection;
import java.util.List;
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

    public static void renderMouseOverOrbitPoint(
            GuiGraphics graphics, PoseStack poseStack, Matrix4f projectionMatrix,
            int mouseX, int mouseY, Vector3d outRayDir, Vector3d outPosition,
            EntityOrbitBody<?> controllingBody, List<ManeuverManager.PredictedSOIChange> predictedSOIChangesList,
            Map<OrbitId, MapRenderable> mapRenderables
    ) {
        if (controllingBody.getParent() == null || controllingBody.getOrbitalElements() == null) {
            return;
        }
        OrbitalElementsc orbitalElements = controllingBody.getOrbitalElements();
        CelestialBody parentBody = controllingBody.getParent();
        Vector3d closestOrbitPoint;
        Vector3d intersectionOrbitalPlane = ProjectionUtils.intersectLinePlane(
                outPosition, outRayDir, parentBody.getAbsolutePos(), orbitalElements.getOrbitRotation());
        if (intersectionOrbitalPlane == null) {
            return;
        }

        if (orbitalElements.isHyperbolic()) {
            Vector3d targetOnPlane = orbitalElements.getOrbitRotation().transformInverse(intersectionOrbitalPlane.sub(parentBody.getAbsolutePos()));
            Vector2d posOnPlane  = ProjectionUtils.closestPointOnHyperbola(orbitalElements.getSemiMajorAxis(),
                    orbitalElements.getSemiMinorAxis(), new Vector2d(targetOnPlane.x(), targetOnPlane.z()));
            closestOrbitPoint = new Vector3d(posOnPlane.x(), 0.0d, posOnPlane.y());
            orbitalElements.getOrbitRotation().transform(closestOrbitPoint);
        } else {
            double trueAnomaly = getTrueAnomalyFromAbsolutePos(intersectionOrbitalPlane, parentBody, orbitalElements);
            closestOrbitPoint = orbitalElements.getPositionAtAnomaly(trueAnomaly);
        }

        Screen screen = Minecraft.getInstance().screen;
        Matrix4f poseMatrix = new Matrix4f(poseStack.last().pose());
        Vector3f mapPos = mapRenderables.get(parentBody.getOrbitId()).getMapPos().add(MapRenderer.toMapCoordinate(closestOrbitPoint), new Vector3f());

        Vector2i screenPos = ProjectionUtils.worldToScreenCoordinate(mapPos, poseMatrix, projectionMatrix, screen.width, screen.height);
        if (screenPos != null && isHoveringOver(mouseX, mouseY, screenPos)) {
            IconRenderer.drawIcon(graphics, IconRenderer.DEFAULT_PLANET_ICON, screenPos);
        }
    }

    private static boolean isHoveringOver(int mouseX, int mouseY, Vector2i iconPos) {
        int xDiff = Math.abs(mouseX - iconPos.x());
        int yDiff = Math.abs(mouseY - iconPos.y());
        return xDiff < 6 && yDiff < 6;
    }

    private static double getTrueAnomalyFromAbsolutePos(Vector3d intersectionOrbitalPlane, CelestialBody parentBody, OrbitalElementsc orbitalElements) {
        Vector2d periapsisPos = new Vector2d(1.0d, 0.0d);
        Vector3d intersectionVector = new Vector3d(intersectionOrbitalPlane).sub(parentBody.getAbsolutePos());
        orbitalElements.getOrbitRotation().transformInverse(intersectionVector);
        intersectionVector.normalize();

        return -Math.atan2(
                periapsisPos.x * intersectionVector.z - periapsisPos.y * intersectionVector.x,  // cross product (Z component)
                (periapsisPos.x() * intersectionVector.x()) + (periapsisPos.y() * intersectionVector.z()) // dot product
        );
    }
}
