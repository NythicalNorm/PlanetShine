package com.nythicalnorm.nythicalSpaceProgram.util;

import com.nythicalnorm.nythicalSpaceProgram.planet.PlanetaryBody;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class DayNightCycleHandler {

    public static void getSunAngle(Vec3 WorldPos, Level level) {

    }

    public static float getSunAngle(Vector3d EntityRelativePos, Vector3d planetAbsolutePos) {
        Vector3f entityDir = new Vector3f((float) EntityRelativePos.x,(float) EntityRelativePos.y,(float) EntityRelativePos.z);
        Vector3f sunDir = new Vector3f((float) planetAbsolutePos.x,(float) planetAbsolutePos.y,(float) planetAbsolutePos.z);
        entityDir.normalize();
        sunDir.normalize();
        float diff = sunDir.dot(entityDir);
        diff = (diff + 1.0f) * 0.25f;
        return Mth.clamp(diff, 0f, 1f);
    }

    public static float getSunAngleAtSpawn(PlanetaryBody planetaryBody) {
        Vector3d spawnLocation = new Vector3d(planetaryBody.getRadius(), 0f, 0f);
        Quaternionf planetRot = planetaryBody.getPlanetRotation();
        spawnLocation.rotate(new Quaterniond(planetRot.x, planetRot.y,planetRot.z, planetRot.w));
        Vector3d planetAbsolutePos = planetaryBody.getPlanetAbsolutePos().add(spawnLocation);
        return getSunAngle(spawnLocation, planetAbsolutePos);
    }
}
