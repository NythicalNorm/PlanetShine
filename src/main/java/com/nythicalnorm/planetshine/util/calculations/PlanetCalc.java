package com.nythicalnorm.planetshine.util.calculations;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;

import java.lang.Math;

public class PlanetCalc {

    public static Vector3d getPlanetRelativePosition(Vector3dc pos, CelestialBody celestialBody) {
        return PlanetCalc.getPlanetRotatedPosition(pos.x(), pos.y(), pos.z(), celestialBody.getRadius(), celestialBody.getRotation(), false);
    }

    public static Vector3d getPlanetRelativePosition(Vec3 pos, CelestialBody celestialBody, boolean isNormalized) {
        return getPlanetRotatedPosition(pos.x, pos.y, pos.z, celestialBody.getRadius(), celestialBody.getRotation(), isNormalized);
    }

    public static Vector3d getPlanetRelativePosition(double posX, double posY, double posZ, CelestialBody celestialBody, boolean isNormalized) {
        return getPlanetRotatedPosition(posX, posY, posZ, celestialBody.getRadius(), celestialBody.getRotation(), isNormalized);
    }

    public static Vector3d getPlanetRotatedPosition(double posX, double posY, double posZ, double planetRadius,
                                                    Quaternionfc planetRot, boolean isNormalized) {
        Vector3d quadSpherePos = getPlanetRelativeNonRotatingPosition(posX, posY, posZ, planetRadius, isNormalized);
        quadSpherePos.rotate(new Quaterniond().set(planetRot));
        return quadSpherePos;
    }

    public static Vector3d getPlanetRelativeNonRotatingPosition(double posX, double posY, double posZ, double planetRadius, boolean isNormalized) {
        double cellSize = getSquareCellSize(planetRadius);
        double halfCellSize = cellSize*0.5d;

        int xCell = getCellIndex(cellSize, posX);
        int zCell = getCellIndex(cellSize, posZ);

        xCell = Mth.clamp(xCell,-1, 2);

        if (xCell == 0) {
            zCell = Mth.clamp(zCell,-1, 1);
        }
        else {
            zCell = 0;
        }

        double xWithinCell = posX - xCell*cellSize;
        double zWithinCell = posZ - zCell*cellSize;

        xWithinCell = Mth.clamp(xWithinCell, -halfCellSize, halfCellSize);
        zWithinCell = Mth.clamp(zWithinCell, -halfCellSize, halfCellSize);

        int QuadId = getQuadID(xCell, zCell);

        double radius = 0.5d;
        if (!isNormalized) {
            radius = planetRadius + posY;
        }
        return getQuadPlanettoSquarePos(zWithinCell, xWithinCell, halfCellSize, QuadId, radius);
    }

    public static int getQuadID(double cellSize, double posX, double posZ) {
        int xCell = getCellIndex(cellSize, posX);
        int zCell = getCellIndex(cellSize, posZ);

        return getQuadID(xCell, zCell);
    }

    public static int getQuadID(int xCell, int zCell) {
        xCell = Mth.clamp(xCell,-1, 2);

        if (xCell == 0) {
            zCell = Mth.clamp(zCell,-1, 1);
        }
        else {
            zCell = 0;
        }

        int quadId = xCell + 1;
        if (xCell == 0) {
            if (zCell == 1){
                quadId = 4;
            }
            else if (zCell == -1) {
                quadId = 5;
            }
        }
        return quadId;
    }

    public static boolean isPosInsidePlanetBounds(double posX, double posZ, CelestialBody celestialBody) {
        double cellSize = getSquareCellSize(celestialBody.getRadius());
        return isPosInsidePlanetBounds(posX, posZ, cellSize);
    }

    public static boolean isPosInsidePlanetBounds(double posX, double posZ, double cellSize) {
        int xCell = getCellIndex(cellSize, posX);
        int zCell = getCellIndex(cellSize, posZ);

        if (xCell == 0) {
            return (zCell <= 1 && zCell >= -1);
        } else {
            return  (xCell >= -1 && xCell <= 2) && zCell == 0;
        }
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

    public static Vector2d getDimensionPosition(Vector3dc pos, double planetRadius, CelestialBody celestialBody) {
        int squareSide = 0;
        Vector3d position = new Vector3d(pos).normalize();
        position.rotate(new Quaterniond(celestialBody.getRotation()).invert());
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

    public static Quaterniond getPlanetToSpaceRotation(Vector3dc planetDimensionPos, CelestialBody celestialBody) {
        double cellSize = getSquareCellSize(celestialBody.getRadius());
        int quadID = getQuadID(cellSize, planetDimensionPos.x(), planetDimensionPos.z());
        Vector3d upVector = getPlanetRelativeNonRotatingPosition(planetDimensionPos.x(), planetDimensionPos.y(), planetDimensionPos.z(),
                celestialBody.getRadius(), true);
        upVector.normalize();

        Vector3d rightVector = new Vector3d();
        Vector3d forwardVector = new Vector3d();

        switch (quadID) {
            case 0, 1, 2, 3 -> upVector.cross(0d, 1d, 0d, rightVector);
            case 4 -> upVector.cross(1d, 0d, 0d, rightVector);
            case 5 -> upVector.cross(-1d, 0d, 0d, rightVector);
        }

        forwardVector = rightVector.cross(upVector, forwardVector);
        forwardVector.normalize();

        Quaterniond finalRot = new Quaterniond();
        finalRot.lookAlong(forwardVector, upVector);

        finalRot.mul(new Quaterniond(celestialBody.getRotation()).invert());
        finalRot.normalize();

        return finalRot;
    }

    public static Quaterniond getShipPlanetToSpaceRotation(ShipTransform shipTransform, CelestialBody celestialBody) {
        Quaterniond planetToSpaceRotation = getPlanetToSpaceRotation(shipTransform.getPositionInWorld(), celestialBody);

        return new Quaterniond();
    }

    public static Quaterniond getShipSpaceToPlanetRotation(Vector3dc planetDimensionPos, Vector3dc planetRelativePosition, CelestialBody celestialBody) {
        return new Quaterniond();
    }
}
