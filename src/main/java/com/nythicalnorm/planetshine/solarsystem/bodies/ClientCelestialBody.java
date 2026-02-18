package com.nythicalnorm.planetshine.solarsystem.bodies;

import net.minecraft.resources.ResourceLocation;

public interface ClientCelestialBody {
    String getName();
    ResourceLocation getMainTexture();

    void setMainTexture(ResourceLocation texResourceLocation);
}
