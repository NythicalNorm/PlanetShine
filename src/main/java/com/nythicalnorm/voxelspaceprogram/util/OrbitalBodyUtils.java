package com.nythicalnorm.voxelspaceprogram.util;

import com.nythicalnorm.voxelspaceprogram.dimensions.SpaceDimension;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.voxelspaceprogram.util.calculations.PlanetBodyCalc;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;

public class OrbitalBodyUtils {
    public static Vector3d getRelativePositon(Vector3dc pos, CelestialBody celestialBody) {
        Vec3 vec3Pos = new Vec3(pos.x(), pos.y(), pos.z());
        return PlanetBodyCalc.planetDimPosToNormalizedVector(vec3Pos, celestialBody.getRadius(), celestialBody.getRotation(), false);
    }

    public static Quaterniond getSpaceRotationFromPlanetPos(Vector3dc relativePlanetPosition, CelestialBody celestialBody) {
        Quaternionf planetRotation = new Quaternionf(new AxisAngle4d(Mth.HALF_PI,1f,0f,0f));
        Vector3f playerRelativePos = new Vector3f((float) relativePlanetPosition.x(), (float) relativePlanetPosition.y(), (float) relativePlanetPosition.z());
        playerRelativePos.normalize();
        Vector3f upVector = PlanetBodyCalc.getUpVectorForPlanetRot(new Vector3f(playerRelativePos), celestialBody);
        planetRotation.lookAlong(playerRelativePos, upVector);
        return new Quaterniond(planetRotation);
    }

    public static Vector2dc getLatLongCoordinates(Vec3 pos, CelestialBody currentPlanetOn) {
        if (currentPlanetOn != null) {
            Vector3d planetNonRotPos = PlanetBodyCalc.getNonRotatedDimPosFromNormalizeVector(pos,
                    currentPlanetOn.getRadius(), true);
            double latitude = Math.asin(planetNonRotPos.y);
            double longitude = Math.atan2(-planetNonRotPos.z, planetNonRotPos.x);
            return new Vector2d(Math.toDegrees(latitude), Math.toDegrees(longitude));
        }
        return null;
    }

    public static boolean isSpaceLevel(Level level) {
        return level.dimension().equals(SpaceDimension.SPACE_LEVEL_KEY);
    }
}
