package com.nythicalnorm.voxelspaceprogram.solarsystem;

import com.nythicalnorm.voxelspaceprogram.PSServer;
import com.nythicalnorm.voxelspaceprogram.network.PacketHandler;
import com.nythicalnorm.voxelspaceprogram.network.orbitaldata.ClientboundOrbitRemove;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.voxelspaceprogram.solarsystem.bodies.ServerCelestialBody;
import com.nythicalnorm.voxelspaceprogram.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.voxelspaceprogram.spacecraft.EntityOrbitBody;
import com.nythicalnorm.voxelspaceprogram.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.voxelspaceprogram.spacecraft.player.AbstractPlayerOrbitBody;
import com.nythicalnorm.voxelspaceprogram.spacecraft.spaceship.AbstractSpaceshipBody;
import com.nythicalnorm.voxelspaceprogram.spacecraft.spaceship.ServerSpaceshipBody;
import com.nythicalnorm.voxelspaceprogram.spacecraft.vs.ShipTeleporter;
import com.nythicalnorm.voxelspaceprogram.storage.IDataSavable;
import com.nythicalnorm.voxelspaceprogram.util.calculations.PlanetBodyCalc;
import com.nythicalnorm.voxelspaceprogram.util.OrbitalBodyUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.valkyrienskies.core.api.bodies.properties.BodyKinematics;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.lang.Math;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class HostSpaceManager implements IDataSavable<Map<OrbitId, Vector2ic>> {

    private final PSServer psServer;
    private final ShipTeleporter shipTeleporter;
    private final Map<Vector2ic, OrbitHostSpace> loadedHostSpaces;
    private final Map<OrbitId, Vector2ic> allRegisteredHostSpaces;

    private static final int HOST_SPACE_GAP_SIZE = 16000;
    private static final int HOST_SPACE_DIAMETER = HOST_SPACE_GAP_SIZE / 2;
    private static final double teleportToGroundHeight = 1000d;
    private boolean isDirty = false;

    public HostSpaceManager(PSServer psServer, Map<OrbitId, Vector2ic> allRegisteredHostSpaces) {
        this.psServer = psServer;
        shipTeleporter = new ShipTeleporter(psServer.getSpaceLevel(), this);
        this.allRegisteredHostSpaces = allRegisteredHostSpaces;
        this.loadedHostSpaces = new Object2ObjectOpenHashMap<>();
    }

    public OrbitHostSpace getOrCreateHostSpace(EntityOrbitBody entityOrbitBody){
        Vector2ic hostSpaceLoc = allRegisteredHostSpaces.computeIfAbsent(entityOrbitBody.getOrbitId(), (k) -> {
            this.markDirty(true);
            return genNewHostSpaceLoc(allRegisteredHostSpaces.size());
        });

        Vector3d posNew = new Vector3d(hostSpaceLoc.x(), 128, hostSpaceLoc.y());

        return loadedHostSpaces.computeIfAbsent(hostSpaceLoc,
                k -> entityOrbitBody.createHostSpace(posNew));
    }

    private OrbitHostSpace getHostSpaceAt(Vec3 spaceDimPos) {
        int x = (int) (Math.round(spaceDimPos.x / HOST_SPACE_GAP_SIZE) * HOST_SPACE_GAP_SIZE);
        int z = (int) (Math.round(spaceDimPos.z / HOST_SPACE_GAP_SIZE) * HOST_SPACE_GAP_SIZE);
        Vector2ic pos = new Vector2i(x,z);
        OrbitHostSpace hostSpace = loadedHostSpaces.get(pos);

        return hostSpace;
    }

    private Vector2i genNewHostSpaceLoc(int alreadyGenerated) {
        int totalXDist = 56_000_000;
        int startPosX = -28_000_000;
        int startPosZ = -10_000_000;
        int posPerXSlice = totalXDist / HOST_SPACE_GAP_SIZE;
        //alreadyGenerated = alreadyGenerated + (posPerXSlice / 2); // beginning at origin in the first slice, cause its probably less bugs at low pos.

        int x = startPosX + ((alreadyGenerated % posPerXSlice) * HOST_SPACE_GAP_SIZE);
        int z = startPosZ + ((alreadyGenerated / posPerXSlice) * HOST_SPACE_GAP_SIZE);
        return new Vector2i(x, z);
    }

    public void onGameTick() {
        for (OrbitHostSpace hostSpace : loadedHostSpaces.values()) {
            hostSpace.OnGameTick();
        }

        checkShipTeleportToSpace();
        checkEntityTeleportToPlanet();
    }

    public void onPhysTick() {
        for (OrbitHostSpace hostSpace : loadedHostSpaces.values()) {
            hostSpace.onPhysTick();
        }
    }

    public void spaceEntitySpawn(Entity entity) {
        OrbitHostSpace entityHostSpace = getHostSpaceAt(entity.position());
        if (entityHostSpace == null && entity instanceof Player player) {
            AbstractPlayerOrbitBody entityOrbitBody = (AbstractPlayerOrbitBody) psServer.getSolarSystem().getSpacecraftOrbit(new OrbitId(player));
            if (entityOrbitBody != null) {
                entityOrbitBody.setPlayer(player);
                entityHostSpace = getOrCreateHostSpace(entityOrbitBody);
            }
        }

        if (entityHostSpace != null) {
            entityHostSpace.addEntityToHostSpace(entity);
        }
    }

    public void spaceEntityLeave(Entity entity) {
        OrbitHostSpace entityHostSpace = getHostSpaceAt(entity.position());
        if (entityHostSpace != null) {
            entityHostSpace.removeEntityToHostSpace(entity);
        }
    }

    public void handleHostPlayerMove(@Nullable ServerPlayer sender, OrbitId playerBodyID, Vector3d addedVel) {
        EntityOrbitBody entityOrbitBody = psServer.getSolarSystem().getSpacecraftOrbit(playerBodyID);
        if (entityOrbitBody instanceof AbstractPlayerOrbitBody playerOrbitBody && playerOrbitBody.getPlayerEntity().equals(sender)) {
            OrbitHostSpace playerHostSpace = getOrCreateHostSpace(playerOrbitBody);
            if (!psServer.isTimeWarping()) {
                playerOrbitBody.addVelocityForUpdate(addedVel);
                playerHostSpace.applyHostVelocity(addedVel);
            }
        }
    }

    public void checkShipTeleportToSpace() {
        List<LoadedServerShip> alreadyTeleported = new ArrayList<>();
        ValkyrienSkies.api().getServerShipWorld(psServer.getServer()).getLoadedShips().forEach(loadedServerShip -> {
            ResourceKey<Level> shipDimension = VSGameUtilsKt.getResourceKey(loadedServerShip.getChunkClaimDimension());
            CelestialBody celestialBody = psServer.getSolarSystem().getDimensionOfPlanet(shipDimension);

            if (celestialBody != null) {
                Vector3dc currentPos = loadedServerShip.getTransform().getPositionInWorld();
                if (currentPos.y() >= ShipTeleporter.TELEPORT_Y_HEIGHT && !alreadyTeleported.contains(loadedServerShip)) {
                    shipTeleporter.teleportShipsAndEntities(loadedServerShip, celestialBody, alreadyTeleported, psServer.getServer());
                }
            }
        });
    }

    private void checkEntityTeleportToPlanet() {
        psServer.getSolarSystem().getAllSpacecraftBodies().values().forEach(entityOrbitBody -> {
            if (entityOrbitBody.isHostOfItsSpace() && entityOrbitBody.getAltitude() < teleportToGroundHeight) {
                ServerLevel planetLevel = ((ServerCelestialBody)entityOrbitBody.getParent()).getLevel();
                if (planetLevel != null) {
                    Vector2d pos = PlanetBodyCalc.vectorToPlanetDimPos(entityOrbitBody.getRelativePos(), entityOrbitBody.getParent().getRadius(), entityOrbitBody.getParent().getRotation());
                    if (entityOrbitBody instanceof AbstractPlayerOrbitBody playerOrbitBody) {
                        teleportEntity(playerOrbitBody.getPlayerEntity(), planetLevel, pos);
                        psServer.getSolarSystem().entityRemoveOrbital(entityOrbitBody);
                        PacketHandler.sendToAllClients(new ClientboundOrbitRemove(playerOrbitBody.getOrbitId()));
                    }
                }
            }
        });
    }

    public void teleportEntity(Entity entity, ServerLevel level, Vector3d position) {
        teleportEntity(entity, level, position.x, position.y, position.z);
    }

    public void teleportEntity(Entity entity, ServerLevel level, Vector2d position) {
        teleportEntity(entity, level, position.x, 1000d, position.y);
    }

    public void teleportEntity(Entity entity, ServerLevel level, double x, double y, double z) {
        entity.teleportTo(level, x, y, z, EnumSet.noneOf(RelativeMovement.class), -85f, 0f);
    }

    public ServerSpaceshipBody planetShipToSpaceShipBodyBuilder(ServerShip ship, CelestialBody celestialBody) {
        BodyKinematics bodyKinematics = ship.getKinematics();
        AbstractSpaceshipBody.ShipOrbitBuilder builder = new AbstractSpaceshipBody.ShipOrbitBuilder();
        builder.setShip(ship);

        Vector3d relativesShipPosition = OrbitalBodyUtils.getRelativePositon(bodyKinematics.getTransform().getPosition(), celestialBody);
        builder.setRelativeOrbitalPos(relativesShipPosition);

        Quaterniond rotationDifference = OrbitalBodyUtils.getSpaceRotationFromPlanetPos(relativesShipPosition, celestialBody);
        Quaterniond shipNewRot = new Quaterniond();
        bodyKinematics.getRotation().mul(rotationDifference, shipNewRot);
        builder.setRotation(shipNewRot);
        // need to take into account the planets rotational velocity that is also transferred to the ship, earth moving at 1000 m/s at the equator etc...
        Vector3d velocity = new Vector3d(ship.getVelocity()).rotate(rotationDifference);
        builder.setRelativeVelocity(velocity);
        Vector3d absoluteShipPosition = new Vector3d(celestialBody.getAbsolutePos()).add(relativesShipPosition);
        builder.setAbsoluteOrbitalPos(absoluteShipPosition);

        builder.setParent(celestialBody);
        builder.setStableOrbit(false);
        builder.setOrbitalElements(new OrbitalElements(relativesShipPosition, velocity, psServer.getCurrentTime(), celestialBody.getMass()));

        return (ServerSpaceshipBody) builder.build();
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public void markDirty(boolean state) {
        this.isDirty = state;
    }

    @Override
    public Map<OrbitId, Vector2ic> getDataToSave() {
        return allRegisteredHostSpaces;
    }
}
