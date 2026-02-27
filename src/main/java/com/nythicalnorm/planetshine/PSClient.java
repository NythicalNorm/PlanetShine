package com.nythicalnorm.planetshine;

import com.nythicalnorm.planetshine.gui.PSScreenManager;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.time.ServerboundTimeWarpChange;
import com.nythicalnorm.planetshine.rendering.PSRenderer;
import com.nythicalnorm.planetshine.rendering.map.MapRenderer;
import com.nythicalnorm.planetshine.rendering.renderTypes.SpaceRenderable;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBodyAccessor;
import com.nythicalnorm.planetshine.solarsystem.bodies.ClientCelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.DaylightRegion;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.rendering.networking.ClientTimeHandler;
import com.nythicalnorm.planetshine.rendering.textures.ClientTexManager;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.rendering.renderers.SpaceObjRenderer;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.ClientPlayerOrbitBody;
import com.nythicalnorm.planetshine.util.OrbitalBodyUtils;
import com.nythicalnorm.planetshine.util.Stage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

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

    // Rendering stuff
    private final MapRenderer mapRenderer;
    private SpaceRenderable[] renderPlanets;

    public PSClient(@NotNull ClientPlayerOrbitBody playerDataFromServer, SolarSystem solarSystem) {
        super(solarSystem);
        minecraft = Minecraft.getInstance();
        this.playerOrbit = playerDataFromServer;
        this.screenManager = new PSScreenManager();
        this.planetTexManager = new ClientTexManager(this);
        this.updatePlanets();

        this.clientTimeHandler = new ClientTimeHandler();
        this.daylightRegion = new DaylightRegion();
        this.mapRenderer = new MapRenderer();
        if (minecraft.level != null) {
            onClientLevelLoad(minecraft.level);
        }
        instance = this;
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

    @Override
    protected void updatePlanets() {
        super.updatePlanets();
        this.renderPlanets = SpaceObjRenderer.PopulateRenderPlanets(solarSystem);
    }

    public SpaceRenderable[] getSpaceRenderables() {
        return this.renderPlanets;
    }

    public MapRenderer getMapRenderer() {
        return mapRenderer;
    }

    public ClientCelestialBody getClientPlanet(OrbitId planetID) {
       return (ClientCelestialBody) this.solarSystem.getPlanet(planetID);
    }

    public void setHostOrbit(OrbitId orbitId) {
        this.getPlayerOrbit().setHostSpaceId(orbitId);
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
        clientTimeHandler.tick();
    }

    public void onClientLevelLoad(ClientLevel clientLevel) {
        this.screenManager.resetMapState();
        CelestialBody celestialBody = solarSystem.getDimensionOfPlanet(clientLevel.dimension());
        if (celestialBody != null) {
            ((CelestialBodyAccessor) clientLevel).ps$setCelestialBody(celestialBody);
            this.currentPlanetOn = celestialBody;
            this.solarSystem.entityRemoveOrbital(this.playerOrbit);
            this.currentPlanetOn.addChildBody(this.playerOrbit);
        } else {
            this.playerOrbit.clearRotation();
            this.currentPlanetOn = null;
            if (!OrbitalBodyUtils.isSpaceLevel(clientLevel)) {
                this.playerOrbit.removeParent();
            }
        }
    }

    public void renderTick(float partialTick) {
        this.setCurrentTime(clientTimeHandler.calculateCurrentTime(partialTick));
        this.solarSystem.UpdatePlanets(this.getCurrentTime(), this.isTimeWarping());

        if (this.screenManager.isMapScreenOpen()) {
            this.solarSystem.UpdateSpacecraft(this.getCurrentTime(), this.isTimeWarping());
        } else if (this.playerOrbit.getParent() != null) {
            this.playerOrbit.getParent().simulateSpacecraft(this.getCurrentTime(), this.isTimeWarping());
        }

        if (currentPlanetOn != null && playerOrbit.getPlayerEntity() != null) {
            this.playerOrbit.updatePlayerPosRot(currentPlanetOn);
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

    public void localPlayerJoinOrbital(OrbitId newParentID, OrbitalElements orbitalElements) {
        this.playerOrbit.setOrbitalElements(orbitalElements);
        solarSystem.entityJoinedOrbital(this.playerOrbit, newParentID);
    }

    public void entityJoinOrbital(EntityOrbitBody entityOrbitBody, OrbitId orbitParent) {
        this.solarSystem.entityJoinedOrbital(entityOrbitBody, orbitParent);
    }

    public void orbitSOIChange(OrbitId spacecraftID, OrbitId newParentID, OrbitalElements orbitalElements) {
        EntityOrbitBody entityOrbitBody = solarSystem.getSpacecraftOrbit(spacecraftID);

        if (entityOrbitBody != null) {
            solarSystem.playerChangeOrbitalSOIs(entityOrbitBody, newParentID, orbitalElements);
        }
    }

    public void orbitChange(OrbitId spacecraftID, OrbitalElements orbitalElements) {
        EntityOrbitBody entityOrbitBody = this.solarSystem.getSpacecraftOrbit(spacecraftID);
        if (entityOrbitBody != null) {
            entityOrbitBody.setOrbitalElements(orbitalElements);
        }
    }

    public void orbitRemove(OrbitId spacecraftID) {
        EntityOrbitBody entityOrbitBody = solarSystem.getSpacecraftOrbit(spacecraftID);
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