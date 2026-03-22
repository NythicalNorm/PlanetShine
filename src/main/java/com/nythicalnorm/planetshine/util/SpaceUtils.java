package com.nythicalnorm.planetshine.util;

import com.nythicalnorm.planetshine.dimensions.SpaceDimension;
import com.nythicalnorm.planetshine.dimensions.SpaceServerLevel;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.util.calculations.PlanetCalc;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.joml.*;

import java.lang.Math;
import java.util.Collection;
import java.util.HashSet;

public class SpaceUtils {
    // is this sus? I mean it's not like I am planning to change the dimension's name.
    private static final String spaceLevelString = "minecraft:dimension:planetshine:spacedim";

    public static boolean isSpaceLevel(Level level) {
        return level.dimension().equals(SpaceDimension.SPACE_LEVEL_KEY);
    }

    public static boolean isSpaceLevel(ServerLevel serverLevel) {
        return serverLevel instanceof SpaceServerLevel;
    }

    public static boolean isSpaceLevel(String chunkClaimDimension) {
        return spaceLevelString.equals(chunkClaimDimension);
    }

    public static Vector2dc getLatLongCoordinates(double xPos, double yPos, double zPos, CelestialBody planet) {
        if (planet != null) {
            Vector3d planetNonRotPos = PlanetCalc.getPlanetRelativeNonRotatingPosition(xPos, yPos, zPos,
                    planet.getRadius(), true);
            double latitude = Math.asin(planetNonRotPos.y);
            double longitude = Math.atan2(-planetNonRotPos.z, planetNonRotPos.x);
            return new Vector2d(Math.toDegrees(latitude), Math.toDegrees(longitude));
        }
        return null;
    }

    private static final double distanceToSearch = 2000;

    public static String getSpaceLevelString() {
        return spaceLevelString;
    }

    //don't call this while time warping
    private HashSet<ShipIntercept> getEntityBodyIntersections(Collection<EntityOrbitBody<?>> childEntityBodies) {
        double dist = distanceToSearch * distanceToSearch;
        HashSet<ShipIntercept> shipIntercepts = new HashSet<>();
        
        return shipIntercepts;
    }

    public record ShipIntercept(EntityOrbitBody<?> bodyA, EntityOrbitBody<?> bodyB) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            ShipIntercept shipIntercept = (ShipIntercept) obj;
            if (shipIntercept.bodyA() == this.bodyA && shipIntercept.bodyB() == this.bodyB) {
                return true;
            } else if (shipIntercept.bodyA() == this.bodyB() && shipIntercept.bodyB() == this.bodyA) {
                return true;
            }
            return false;
        }
    }
}
