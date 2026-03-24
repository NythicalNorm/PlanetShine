package com.nythicalnorm.planetshine.network;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.solarsystem.*;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetAtmosphere;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class NetworkEncoders {

    public static void writeOrbitalBody(FriendlyByteBuf friendlyByteBuf, OrbitalBody orbitalBody) {
        orbitalBody.getType().get().encodeToBuffer(orbitalBody, friendlyByteBuf);
    }

    public static OrbitalBody readOrbitalBody(FriendlyByteBuf friendlyByteBuf) {
        return OrbitalBodyTypeRegistry.getType(friendlyByteBuf.readResourceLocation()).decodeFromBuffer(friendlyByteBuf).build();
    }

    @OnlyIn(Dist.CLIENT)
    public static OrbitalBody readOrbitalBodyClient(FriendlyByteBuf friendlyByteBuf) {
        return OrbitalBodyTypeRegistry.getType(friendlyByteBuf.readResourceLocation()).decodeFromBuffer(friendlyByteBuf).buildClientSide();
    }

    public static void writePlanetaryBodyList(FriendlyByteBuf friendlyByteBuf, List<CelestialBody> bodyList) {
        friendlyByteBuf.writeVarInt(bodyList.size());

        for (CelestialBody orbitBody : bodyList) {
            NetworkEncoders.writeOrbitalBody(friendlyByteBuf, orbitBody);
            List<OrbitId> planetChildBodiesIDs = new ArrayList<>();

            for (CelestialBody childBody : orbitBody.getPlanetChildren()) {
                planetChildBodiesIDs.add(childBody.getOrbitId());
            }

            friendlyByteBuf.writeVarInt(planetChildBodiesIDs.size());
            for (OrbitId childBodyID : planetChildBodiesIDs) {
                childBodyID.encodeToBuffer(friendlyByteBuf);
            }
        }
    }

    public static List<CelestialBody> readPlanetaryBodyList(FriendlyByteBuf friendlyByteBuf) {
        int planetNo = friendlyByteBuf.readVarInt();
        Map<OrbitId, TempPlanetaryHolder> tempPlanetHolderMap = new Object2ObjectOpenHashMap<>();

        for (int i = 0; i < planetNo; i++) {
            if (NetworkEncoders.readOrbitalBodyClient(friendlyByteBuf) instanceof CelestialBody planetaryBody) {
                List<OrbitId> childPlanets = new ArrayList<>();
                int childSize = friendlyByteBuf.readVarInt();

                for (int j = 0; j < childSize; j++) {
                    OrbitId childOrbitId = new OrbitId(friendlyByteBuf);
                    childPlanets.add(childOrbitId);
                }

                tempPlanetHolderMap.put(planetaryBody.getOrbitId(), new TempPlanetaryHolder(planetaryBody, childPlanets));
            }
        }
        // setting the parent references
        for (TempPlanetaryHolder holder : tempPlanetHolderMap.values()) {
            for (OrbitId childID : holder.orbitIdList) {
                if (tempPlanetHolderMap.containsKey(childID)) {
                   holder.planetaryBody.addChildPlanet(tempPlanetHolderMap.get(childID).planetaryBody);
                } else {
                    PlanetShine.logError("unable to parse planetaryBody due to improper static bodies orbiting planets");
                }
            }
        }
        List<CelestialBody> bodyList = new ArrayList<>();
        tempPlanetHolderMap.forEach((orbitId, tempPlanetaryHolder) -> bodyList.add(tempPlanetaryHolder.planetaryBody));
        return bodyList;
    }

    public static void writeEntityBodyList(FriendlyByteBuf friendlyByteBuf, List<EntityOrbitBody<?>> bodyList) {
        friendlyByteBuf.writeVarInt(bodyList.size());

        for (EntityOrbitBody<?> orbitBody : bodyList) {
            if (orbitBody.getParent() != null) {
                NetworkEncoders.writeOrbitalBody(friendlyByteBuf, orbitBody);
                orbitBody.getParent().getOrbitId().encodeToBuffer(friendlyByteBuf);
            }
        }
    }

    public static List<TempEntityOrbitHolder> readEntityBodyList(FriendlyByteBuf friendlyByteBuf) {
        int bodyNo = friendlyByteBuf.readVarInt();
        List<TempEntityOrbitHolder> tempEntityOrbitHolder = new ArrayList<>();

        for (int i = 0; i < bodyNo; i++) {
            if (NetworkEncoders.readOrbitalBodyClient(friendlyByteBuf) instanceof EntityOrbitBody<?> entityOrbitBody) {
                OrbitId parentID = new OrbitId(friendlyByteBuf);
                tempEntityOrbitHolder.add(new TempEntityOrbitHolder(entityOrbitBody, parentID));
            }
        }
        return tempEntityOrbitHolder;
    }

    public static void writeOrbitalElements(FriendlyByteBuf friendlyByteBuf, OrbitalElementsc orbitalElements) {
        friendlyByteBuf.writeDouble(orbitalElements.getSemiMajorAxis());
        friendlyByteBuf.writeDouble(orbitalElements.getEccentricity());
        friendlyByteBuf.writeLong(orbitalElements.getPeriapsisTime());

        friendlyByteBuf.writeDouble(orbitalElements.getInclination());
        friendlyByteBuf.writeDouble(orbitalElements.getArgumentOfPeriapsis());
        friendlyByteBuf.writeDouble(orbitalElements.getLongitudeOfAscendingNode());
        friendlyByteBuf.writeDouble(orbitalElements.getParentMass());
    }

    public static OrbitalElements readOrbitalElements(FriendlyByteBuf friendlyByteBuf) {
        return new OrbitalElements(
                friendlyByteBuf.readDouble(),
                friendlyByteBuf.readDouble(),
                friendlyByteBuf.readLong(),
                friendlyByteBuf.readDouble(),
                friendlyByteBuf.readDouble(),
                friendlyByteBuf.readDouble(),
                friendlyByteBuf.readDouble()
        );
    }

    public static void writePlanetAtmosphere(FriendlyByteBuf byteBuf, PlanetAtmosphere atmosphere) {
        byteBuf.writeBoolean(atmosphere.hasAtmosphere());
        byteBuf.writeInt(atmosphere.getOverlayColorInt());
        byteBuf.writeInt(atmosphere.getAtmoColorInt());
        byteBuf.writeDouble(atmosphere.getAtmosphereHeight());
        byteBuf.writeFloat(atmosphere.getAtmosphereAlpha());
        byteBuf.writeFloat(atmosphere.getAlphaNight());
        byteBuf.writeFloat(atmosphere.getAlphaDay());
    }

    public static PlanetAtmosphere readPlanetAtmosphere(FriendlyByteBuf byteBuf) {
        return new PlanetAtmosphere(
                byteBuf.readBoolean(),
                byteBuf.readInt(),
                byteBuf.readInt(),
                byteBuf.readDouble(),
                byteBuf.readFloat(),
                byteBuf.readFloat(),
                byteBuf.readFloat()
        );
    }

    public static void writeVector3d(FriendlyByteBuf buffer, Vector3dc pVector3f) {
        buffer.writeDouble(pVector3f.x());
        buffer.writeDouble(pVector3f.y());
        buffer.writeDouble(pVector3f.z());
    }

    public static void writeQuaternionfc(FriendlyByteBuf buf, Quaternionfc pQuaternion) {
        buf.writeFloat(pQuaternion.x());
        buf.writeFloat(pQuaternion.y());
        buf.writeFloat(pQuaternion.z());
        buf.writeFloat(pQuaternion.w());
    }

    public static Vector3d readVector3d(FriendlyByteBuf buffer) {
        return new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void writeASCII(FriendlyByteBuf friendlyByteBuf, String text) {
        friendlyByteBuf.writeVarInt(text.length());
        friendlyByteBuf.writeCharSequence(text, StandardCharsets.US_ASCII);
    }

    public static String readASCII(FriendlyByteBuf friendlyByteBuf) {
        int stringSize = friendlyByteBuf.readVarInt();
        return friendlyByteBuf.readCharSequence(stringSize, StandardCharsets.US_ASCII).toString();
    }

    public static void writeOrbitIntercept(FriendlyByteBuf friendlyByteBuf, OrbitalCalc.SOIIntercept soiIntercept) {
        friendlyByteBuf.writeDouble(soiIntercept.trueAnomaly());
        friendlyByteBuf.writeLong(soiIntercept.timeElapsed());
        soiIntercept.interceptingBody().encodeToBuffer(friendlyByteBuf);
        friendlyByteBuf.writeBoolean(soiIntercept.isEscape());
    }

    public static OrbitalCalc.SOIIntercept readOrbitIntercept(FriendlyByteBuf friendlyByteBuf) {
        return new OrbitalCalc.SOIIntercept(
                friendlyByteBuf.readDouble(),
                friendlyByteBuf.readLong(),
                new OrbitId(friendlyByteBuf),
                friendlyByteBuf.readBoolean()
        );
    }

    private record TempPlanetaryHolder(CelestialBody planetaryBody, List<OrbitId> orbitIdList) {}

    public record TempEntityOrbitHolder(EntityOrbitBody<?> orbitBody, OrbitId parentID) {}
}
