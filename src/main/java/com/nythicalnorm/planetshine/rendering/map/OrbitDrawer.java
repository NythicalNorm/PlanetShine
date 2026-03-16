package com.nythicalnorm.planetshine.rendering.map;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.rendering.shaders.PSShaders;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class OrbitDrawer {
    private static VertexBuffer circleBuffer;
    private static VertexBuffer hyperbolaBuffer;

    private static Uniform startTrueAnomaly;
    private static Uniform distanceToFoci;
    private static Uniform semiMinorAxisMultiplier;
    private static Uniform endTrueAnomaly;

    public static void setupShader() {
        net.minecraft.client.renderer.ShaderInstance orbitShader = PSShaders.getOrbitShader();
        if (orbitShader != null) {
            startTrueAnomaly = orbitShader.getUniform("startTrueAnomaly");
            distanceToFoci = orbitShader.getUniform("distanceToFoci");
            semiMinorAxisMultiplier = orbitShader.getUniform("semiMinorAxisMultiplier");
            endTrueAnomaly = orbitShader.getUniform("endTrueAnomaly");
        }
        else {
            PlanetShine.logError("Shader not loading");
        }
    }
    public static void setupBuffers() {
        generateCircle(2048);
        generateHyperbola(2048);
    }

    public static void generateCircle(int segments) {
        circleBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < segments; i++) {
            float angleAround = (float) i/(segments);
            float angleAroundNext = (i+1f)/(segments);

            angleAround = angleAround * Mth.TWO_PI;
            angleAroundNext = angleAroundNext * Mth.TWO_PI;

            bufferbuilder.vertex(Math.cos(angleAround), 0f, Math.sin(angleAround)).color(1.0f,1.0f,1.0f,1.0f).endVertex();
            bufferbuilder.vertex(Math.cos(angleAroundNext), 0f, Math.sin(angleAroundNext)).color(1.0f,1.0f,1.0f,1.0f).endVertex();
        }

        circleBuffer.bind();
        circleBuffer.upload(bufferbuilder.end());
        VertexBuffer.unbind();
    }

    public static void generateHyperbola(int segments) {
        hyperbolaBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < segments; i++) {
            float angleAround = (float) i/(segments);
            float angleAroundNext = (i+1f)/(segments);

            angleAround = (angleAround * Mth.PI) + Mth.HALF_PI + Mth.PI;
            angleAroundNext = (angleAroundNext * Mth.PI) + Mth.HALF_PI + Mth.PI;

//            angleAround = angleAround * Mth.TWO_PI;
//            angleAroundNext = angleAroundNext * Mth.TWO_PI;

            Vector3f startLine = new Vector3f((float) (1f/Math.cos(angleAround)), 0f,(float) Math.tan(angleAround));
            Vector3f endLine = new Vector3f((float)  (1f/Math.cos(angleAroundNext)), 0f,(float) Math.tan(angleAroundNext));

            if (startLine.isFinite() && endLine.isFinite() && startLine.x > 0 && endLine.x > 0) {
                bufferbuilder.vertex(startLine.x, startLine.y, startLine.z).color(1.0f,1.0f,1.0f,1.0f).endVertex();
                bufferbuilder.vertex(endLine.x, endLine.y, endLine.z).color(1.0f,1.0f,1.0f,1.0f).endVertex();
            }
        }

        hyperbolaBuffer.bind();
        hyperbolaBuffer.upload(bufferbuilder.end());
        VertexBuffer.unbind();
    }

    public static void drawOrbit(OrbitalBody orbitalBody, float scaleFactor, PoseStack poseStack, Matrix4f projectionMatrix) {
        OrbitalElementsc orbitalElements = orbitalBody.getOrbitalElements();
        if (orbitalElements == null) {
            return;
        }
        boolean isElliptical = !orbitalElements.isHyperbolic();

        VertexBuffer drawBuffer = isElliptical ? circleBuffer : hyperbolaBuffer;

        double a = orbitalElements.getSemiMajorAxis();
        double b = isElliptical ? a*Math.sqrt(1-(orbitalElements.getEccentricity() * orbitalElements.getEccentricity()))
                : a*Math.sqrt((orbitalElements.getEccentricity() * orbitalElements.getEccentricity()) - 1);

        a = a*scaleFactor;
        b = b*scaleFactor;
        float distanceFromCenterToFoci =  isElliptical ? (float) -Math.sqrt(a*a - b*b) : (float) Math.sqrt(a*a + b*b);

        if (orbitalBody instanceof EntityOrbitBody entityOrbitBody) {
            setCullingUniforms(orbitalElements, entityOrbitBody.getNextOrbitIntercept(), b / a);
        } else {
            distanceToFoci.set(0.0f);
            startTrueAnomaly.set((float) -Math.PI);
            semiMinorAxisMultiplier.set(1.0f);
        }

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().set(orbitalElements.getOrbitRotation()));

        poseStack.translate(distanceFromCenterToFoci, 0f, 0f);
        poseStack.scale((float) a,1f,(float) b);

        if (orbitalBody instanceof EntityOrbitBody) {
            RenderSystem.setShaderColor(0.0f,0.0f,1.0f,1.0f);
        } else {
            RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);
        }

        drawBuffer.bind();
        drawBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, PSShaders.getOrbitShader());
        VertexBuffer.unbind();
        poseStack.popPose();
    }

    private static void setCullingUniforms(OrbitalElementsc orbitalElements, OrbitalCalc.SOIIntercept intercept, double baRatio) {
        if (intercept == null) {
            distanceToFoci.set(0.0f);
            startTrueAnomaly.set((float) -Math.PI);
            semiMinorAxisMultiplier.set(1.0f);
            endTrueAnomaly.set((float) Math.PI);
            return;
        }

        double trueAnomaly = OrbitalCalc.getTrueAnomalyFromEccentricAnomaly(orbitalElements.getEccentricityAnomaly(),
                orbitalElements.getEccentricity());

        if (orbitalElements.isHyperbolic()) {
            startTrueAnomaly.set((float) trueAnomaly);
            endTrueAnomaly.set((float) intercept.trueAnomaly());
            distanceToFoci.set((float) orbitalElements.getEccentricity());
            semiMinorAxisMultiplier.set((float) baRatio);
        } else {
            startTrueAnomaly.set((float) trueAnomaly);
            endTrueAnomaly.set((float) intercept.trueAnomaly());
            distanceToFoci.set((float) orbitalElements.getEccentricity());
            semiMinorAxisMultiplier.set((float) -baRatio);
        }
    }
}
