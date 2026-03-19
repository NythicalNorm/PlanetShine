package com.nythicalnorm.planetshine.solarsystem.bodies.planet;

import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.joml.Vector2i;

import java.util.Map;

public class DaylightData {
    private final Map<Vector2i, DaylightRegion> daylightRegions;
    private final CelestialBody celestialBody;
    private final int daylightRegionSize;

    public DaylightData(CelestialBody celestialBody) {
        this.celestialBody = celestialBody;
        this.daylightRegions = new Object2ObjectOpenHashMap<>();
        this.daylightRegionSize = (int) (celestialBody.getRadius() * Math.PI / 180d);
    }

    public DaylightRegion getOrCalculateRegionAt(double x, double z) {
        Vector2i pos = getRegionLoc(x, z);
        DaylightRegion region = daylightRegions.computeIfAbsent(pos, k -> new DaylightRegion());

        if (!region.isCalculatedThisTick()) {
            region.calculate(pos.x, pos.y, this.celestialBody, celestialBody.getCelestialServerData().getServerLevel());
        }

        return region;
    }

    private Vector2i getRegionLoc(double x, double z) {
        int locX = (int) (Math.round(x / daylightRegionSize) * daylightRegionSize);
        int locZ = (int) (Math.round(z / daylightRegionSize) * daylightRegionSize);
        return new Vector2i(locX, locZ);
    }

    public void tickStart() {
        for (DaylightRegion region : daylightRegions.values()) {
            region.setCalculatedThisTick(false);
        }
    }

    public void tickEnd() {
        daylightRegions.entrySet().removeIf(entry -> !entry.getValue().isCalculatedThisTick());
    }
}
