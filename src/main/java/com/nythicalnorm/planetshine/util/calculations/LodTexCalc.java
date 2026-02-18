package com.nythicalnorm.planetshine.util.calculations;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

public class LodTexCalc {
    public static final int texQuadsPerCubeCell = 1024;
    public static final int textureResolution = 256;
    public static final int texInOneAxisCount = 16;
    public static final int textureSizeJumpExponent = 32;

    public static Vector2i textureIndexTo2d(int texIndex) {
        int x = texIndex % texQuadsPerCubeCell;
        int z = texIndex / texQuadsPerCubeCell;
        return new Vector2i(x, z);
    }

    public static Vector2i getPlanetTexCoordinates(Vec3 plrPos, CelestialBody celestialBody) {
        return getPlanetTexCoordinates(plrPos, getTexturePixelSize(celestialBody));
    }

    public static Vector2i getPlanetTexCoordinates(Vec3 plrPos, int texturePixelSize) {
        int xIndex = PlanetBodyCalc.getCellIndex(texturePixelSize, plrPos.x);
        int zIndex = PlanetBodyCalc.getCellIndex(texturePixelSize, plrPos.z);
        return new Vector2i(xIndex, zIndex);
    }

    public static int getTexturePixelSize(CelestialBody planet) {
        double cellSize = PlanetBodyCalc.getSquareCellSize(planet.getRadius());
        return (int) Math.ceil(cellSize / texQuadsPerCubeCell);
    }
}
