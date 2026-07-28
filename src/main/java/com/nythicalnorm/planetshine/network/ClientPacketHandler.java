package com.nythicalnorm.planetshine.network;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.gui.screen.map.BlockEntityMapScreen;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.star.StarBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import com.nythicalnorm.planetshine.spacecraft.player.AbstractPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.ClientPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClientPacketHandler {
    public static void StartClientPacket(long currentTime, long timeWarp, EntityOrbitBody<?> playerData, OrbitId playerParentOrbit,List<CelestialBody> planetaryBodyList) {
        Map<OrbitId, CelestialBody> AllPlanetaryBodies = new Object2ObjectOpenHashMap<>();
        ConcurrentMap<OrbitId, EntityOrbitBody<?>> AllSpacecraftBodies = new ConcurrentHashMap<>();
        Map<ResourceKey<Level>, CelestialBody> PlanetDimensions = new Object2ObjectOpenHashMap<>();
        StarBody rootStar = null;

        for (CelestialBody planetaryBody : planetaryBodyList) {
            if (planetaryBody instanceof StarBody starBody) {
                rootStar = starBody;
            }
            if (planetaryBody.getDimension() != null) {
                PlanetDimensions.put(planetaryBody.getDimension(), planetaryBody);
            }
            AllPlanetaryBodies.put(planetaryBody.getOrbitId(), planetaryBody);
        }
        if (rootStar == null) {
            throw new IllegalStateException ("can't start client Solar system without a host star");
        }
        SolarSystem solarSystem = new SolarSystem(AllPlanetaryBodies, AllSpacecraftBodies, PlanetDimensions, rootStar);
        ClientPlayerOrbitBody clientPlayerSpacecraftBody;
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) {
            PlanetShine.logError("this shouldn't happen, but shut up, intellij");
            return;
        }
        if (playerData instanceof ClientPlayerOrbitBody plrSpacecraftBody) {
            if (playerParentOrbit != null) {
                solarSystem.entityJoinedOrbital(playerData, playerParentOrbit);
                plrSpacecraftBody.setBody(localPlayer);
            }
            clientPlayerSpacecraftBody = plrSpacecraftBody;
        } else {
            AbstractPlayerOrbitBody.PlayerOrbitBuilder playerSpacecraftBuilder = new AbstractPlayerOrbitBody.PlayerOrbitBuilder();
            playerSpacecraftBuilder.setPlayer(localPlayer);
            clientPlayerSpacecraftBody = (ClientPlayerOrbitBody) playerSpacecraftBuilder.buildClientSide();
        }
        PSClient css =  new PSClient(clientPlayerSpacecraftBody, solarSystem);
        css.setCurrentTime(currentTime);
        css.setTimePassPerTick(timeWarp);

//        localPlayer.connection.getOnlinePlayers().forEach(playerInfo -> {
//            if (css.getSolarSystem().getSpacecraftOrbit(new OrbitId(playerInfo.getProfile().getId())) instanceof ClientPlayerOrbitBody clientPlayerOrbitBody) {
//                clientPlayerOrbitBody.setPlayerInfo(playerInfo);
//            }
//        });
    }

    public static void localPlayerJoinOrbital(OrbitId newParentID, OrbitalElementsc orbitalElements) {
        PSClient.getInstance().ifPresent(psClient ->
                psClient.localPlayerJoinOrbital(newParentID, orbitalElements));
    }

    public static void OrbitSOIChange(OrbitId spacecraftID, OrbitId newParentID, OrbitalElementsc orbitalElements) {
        PSClient.getInstance().ifPresent(psClient ->
                psClient.orbitSOIChange(spacecraftID, newParentID, orbitalElements));
    }

    public static void orbitRemove(OrbitId spacecraftID) {
        PSClient.getInstance().ifPresent(psClient ->
                psClient.orbitRemove(spacecraftID));
    }

    public static void incomingLodTexture(ResourceKey<Level> dimensionID, int textureID, int textureSize, byte[] biomeTexture) {
        PSClient.getInstance().ifPresent(psClient ->
                psClient.getPlanetTexManager().incomingLodTexture(dimensionID, textureID, textureSize, biomeTexture));
    }

    public static void incomingPlanetTexture(OrbitId planetID, byte[] planetTexture) {
        PSClient.getInstance().ifPresent(psClient ->
                psClient.getPlanetTexManager().incomingPlanetTexture(psClient.getSolarSystem().getPlanet(planetID), planetTexture));
    }

    public static void UpdateTimeState(long currenttime) {
        PSClient.getInstance().ifPresent(ps ->
                ps.clientTimeHandler.UpdateState(currenttime));
    }

    public static void timeWarpSetFromServer(boolean successfullyChanged, long setTimeWarpSpeed) {
        PSClient.getInstance().ifPresent(psClient ->
                psClient.timeWarpSetFromServer(successfullyChanged, setTimeWarpSpeed));
    }

    public static void hostOrbitSet(OrbitId spaceHostOrbitId, Vector3d originPos) {
        PSClient.getInstance().ifPresent(psClient ->
                psClient.setHostOrbit(spaceHostOrbitId, originPos));
    }

    public static void orbitChange(OrbitId spacecraftID, OrbitalElementsc orbitalElements) {
        PSClient.getInstance().ifPresent(psClient ->
                psClient.orbitChange(spacecraftID, orbitalElements));
    }

    public static void stateVectorChange(OrbitId spacecraftID, Vector3dc relativePosition, Vector3dc relativeVelocity) {
        PSClient.getInstance().ifPresent(psClient ->
                psClient.stateVectorChange(spacecraftID, relativePosition, relativeVelocity));
    }

    public static void entityBodyList(List<NetworkEncoders.TempEntityOrbitHolder> entityOrbitHolderList) {
        if (PSClient.get() != null) {
            SolarSystem solarSystem = PSClient.get().getSolarSystem();
            for (NetworkEncoders.TempEntityOrbitHolder entityOrbitHolder : entityOrbitHolderList) {
                solarSystem.entityJoinedOrbital(entityOrbitHolder.orbitBody(), entityOrbitHolder.parentID());
            }
        }
    }

    public static void entityBodyJoinOrbital(EntityOrbitBody<?> entityOrbitBody, OrbitId orbitParent) {
        if (PSClient.get() != null) {
            PSClient.get().entityJoinOrbital(entityOrbitBody, orbitParent);
        }
    }

    public static void setOrbitIntercept(OrbitId spacecraftID, OrbitalCalc.@Nullable SOIIntercept soiIntercept) {
        if (PSClient.get() != null) {
            PSClient.get().setOrbitIntercept(spacecraftID, soiIntercept);
        }
    }

    public static void openTimeWarpMapScreen() {
        PSClient psClient = PSClient.get();
        if (psClient != null) {
            if (psClient.doRender()) {
                psClient.addRunnableToRenderTick(() -> Minecraft.getInstance().setScreen(new BlockEntityMapScreen()));
            }
        }
    }
}
