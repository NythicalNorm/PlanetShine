package com.nythicalnorm.nythicalSpaceProgram.planetshine.renderers;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.nythicalnorm.nythicalSpaceProgram.NythicalSpaceProgram;
import com.nythicalnorm.nythicalSpaceProgram.planet.PlanetAtmosphere;
import com.nythicalnorm.nythicalSpaceProgram.planetshine.RenderableObjects;
import com.nythicalnorm.nythicalSpaceProgram.planetshine.shaders.ModShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.*;

import java.util.function.Supplier;

public class AtmosphereRenderer {
    private static Supplier<ShaderInstance> skyboxShader;
    private static VertexBuffer skyboxBuffer;
    private static Uniform BottomColor;
    private static Uniform TopColor;
    private static Uniform TransitionPoint;
    private static Uniform Opacity;

    public static void setupShader(VertexBuffer skyBuffer) {
        skyboxShader = ModShaders.getSkyboxShaderInstance();
        skyboxBuffer = skyBuffer;
        if (skyboxShader.get() != null) {
            BottomColor = skyboxShader.get().getUniform("nspBottomColor");
            TopColor = skyboxShader.get().getUniform("nspTopColor");
            TransitionPoint = skyboxShader.get().getUniform("nspTransitionPoint");
            Opacity = skyboxShader.get().getUniform("nspOpacity");
        }
        else {
            NythicalSpaceProgram.logError("Shader not loading");
        }
    }

    public static void render(RenderableObjects renBody, PlanetAtmosphere atmosphere, PoseStack poseStack, Matrix4f projectionMatrix, float pPartialTick) {
        poseStack.pushPose();

        RenderSystem.enableBlend();
        float rainAlpha = 1.0f;
        if (NythicalSpaceProgram.getCelestialStateSupplier().getPlayerData().isOnPlanet()) {
            rainAlpha = 1.0F - Minecraft.getInstance().level.getRainLevel(pPartialTick);
        }
        Vector3f relativeDir = renBody.getNormalizedDiffVectorf();

        poseStack.mulPose(new Quaternionf().rotateTo(new Vector3f(0f,-1f,0f), relativeDir));

        BottomColor.set(atmosphere.getColorTransitionOne());
        TopColor.set(atmosphere.getColorTransitionTwo());
        TransitionPoint.set(0.52777777777f);
        Opacity.set(0.5f*rainAlpha);
//        skyboxBuffer.bind();
//        skyboxBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, skyboxShader.get());
//        VertexBuffer.unbind();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}
