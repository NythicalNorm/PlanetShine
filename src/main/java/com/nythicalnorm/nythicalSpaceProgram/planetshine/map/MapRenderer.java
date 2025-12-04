package com.nythicalnorm.nythicalSpaceProgram.planetshine.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nythicalnorm.nythicalSpaceProgram.NythicalSpaceProgram;
import com.nythicalnorm.nythicalSpaceProgram.planetshine.PlanetShine;
import com.nythicalnorm.nythicalSpaceProgram.planetshine.renderers.RenderableObjects;
import com.nythicalnorm.nythicalSpaceProgram.planetshine.renderers.AtmosphereRenderer;
import com.nythicalnorm.nythicalSpaceProgram.planetshine.renderers.PlanetRenderer;
import com.nythicalnorm.nythicalSpaceProgram.planetshine.renderers.SpaceObjRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.*;

import java.util.Optional;

public class MapRenderer {
    Quaternionf currentRotation;
    private static final float SCALE_FACTOR = 1/1000000000f;
    private static VertexBuffer playerHeadBillBoard;

    public static void initModel() {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        playerHeadBillBoard = new VertexBuffer(VertexBuffer.Usage.STATIC);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        float size = 0.5f;
        bufferbuilder.vertex(-size, -size, 0).uv(0.125F, 0.125F).endVertex();
        bufferbuilder.vertex(+size, -size, 0).uv(0.25F, 0.125F).endVertex();
        bufferbuilder.vertex(+size, size, 0).uv(0.25F, 0.25F).endVertex();
        bufferbuilder.vertex(-size, size, 0).uv(0.125F, 0.25F).endVertex();
        playerHeadBillBoard.bind();
        playerHeadBillBoard.upload(bufferbuilder.end());
        VertexBuffer.unbind();
    }

    public static void renderSkybox(PoseStack mapPosestack, Matrix4f projectionMatrix) {
        AtmosphereRenderer.renderSpaceSky(mapPosestack, projectionMatrix);
        PlanetShine.drawStarBuffer(mapPosestack, projectionMatrix, 1.0f);
    }

    public static void renderMapObjects(PoseStack poseStack, Matrix4f projectionMatrix, Vector3d cameraPos) {
        RenderableObjects[] renderableObjects = SpaceObjRenderer.getRenderPlanets();
        Vector3f mapCameraPos = toMapCoordinate(cameraPos);
        poseStack.translate(-mapCameraPos.x, -mapCameraPos.y, -mapCameraPos.z);

        for (RenderableObjects plnt : renderableObjects) {
            poseStack.pushPose();
            Vector3f planetMapPos = toMapCoordinate(plnt.getBody().getAbsolutePos());
            poseStack.translate(planetMapPos.x, planetMapPos.y, planetMapPos.z);

            float PlanetSize = (float) (SCALE_FACTOR*plnt.getBody().getRadius());
            poseStack.scale(PlanetSize, PlanetSize, PlanetSize);
            poseStack.mulPose(plnt.getBody().getRotation());

            PlanetRenderer.render(plnt,false, Optional.empty(), poseStack, projectionMatrix, 0, 1.0f);
            NythicalSpaceProgram.getCelestialStateSupplier().get().getCurrentPlanet().ifPresent(currentplanet -> {
                if (currentplanet.equals(plnt.getBody()))
                {
//                    renderPlayerHeadOnPlanet(NythicalSpaceProgram.getCelestialStateSupplier().get().getPlayerData().getRelativePos(),
//                            poseStack, projectionMatrix);
                }
            });

            poseStack.popPose();
        }

    }

    private static void renderPlayerHeadOnPlanet(Vector3d PlayerRelativePos, PoseStack poseStack, Matrix4f projectionMatrix) {
        poseStack.pushPose();
        RenderSystem.disableDepthTest();
        Vector3f mapPos = toMapCoordinate(PlayerRelativePos);
        poseStack.translate(mapPos.x, mapPos.y, mapPos.z);
        poseStack.scale(1f, 1f, 1f);
        ResourceLocation playerTexture = Minecraft.getInstance().player.getSkinTextureLocation();
        RenderSystem.setShaderTexture(0, playerTexture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        playerHeadBillBoard.bind();
        playerHeadBillBoard.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionTexShader());
        VertexBuffer.unbind();
        RenderSystem.enableDepthTest();
        poseStack.popPose();
    }


    public static Vector3f toMapCoordinate(Vector3d position) {
        position.mul(SCALE_FACTOR);
        Vector3f mapCoord = new Vector3f((float) position.x, (float) position.y, (float) position.z);
        return mapCoord;
    }


//    private void renderPlayerHead(GuiGraphics graphics, int posX, int posY, float textureSize) {
//        ResourceLocation playerTexture = Minecraft.getInstance().player.getSkinTextureLocation();
//        float relativeHeadSize = textureSize/8;
//
//        graphics.blit(playerTexture, (int) (posX - relativeHeadSize*0.5f), (int) (posY - relativeHeadSize*0.5f), (int) relativeHeadSize,
//                (int) relativeHeadSize,(int) relativeHeadSize, (int) relativeHeadSize,  (int) textureSize, (int) textureSize);
//    }
}
