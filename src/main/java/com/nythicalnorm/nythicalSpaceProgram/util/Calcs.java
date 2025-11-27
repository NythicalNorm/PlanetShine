package com.nythicalnorm.nythicalSpaceProgram.util;

import com.nythicalnorm.nythicalSpaceProgram.planet.PlanetaryBody;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Calcs {
    public static Vector3d planetDimPosToNormalizedVector(Vec3 pos, double planetRadius, boolean isNormalized) {
        double cellSize = 100; //Math.PI*planetRadius*0.5d;
        double halfCellSize = cellSize*0.5d;

        int xCell = (int)Math.floor((pos.x + halfCellSize) / cellSize);
        int zCell = (int)Math.floor((pos.z + halfCellSize) / cellSize);

        xCell = clamp(-1, 2, xCell);
        zCell = clamp(-1, 1, zCell);

        double xWithinCell = pos.x - xCell*cellSize;
        double zWithinCell = pos.z - zCell*cellSize;

        xWithinCell = clamp(-halfCellSize, halfCellSize, xWithinCell);
        zWithinCell = clamp(-halfCellSize, halfCellSize, zWithinCell);

        int QuadId = xCell + 1;
        if (xCell == 0) {
            if (zCell == 1){
                QuadId = 4;
            }
            else if (zCell == -1) {
                QuadId = 5;
            }
        }
        double radius = 1;
        if (!isNormalized) {
            radius = planetRadius + 10000000 + pos.y;
        }

        return getQuadPlanettoSquarePos(zWithinCell, xWithinCell, halfCellSize, QuadId, radius);
    }

    public static Vector3d getQuadPlanettoSquarePos(double sidesUpIter, double sidesRightIter, double MaxPerSide, int squareSide, double radius) {
        double sidesrightP = (sidesRightIter)/MaxPerSide;
        //negative correction because the order to build it was based on quad model and texture requirements
        double sidesupP = (sidesUpIter)/MaxPerSide;
        Vector3d squarePos = new Vector3d();
//        sidesupP = (sidesupP - 0.5f)*2f;
//        sidesrightP = (sidesrightP - 0.5f)*2f;

        squarePos = switch (squareSide) {
            case 0 -> new Vector3d(sidesrightP, sidesupP, 1f);
            case 1 -> new Vector3d(1f, sidesupP, -sidesrightP);
            case 2 -> new Vector3d(-sidesrightP, sidesupP, -1);
            case 3 -> new Vector3d(-1f, sidesupP, sidesrightP);
            case 4 -> new Vector3d(-sidesupP, 1f, -sidesrightP);
            case 5 -> new Vector3d(sidesupP, -1f, -sidesrightP);
            default -> squarePos;
        };

        squarePos.normalize();
        squarePos.mul(radius);
        return squarePos;
    }

    public static Vector3f getQuadSquarePos(float sidesUpIter, float sidesRightIter, float MaxPerSide, int squareSide, float radius) {
        float sidesrightP = sidesRightIter/MaxPerSide;
        float sidesupP = sidesUpIter/MaxPerSide;
        Vector3f squarePos = new Vector3f();

        squarePos = switch (squareSide) {
            case 0 -> new Vector3f(sidesrightP, sidesupP, 1f);
            case 1 -> new Vector3f(1f, sidesupP, -sidesrightP);
            case 2 -> new Vector3f(-sidesrightP, sidesupP, -1);
            case 3 -> new Vector3f(-1f, sidesupP, sidesrightP);
            case 4 -> new Vector3f(-sidesrightP, 1f, sidesupP);
            case 5 -> new Vector3f(sidesrightP, -1f, sidesupP);
            default -> squarePos;
        };
        squarePos.normalize();
        squarePos.mul(radius);
        return squarePos;
    }

    private static int clamp (int min, int max, int val) {
        if (val > max) {
            return max;
        }
        else return Math.max(val, min);
    }

    private static double clamp (double min, double max, double val) {
        if (val > max) {
            return max;
        }
        else return Math.max(val, min);
    }
}
