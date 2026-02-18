package com.nythicalnorm.planetshine.solarsystem.bodies.planet;

import com.nythicalnorm.planetshine.solarsystem.bodies.ClientCelestialBody;
import net.minecraft.resources.ResourceLocation;

public class ClientPlanetaryBody extends PlanetaryBody implements ClientCelestialBody {
    private ResourceLocation mainTexture;

    public ClientPlanetaryBody(PlanetBuilder planetBuilder) {
        super(planetBuilder);
    }

    @Override
    public ResourceLocation getMainTexture() {
        return mainTexture;
    }

    @Override
    public void setMainTexture(ResourceLocation texResourceLocation) {
        mainTexture = texResourceLocation;
    }
}
