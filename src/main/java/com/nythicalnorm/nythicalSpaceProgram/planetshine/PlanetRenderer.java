package com.nythicalnorm.nythicalSpaceProgram.planetshine;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PlanetRenderer {
    private static final VertexBuffer planetvertex = new VertexBuffer(VertexBuffer.Usage.STATIC);
    private static final ResourceLocation Nila_texture =  ResourceLocation.parse("nythicalspaceprogram:textures/planets/moon_axis.png");
    private static final float InWorldPlanetsDistance = 64f;

    public static void setupModels() {
        PoseStack spherePose = new PoseStack();
        spherePose.setIdentity();
        List<BakedQuad> planetquads = SphereModelGenerator.getsphereQuads(); //planetModel.getQuads(null,null, RandomSource.create(), ModelData.builder().build(), RenderType.solid());
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        for(BakedQuad bakedquad : planetquads) {
            bufferbuilder.putBulkData(spherePose.last(), bakedquad, 1f, 1f, 1f, 10, 10);
        }
        planetvertex.bind();
        planetvertex.upload(bufferbuilder.end());
        VertexBuffer.unbind();

    }

    public static void renderPlanet(PoseStack poseStack, Minecraft mc, Camera camera, Matrix4f projectionMatrix) {
        poseStack.pushPose();
        //Vec3 PlanetPos = new Vec3(0,0,0); //CelestialStateSupplier.getPlanetPositon("nila", mc.getPartialTick());
        Vec3 RelativePlanetDir = new Vec3(1,0,0);

        Matrix4f PlanetProjection = PerspectiveShift(projectionMatrix, (float) CelestialStateSupplier.lastUpdatedTimePassedPerSec,
                RelativePlanetDir, poseStack);

        planetvertex.bind();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, Nila_texture);
        ShaderInstance shad = GameRenderer.getPositionTexShader();

        planetvertex.drawWithShader(poseStack.last().pose(), PlanetProjection, shad);
        VertexBuffer.unbind();
        poseStack.popPose();
    }

    private static Matrix4f PerspectiveShift(Matrix4f projectionMatrix, float PlanetDistance, Vec3 relativePlanetDir,
                                             PoseStack poseStack){
        Matrix4f returnMatrix = new Matrix4f(projectionMatrix);
        float PlanetAngularSize = (float) Math.atan(PlanetDistance);
//        float degAdjustedForFOV = (float)((PlanetAngularSize/theta)*2*Math.PI);
//        float newMatrixVal = (float) (1/Math.tan(degAdjustedForFOV/2f));
//        float OrigM00Val = returnMatrix.m00();
//        float newM00Val = (OrigM00Val/returnMatrix.m11())*newMatrixVal;
        float theta = (float) (2f*Math.atan((1/returnMatrix.m11())));
        float m00prefix = returnMatrix.m00()/returnMatrix.m11();
        float newVal = (float) Math.abs(1/Math.tan(PlanetAngularSize/2f));
        returnMatrix.m00(m00prefix*newVal);
        returnMatrix.m11(newVal);

        float ScalediffwithResize = Math.abs(PlanetAngularSize/theta); //(float) Math.abs(Math.tan(theta)/Math.tan(PlanetAngularSize));

        double longitude = Math.atan2(relativePlanetDir.z, relativePlanetDir.x);
        double latitude = Math.acos(relativePlanetDir.y);
        //longitude = longitude;
        //latitude = latitude;

        double x = InWorldPlanetsDistance*Math.sin(latitude)*Math.cos(longitude);
        double y = InWorldPlanetsDistance*Math.cos(latitude);
        double z = InWorldPlanetsDistance*Math.sin(latitude)*Math.sin(longitude);

        poseStack.translate(x, y, z);
        poseStack.scale(ScalediffwithResize,ScalediffwithResize,ScalediffwithResize);

        return returnMatrix;
    }
}
