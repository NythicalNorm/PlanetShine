package com.nythicalnorm.planetshine.gui.screen;

import org.joml.Quaterniondc;

public interface ISpacecraftOrbitDataDisplay {
    Quaterniondc getSpacecraftRotation();
    float getGForce();
    double getVelocity();
    double getAltitude();
}
