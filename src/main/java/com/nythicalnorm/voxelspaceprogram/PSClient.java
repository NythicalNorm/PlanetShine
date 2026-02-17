package com.nythicalnorm.voxelspaceprogram;

import com.nythicalnorm.voxelspaceprogram.gui.PSScreenManager;
import com.nythicalnorm.voxelspaceprogram.network.PacketHandler;
import com.nythicalnorm.voxelspaceprogram.network.time.ServerboundTimeWarpChange;
import com.nythicalnorm.voxelspaceprogram.rendering.PSRenderer;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.CelestialBodyAccessor;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.ClientCelestialBody;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.planet.DaylightRegion;
import com.nythicalnorm.voxelspaceprogram.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.voxelspaceprogram.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitId;
import com.nythicalnorm.voxelspaceprogram.rendering.networking.ClientTimeHandler;
import com.nythicalnorm.voxelspaceprogram.rendering.textures.ClientTexManager;
import com.nythicalnorm.voxelspaceprogram.solarsystem.SolarSystem;
import com.nythicalnorm.voxelspaceprogram.rendering.renderers.SpaceObjRenderer;
import com.nythicalnorm.voxelspaceprogram.spacecraft.EntityOrbitBody;
import com.nythicalnorm.voxelspaceprogram.spacecraft.player.ClientPlayerOrbitBody;
import com.nythicalnorm.voxelspaceprogram.util.OrbitalBodyUtils;
import com.nythicalnorm.voxelspaceprogram.util.Stage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class PSClient extends Stage {
    private static PSClient instance;
    private final Minecraft minecraft;

    private final @NotNull ClientPlayerOrbitBody playerOrbit;
    private CelestialBody currentPlanetOn;
    private ClientPlayerOrbitBody controllingBody;
    private final DaylightRegion daylightRegion;
    public ClientTimeHandler clientTimeHandler;

    private final PSScreenManager screenManager;
    private final ClientTexManager planetTexManager;

    public PSClient(@NotNull ClientPlayerOrbitBody playerDataFromServer, SolarSystem solarSystem) {
        super(solarSystem);
        instance = this;
        minecraft = Minecraft.getInstance();
        this.playerOrbit = playerDataFromServer;
        SpaceObjRenderer.PopulateRenderPlanets(solarSystem);
        this.screenManager = new PSScreenManager();
        this.planetTexManager = new ClientTexManager(this);
        if (minecraft.level != null) {
            onClientLevelLoad(minecraft.level);
        }
        this.solarSystem.getRootStar().initCalcs();
        clientTimeHandler = new ClientTimeHandler();
        this.daylightRegion = new DaylightRegion();
    }

    public static Optional<PSClient> getInstance() {
        if (instance != null) {
            return Optional.of(instance);
        }
        return Optional.empty();
    }

    public static PSClient get() {
        return instance;
    }

    public ClientCelestialBody getClientPlanet(OrbitId planetID) {
       return (ClientCelestialBody) this.solarSystem.getPlanet(planetID);
    }

    public void setHostOrbit(OrbitId orbitId) {
        this.getPlayerOrbit().setHostSpace(orbitId);
    }

    public float getSunAngleOpacity() {
        float angle = this.daylightRegion.getSunAngle();
        angle = angle < 0.5f ? angle * 2f : (1.0f - angle) * 2f;
        return angle;
    }

    public static void close() {
        if (instance != null) {
            instance.getPlanetTexManager().close();
            PSRenderer.close();
            Stage.close();
            instance = null;
        }
    }

    public void tick() {
        if (controllingBody != null && screenManager.isSpacecraftScreenOpen() && getCurrentTimeWarpSetting() == 0) {
            screenManager.getSpacecraftScreen().sendInputs(controllingBody);
        }
        clientTimeHandler.tick();
    }

    public void onClientLevelLoad(ClientLevel clientLevel) {
        CelestialBody celestialBody = solarSystem.getDimensionOfPlanet(clientLevel.dimension());
        if (celestialBody != null) {
            ((CelestialBodyAccessor) clientLevel).ps$setCelestialBody(celestialBody);
            currentPlanetOn = celestialBody;
        } else {
            currentPlanetOn = null;
        }
    }

    public void renderTick(float partialTick) {
        this.setCurrentTime(clientTimeHandler.calculateCurrentTime(partialTick));
        this.solarSystem.UpdatePlanets(this.getCurrentTime(), this.isTimeWarping());

        if (!weInSpaceDim()) {
            playerOrbit.setParent(null);
        }

        if (currentPlanetOn != null && playerOrbit.getPlayerEntity() != null) {
            playerOrbit.updatePlayerPosRot(currentPlanetOn);
            BlockPos playerPos = playerOrbit.getPlayerEntity().blockPosition();
            this.daylightRegion.calculate(playerPos.getX(), playerPos.getZ(), currentPlanetOn, playerOrbit.getPlayerEntity().level());
        }
    }

    public DaylightRegion getDaylightRegion() {
        return daylightRegion;
    }

    public @NotNull ClientPlayerOrbitBody getPlayerOrbit() {
        return playerOrbit;
    }

    public void TryChangeTimeWarp(boolean doInc) {
        int propesedSetIndex = getCurrentTimeWarpSetting();
        propesedSetIndex = doInc ? ++propesedSetIndex : --propesedSetIndex;

        if (propesedSetIndex >= 0 && propesedSetIndex < timeWarpSettings.size()) {
            PacketHandler.sendToServer(new ServerboundTimeWarpChange(timeWarpSettings.get(propesedSetIndex)));
        }
    }

    public void timeWarpSetFromServer(boolean successfullyChanged, long setTimeWarpSpeed) {
        if (successfullyChanged) {
            setTimePassPerTick(setTimeWarpSpeed);
        }
    }

    public void orbitSOIChange(OrbitId spacecraftID, OrbitId newParentID, OrbitalElements orbitalElements) {
        EntityOrbitBody entityOrbitBody = solarSystem.getAllSpacecraftBodies().get(spacecraftID);

        if (entityOrbitBody != null) {
            solarSystem.playerChangeOrbitalSOIs(entityOrbitBody, newParentID, orbitalElements);
        } else if (this.playerOrbit.getOrbitId().equals(spacecraftID)) {
            //temporary setting the rotation to default
            this.playerOrbit.setRotation(new Quaternionf());
            if (solarSystem.getAllSpacecraftBodies().containsKey(this.playerOrbit.getOrbitId())) {
                solarSystem.playerChangeOrbitalSOIs(this.playerOrbit, newParentID, orbitalElements);
            } else {
                // A temporary place to put it player joining has to be a separate packet
                this.playerOrbit.setOrbitalElements(orbitalElements);
                solarSystem.playerJoinedOrbital(this.playerOrbit, newParentID);
            }
        }
    }

    public void orbitChange(OrbitId spacecraftID, OrbitalElements orbitalElements) {
        EntityOrbitBody entityOrbitBody = this.solarSystem.getSpacecraftOrbit(spacecraftID);
        if (entityOrbitBody != null) {
            entityOrbitBody.setOrbitalElements(orbitalElements);
        }
    }

    public void orbitRemove(OrbitId spacecraftID) {
        EntityOrbitBody entityOrbitBody = solarSystem.getAllSpacecraftBodies().get(spacecraftID);
        if (entityOrbitBody != null) {
            solarSystem.entityRemoveOrbital(entityOrbitBody);
        }
    }

    public boolean doRender() {
        if (minecraft.level == null) {
            return false;
        }
        CelestialBodyAccessor planetAccessor = (CelestialBodyAccessor) minecraft.level;
        return planetAccessor.ps$isPlanet() || OrbitalBodyUtils.isSpaceLevel(minecraft.level);
    }

    public Optional<CelestialBody> getCurrentPlanet() {
        if (currentPlanetOn != null) {
            return Optional.of(currentPlanetOn);
        }
        else  {
            return Optional.empty();
        }
    }

    public Optional<CelestialBody> getCurrentPlanetSOIin() {
        if (playerOrbit.getParent() != null) {
            return Optional.of(playerOrbit.getParent());
        } else if (currentPlanetOn != null) {
            return Optional.of(currentPlanetOn);
        }
        else  {
            return Optional.empty();
        }
    }

    public Optional<OrbitalBody> getControllingBody() {
        if (controllingBody != null) {
            return  Optional.of(controllingBody);
        }
        return Optional.empty();
    }

    public void setControllingBody(ClientPlayerOrbitBody controllingBody) {
        this.controllingBody = controllingBody;
    }

    public boolean isOnPlanet()
    {
        return currentPlanetOn != null;
    }

    public boolean weInSpaceDim() {
        if (minecraft.level != null) {
            return OrbitalBodyUtils.isSpaceLevel(minecraft.level);
        } else {
            return false;
        }
    }

    public PSScreenManager getScreenManager() {
        return screenManager;
    }

    public ClientTexManager getPlanetTexManager() {
        return planetTexManager;
    }
}