package com.nythicalnorm.planetshine.gui.screen;

import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import org.joml.Quaterniondc;
import org.joml.Vector3dc;

public interface ISpacecraftOrbitDataDisplay {
    Quaterniondc getSpacecraftRotation();
    float getGForce();
    Vector3dc getRelativeVelocity();
    double getAltitude();
    OrbitalElementsc getOrbitalElements();
    Vector3dc getRelativePosition();
}
