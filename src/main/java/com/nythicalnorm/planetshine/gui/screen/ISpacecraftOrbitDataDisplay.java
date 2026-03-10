package com.nythicalnorm.planetshine.gui.screen;

import org.joml.Quaterniondc;
import org.joml.Vector3dc;

public interface ISpacecraftOrbitDataDisplay {
    Quaterniondc getSpacecraftRotation();
    float getGForce();
    Vector3dc getVelocityVector();
    double getAltitude();
}
