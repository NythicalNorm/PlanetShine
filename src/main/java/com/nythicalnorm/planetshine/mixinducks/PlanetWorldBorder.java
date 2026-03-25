package com.nythicalnorm.planetshine.mixinducks;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface PlanetWorldBorder {
    VoxelShape ps$getPlanetBorder();
    void ps$setPlanetBorder(CelestialBody celestialBody);
}
