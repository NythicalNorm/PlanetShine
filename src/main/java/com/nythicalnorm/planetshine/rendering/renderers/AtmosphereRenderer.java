package com.nythicalnorm.planetshine.rendering.renderers;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.rendering.PSRenderer;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetAtmosphere;
import com.nythicalnorm.planetshine.rendering.shaders.PSShaders;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

import java.lang.Math;

@OnlyIn(Dist.CLIENT)
public class AtmosphereRenderer {
    private static ShaderInstance skyboxShader;
    private static VertexBuffer skyboxBuffer;
    private static Uniform OverlayColor;
    private static Uniform AtmoColor;
    private static Uniform OverlayAngle;
    private static Uniform AtmoAngle;

    public static void setupShader() {
        skyboxShader = PSShaders.getSkyboxShader();
        skyboxBuffer = PSRenderer.getSkyboxBuffer();
        if (skyboxShader != null) {
            OverlayColor = skyboxShader.getUniform("nspOverlayColor");
            AtmoColor = skyboxShader.getUniform("nspAtmoColor");
            OverlayAngle = skyboxShader.getUniform("nspOverlayAngle");
            AtmoAngle = skyboxShader.getUniform("nspAtmoAngle");
        }
        else {
            PlanetShine.logError("Shader not loading");
        }
    }

    public static void render(CelestialBody renBody, Vector3f relativeDir, double distance, PlanetAtmosphere atmosphere, PoseStack poseStack, Matrix4f projectionMatrix) {
        poseStack.pushPose();
        RenderSystem.enableBlend();
        float scale = SpaceObjRenderer.maxDepthDistance();
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(new Quaternionf().rotateTo(new Vector3f(0f,1f,0f), relativeDir));

        //reduce the atmosphere alpha as the player gets further away, only works if the atmosphere's alpha value is less than 1
        //float distDiffAtmo =  1f - (float)((distance - renBody.getRadius())/atmosphere.getAtmosphereHeight());
        float colorAlpha = Mth.clamp(atmosphere.getAtmosphereAlpha(),0f, 1f);// Mth.clamp(distDiffAtmo,0f,1f), 1f);

        float[] overlayColor = atmosphere.getSurfaceColor(colorAlpha);
        float[] atmosphereColor = atmosphere.getAtmoColor();

        float planetAnglularSize = cosOfasin(renBody.getRadius()/distance);
        float atmoAnglularSize = cosOfasin(renBody.getAtmosphereRadius()/distance);

        OverlayColor.set(overlayColor);
        AtmoColor.set(atmosphereColor);
        OverlayAngle.set(planetAnglularSize);
        AtmoAngle.set(atmoAnglularSize);

        skyboxBuffer.bind();
        skyboxBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, skyboxShader);
        VertexBuffer.unbind();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private static float cosOfasin(double x) {
        if (x > 1) {
            x = x-2;
            return (float) -Math.sqrt(1-x*x);
        }

        return (float)  Math.sqrt(1-x*x);
    }

//    public static void renderAtmospheres(SpaceRenderable[] renBody, PoseStack poseStack, Matrix4f projectionMatrix, Optional<PlanetAtmosphere> atmosphere) {
//        for (SpaceRenderable ren : renBody) {
//            if (ren instanceof RenderablePlanet renPlanet) {
//                if (renPlanet.getBody().getAtmosphere().hasAtmosphere()) {
//                    render(renPlanet.getBody(), renPlanet.getNormalizedDiffVectorf(), renPlanet.getDistance(), renPlanet.getBody().getAtmosphere(), poseStack, projectionMatrix);
//                }
//            }
//        }
//    }
}
