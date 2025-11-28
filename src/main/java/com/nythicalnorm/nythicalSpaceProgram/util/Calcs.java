package com.nythicalnorm.nythicalSpaceProgram.util;

import com.nythicalnorm.nythicalSpaceProgram.NythicalSpaceProgram;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;

public class Calcs {
    public static final float PI = 3.14159265359f;
    public static final float hPI = 1.57079632679f;

    public static Vector3d planetDimPosToNormalizedVector(Vec3 pos, double planetRadius, Quaternionf planetRot, boolean isNormalized) {
        double cellSize = 100; //Math.PI*planetRadius*0.5d;
        double halfCellSize = cellSize*0.5d;

        int xCell = (int)Math.floor((pos.x + halfCellSize) / cellSize);
        int zCell = (int)Math.floor((pos.z + halfCellSize) / cellSize);

        xCell = clamp(-1, 2, xCell);

        if (xCell == 0) {
            zCell = clamp(-1, 1, zCell);
        }
        else {
            zCell = 0;
        }
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
        double radius = 1d;
        if (!isNormalized) {
            radius = planetRadius + 10000000 + pos.y;
        }
        Vector3d quadSpherePos =  getQuadPlanettoSquarePos(zWithinCell, xWithinCell, halfCellSize, QuadId, radius);

        quadSpherePos.rotate(new Quaterniond(planetRot.x, planetRot.y,planetRot.z, planetRot.w));
        return quadSpherePos;
    }

    public static Vector3d getQuadPlanettoSquarePos(double sidesUpIter, double sidesRightIter, double MaxPerSide, int squareSide, double radius) {
        double sidesrightP = sidesRightIter/MaxPerSide;
        //negative correction because north is negative z in mc
        double sidesupP = -sidesUpIter/MaxPerSide;
        Vector3d squarePos = new Vector3d();
//        sidesupP = (sidesupP - 0.5f)*2f;
//        sidesrightP = (sidesrightP - 0.5f)*2f;

        squarePos = switch (squareSide) {
            case 0 -> new Vector3d(sidesrightP, sidesupP, 1f);
            case 1 -> new Vector3d(1f, sidesupP, -sidesrightP);
            case 2 -> new Vector3d(-sidesrightP, sidesupP, -1);
            case 3 -> new Vector3d(-1f, sidesupP, sidesrightP);
            case 5 -> new Vector3d(-sidesupP, 1f, -sidesrightP);
            case 4 -> new Vector3d(sidesupP, -1f, -sidesrightP);
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

    public static Vector3f getUpVectorForPlanetRot(Vector3f playerRelativePos) {
        Vector3f upDir = new Vector3f(0f,-1f,0f);
        if (NythicalSpaceProgram.getCelestialStateSupplier().isOnPlanet()) {
            AxisAngle4f northPole = NythicalSpaceProgram.getCelestialStateSupplier().getCurrentPlanet().getNorthPoleDir();
            upDir = new Vector3f(northPole.x, northPole.z, northPole.y);
            upDir.normalize();
        }
        Quaternionf rot = new Quaternionf(new AxisAngle4f(hPI, 1f, 0f, 0f));
        upDir.rotate(rot);
        return upDir;
    }

    public static boolean IsNaN(Quaternionf q) {
        return Double.isNaN(q.w()) ||
                Double.isNaN(q.x()) ||
                Double.isNaN(q.y()) ||
                Double.isNaN(q.z());
    }
}
