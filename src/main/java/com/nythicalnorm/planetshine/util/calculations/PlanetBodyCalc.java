package com.nythicalnorm.planetshine.util.calculations;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetaryBody;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;

public class PlanetBodyCalc {
    public static Vector3d planetDimPosToNormalizedVector(Vec3 pos, double planetRadius, Quaternionfc planetRot, boolean isNormalized) {
        Vector3d quadSpherePos = getNonRotatedDimPosFromNormalizeVector(pos, planetRadius, isNormalized);
        quadSpherePos.rotate(new Quaterniond(planetRot.x(), planetRot.y(), planetRot.z(), planetRot.w()));
        return quadSpherePos;
    }

    public static Vector3d getNonRotatedDimPosFromNormalizeVector(Vec3 pos, double planetRadius, boolean isNormalized) {
        double cellSize = getSquareCellSize(planetRadius);
        double halfCellSize = cellSize*0.5d;

        int xCell = getCellIndex(cellSize, pos.x);
        int zCell = getCellIndex(cellSize, pos.z);

        xCell = Mth.clamp(xCell,-1, 2);

        if (xCell == 0) {
            zCell = Mth.clamp(zCell,-1, 1);
        }
        else {
            zCell = 0;
        }
        double xWithinCell = pos.x - xCell*cellSize;
        double zWithinCell = pos.z - zCell*cellSize;

        xWithinCell = Mth.clamp(xWithinCell, -halfCellSize, halfCellSize);
        zWithinCell = Mth.clamp(zWithinCell, -halfCellSize, halfCellSize);

        int QuadId = xCell + 1;
        if (xCell == 0) {
            if (zCell == 1){
                QuadId = 4;
            }
            else if (zCell == -1) {
                QuadId = 5;
            }
        }
        double radius = 0.5d;
        if (!isNormalized) {
            radius = planetRadius + pos.y; // + 10000000
        }
        return getQuadPlanettoSquarePos(zWithinCell, xWithinCell, halfCellSize, QuadId, radius);
    }

    public static Vector3d planetDimPosToNormalizedVector(double x, double y, double z, CelestialBody celestialBody, boolean isNormalized) {
        return planetDimPosToNormalizedVector(new Vec3(x, y, z), celestialBody.getRadius(), celestialBody.getRotation(), isNormalized);
    }

    public static double getSquareCellSize(double planetRadius) {
        return Math.PI*planetRadius*0.5d;
    }

    public static int getCellIndex(double cellSize, double posAxis) {
        return (int)Math.floor((posAxis + (cellSize/2)) / cellSize);
    }

    public static Vector3d getQuadPlanettoSquarePos(double sidesUpIter, double sidesRightIter, double MaxPerSide, int squareSide, double radius) {
        double sidesrightP = sidesRightIter/MaxPerSide;
        // negative correction because north is negative z in mc
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

    public static Vector3f getQuadSquarePos(float sidesrightP, float sidesupP, int squareSide) {
        Vector3f squarePos = new Vector3f();
        sidesrightP = Math.fma(sidesrightP, 2f, -1f);
        sidesupP = Math.fma(sidesupP, 2f, -1f);

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
        return squarePos;
    }

    public static Vector2d vectorToPlanetDimPos(Vector3dc pos, double planetRadius, Quaternionfc rotation) {
        int squareSide = 0;
        Vector3d position = new Vector3d(pos).normalize();
        position.rotate(new Quaterniond(rotation).invert());
        //reversing the switch case in getSpherePos
        double[] axisVals = new double[]{position.z, position.x, -position.z, -position.x, position.y, -position.y};
        double searchedMax = 0;


        for (int i = 0; i < axisVals.length; i++) {
            if (axisVals[i] > searchedMax) {
                searchedMax = axisVals[i];
                squareSide = i;
            }
        }

        Vector2d squarePos = switch (squareSide) {
            case 0 -> new Vector2d(position.x/searchedMax, -position.y/searchedMax);
            case 1 -> new Vector2d(-position.z/searchedMax, -position.y/searchedMax);
            case 2 -> new Vector2d(-position.x/searchedMax, -position.y/searchedMax);
            case 3 -> new Vector2d(position.z/searchedMax, -position.y/searchedMax);
            case 4 -> new Vector2d(-position.z/searchedMax, position.x/searchedMax);
            case 5 -> new Vector2d(-position.z/searchedMax, -position.x/searchedMax);
            default -> new Vector2d();
        };

        double cellSize = getSquareCellSize(planetRadius);
        squarePos.mul(cellSize * 0.5d);

        Vector2d squareSideCenterPos = switch (squareSide) {
            case 0 -> new Vector2d(-cellSize, 0d);
            case 1 -> new Vector2d(0d, 0d);
            case 2 -> new Vector2d(cellSize, 0d);
            case 3 -> new Vector2d(2 * cellSize, 0d);
            case 4 -> new Vector2d(0d, -cellSize);
            case 5 -> new Vector2d(0d, cellSize);
            default -> new Vector2d();
        };

        return squarePos.add(squareSideCenterPos);
    }

    //client side only
    public static Vector3d getUpVectorForPlanetRot(Vector3d playerRelativePos, CelestialBody planet) {
        Vector3d upDir = new Vector3d(0f,-1f,0f);
        if (planet instanceof PlanetaryBody planetaryBody) {
            AxisAngle4f northPole = planetaryBody.getNorthPoleDir();
            upDir = new Vector3d(northPole.x, northPole.z, northPole.y);
        }
        upDir.normalize();
        Quaterniond rot = new Quaterniond(new AxisAngle4d(Math.PI * 0.5d, 1f, 0f, 0f));

        upDir.rotate(rot);
        return upDir;
    }
}
