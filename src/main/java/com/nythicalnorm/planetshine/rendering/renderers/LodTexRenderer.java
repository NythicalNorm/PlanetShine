package com.nythicalnorm.planetshine.rendering.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nythicalnorm.planetshine.rendering.shaders.PSShaders;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.util.calculations.PlanetBodyCalc;
import com.nythicalnorm.planetshine.util.calculations.LodTexCalc;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3d;

import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class LodTexRenderer {
    private static final Matrix4f identityPose = new Matrix4f().identity();

    public static VertexBuffer updateLodTex(CelestialBody currentPlanetIn, Set<Map.Entry<Vector2i, Vector2i>> worldPosAtlasPosEntrySet, VertexBuffer lodTexBuffer) {
        if (lodTexBuffer == null) {
            lodTexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        }

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        for (Map.Entry<Vector2i, Vector2i> worldPosAtlasPos : worldPosAtlasPosEntrySet) {
            int totalSize = LodTexCalc.texInOneAxisCount * LodTexCalc.textureResolution;
            renderQuad(bufferbuilder, currentPlanetIn, worldPosAtlasPos.getKey(), worldPosAtlasPos.getValue(), totalSize);
        }

        lodTexBuffer.bind();
        lodTexBuffer.upload(bufferbuilder.end());
        VertexBuffer.unbind();
        return lodTexBuffer;
    }

    private static void renderQuad(BufferBuilder bufferbuilder, CelestialBody planetIn, Vector2i worldPos, Vector2i atlasCoords, int totalSize) {
        double cellSize = PlanetBodyCalc.getSquareCellSize(planetIn.getRadius());
        double texQuadPlanetSize = (cellSize / LodTexCalc.texQuadsPerCubeCell);
        double halfQuad = (texQuadPlanetSize) * 0.5d;

        double xPos = worldPos.x * texQuadPlanetSize;
        double yPos = worldPos.y * texQuadPlanetSize;

        float xTexOffset = (float) atlasCoords.x / totalSize;
        float yTexOffset = (float) atlasCoords.y / totalSize;
        float uvSize =  (float) LodTexCalc.textureResolution / totalSize;

        createQuad(bufferbuilder, xPos - halfQuad, yPos - halfQuad, xTexOffset, yTexOffset, planetIn);
        createQuad(bufferbuilder, xPos - halfQuad, yPos + halfQuad, xTexOffset, yTexOffset + uvSize, planetIn);
        createQuad(bufferbuilder, xPos + halfQuad, yPos + halfQuad, xTexOffset + uvSize, yTexOffset + uvSize, planetIn);
        createQuad(bufferbuilder, xPos + halfQuad, yPos - halfQuad, xTexOffset + uvSize, yTexOffset, planetIn);
    }

    private static void createQuad(BufferBuilder builder, double xPos, double yPos, float u, float v, CelestialBody celestialBody) {
        Vector3d normalizedPlanetPos = PlanetBodyCalc.planetDimPosToNormalizedVector(xPos, 0d, yPos, celestialBody, true);
        builder.vertex(identityPose, (float) normalizedPlanetPos.x, (float)normalizedPlanetPos.y, (float)normalizedPlanetPos.z)
                .uv(u, v).endVertex();
    }

    public static void renderLODs(PoseStack poseStack, Matrix4f projectionMatrix, int lodTexAtlasID, float opacity, VertexBuffer lodTexBuffer) {
        if (lodTexBuffer != null) {
            RenderSystem.setShaderTexture(0, lodTexAtlasID);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);
            lodTexBuffer.bind();
            lodTexBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, PSShaders.getPlanetShader());
            VertexBuffer.unbind();
        }
    }
}
