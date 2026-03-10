package com.nythicalnorm.planetshine.util;

import com.nythicalnorm.planetshine.util.calculations.PlanetCalc;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

class CalcsTest {
    @Test
    void planetDimPosToNormalizedVectorTest() {
        Quaternionf rot = new Quaternionf().rotateY((float) 2d).rotateX((float) 1d);
        for (int i = -15; i <= 25; i ++) {
            Vec3 pos = new Vec3(i*1_000_000, 0, -2_000_000);
            Vector3d planetPos = PlanetCalc.getPlanetRelativePosition(pos, 6371000, rot, false);
            System.out.println("z:" + pos.z + ", Pos = " + planetPos);

//            Vector2d reconvertedPos = PlanetCalc.vectorToPlanetDimPos(planetPos, 6371000, rot);
//            System.out.println("\nz:" + reconvertedPos.y + ", reconverted = " + reconvertedPos);
            //assertTrue(LastPlanetPos.y < planetPos.y);
        }
    }
}