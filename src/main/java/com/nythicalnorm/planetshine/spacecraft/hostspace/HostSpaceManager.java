package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundHostOrbitSet;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitRemove;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.ServerCelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.AbstractPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.ServerPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.AbstractSpaceshipBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.ServerSpaceshipBody;
import com.nythicalnorm.planetshine.spacecraft.vs.ShipTeleporter;
import com.nythicalnorm.planetshine.storage.IDataSavable;
import com.nythicalnorm.planetshine.util.calculations.PlanetBodyCalc;
import com.nythicalnorm.planetshine.util.OrbitalBodyUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.valkyrienskies.core.api.bodies.properties.BodyKinematics;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.util.GameTickOnly;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.lang.Math;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HostSpaceManager implements IDataSavable<Map<OrbitId, Vector2ic>> {
    private final PSServer psServer;
    private final ShipTeleporter shipTeleporter;
    private final ConcurrentMap<Vector2ic, OrbitHostSpace> loadedHostSpaces;
    private final Map<OrbitId, Vector2ic> allRegisteredHostSpaces;
    private final ServerLevel spaceLevel;

    private static final int HOST_SPACE_GAP_SIZE = 16000;
    private static final int HOST_SPACE_DIAMETER = HOST_SPACE_GAP_SIZE / 2;
    private static final double teleportToGroundHeight = 1000d;
    private boolean isDirty = false;

    public HostSpaceManager(PSServer psServer, Map<OrbitId, Vector2ic> allRegisteredHostSpaces) {
        this.psServer = psServer;
        this.shipTeleporter = new ShipTeleporter(VSGameUtilsKt.getShipObjectWorld(psServer.getMCServer()));
        this.allRegisteredHostSpaces = allRegisteredHostSpaces;
        this.loadedHostSpaces = new ConcurrentHashMap<>();
        this.spaceLevel = psServer.getSpaceLevel();
    }

    public OrbitHostSpace getOrCreateHostSpace(EntityOrbitBody entityOrbitBody){
        Vector2ic hostSpaceLoc = allRegisteredHostSpaces.computeIfAbsent(entityOrbitBody.getOrbitId(), (k) -> {
            this.markDirty(true);
            return genNewHostSpaceLoc(allRegisteredHostSpaces.size());
        });

        return loadedHostSpaces.computeIfAbsent(hostSpaceLoc,
                k -> entityOrbitBody.createHostSpace(hostSpaceLoc));
    }

    @GameTickOnly
    public ShipTeleporter getShipTeleporter() {
        return shipTeleporter;
    }

    public OrbitHostSpace getHostSpaceAt(Vec3 spaceDimPos) {
        int x = (int) (Math.round(spaceDimPos.x / HOST_SPACE_GAP_SIZE) * HOST_SPACE_GAP_SIZE);
        int z = (int) (Math.round(spaceDimPos.z / HOST_SPACE_GAP_SIZE) * HOST_SPACE_GAP_SIZE);
        Vector2ic pos = new Vector2i(x,z);
        return loadedHostSpaces.get(pos);
    }

    public Vector2ic getHostSpacePos(Vec3 spaceDimPos) {
        int x = (int) (Math.round(spaceDimPos.x / HOST_SPACE_GAP_SIZE) * HOST_SPACE_GAP_SIZE);
        int z = (int) (Math.round(spaceDimPos.z / HOST_SPACE_GAP_SIZE) * HOST_SPACE_GAP_SIZE);
        return new Vector2i(x,z);
    }

    private Vector2i genNewHostSpaceLoc(int alreadyGenerated) {
        int totalXDist = 56_000_000;
        int startPosX = 0;
        int startPosZ = 0;
        int posPerXSlice = totalXDist / HOST_SPACE_GAP_SIZE;
        //alreadyGenerated = alreadyGenerated + (posPerXSlice / 2); // beginning at origin in the first slice, cause its probably less bugs at low pos.

        int x = startPosX + ((alreadyGenerated % posPerXSlice) * HOST_SPACE_GAP_SIZE);
        int z = startPosZ + ((alreadyGenerated / posPerXSlice) * HOST_SPACE_GAP_SIZE);
        return new Vector2i(x, z);
    }

    public void removeHostSpace(OrbitHostSpace orbitHostSpace) {
        loadedHostSpaces.remove(orbitHostSpace.getOriginPos2I());
    }

    public void onGameTick() {
        loadedHostSpaces.forEach((vector2ic, orbitHostSpace) -> orbitHostSpace.OnGameTick());

        checkShipTeleportToSpace();
        checkEntityTeleportToPlanet();
    }

    public void onPhysTick() {
        loadedHostSpaces.forEach((vector2ic, orbitHostSpace) -> orbitHostSpace.onPhysTick());
    }

    public void spaceEntitySpawn(Entity entity) {
        OrbitHostSpace entityHostSpace = getHostSpaceAt(entity.position());

        if (entityHostSpace == null && entity instanceof ServerPlayer player) {
            psServer.get().playerUpdatedInSpace(player);
        }

        if (entityHostSpace != null) {
            entityHostSpace.addEntityToHostSpace(entity);
        }
    }

    public void spaceEntityLeave(Entity entity) {
        OrbitHostSpace entityHostSpace = getHostSpaceAt(entity.position());
        if (entityHostSpace != null) {
            entityHostSpace.removeEntityFromHostSpace(entity);
        }
    }

    public void handleHostPlayerMove(@Nullable ServerPlayer sender, OrbitId playerBodyID, Vector3d addedVel) {
        EntityOrbitBody entityOrbitBody = psServer.getSolarSystem().getSpacecraftOrbit(playerBodyID);
        if (entityOrbitBody instanceof ServerPlayerOrbitBody playerOrbitBody && playerOrbitBody.getPlayerEntity().equals(sender)) {
            if (!psServer.isTimeWarping()) {
                playerOrbitBody.addVelocityForUpdate(addedVel);
            }
        }
    }

    public void checkShipTeleportToSpace() {
        this.shipTeleporter.teleportEntitiesFromLastTick();
        ValkyrienSkies.api().getServerShipWorld(psServer.getMCServer()).getLoadedShips().forEach(loadedServerShip -> {
            Vector3dc currentPos = loadedServerShip.getTransform().getPositionInWorld();

            if (currentPos.y() >= ShipTeleporter.TELEPORT_Y_HEIGHT && !shipTeleporter.isTeleported(loadedServerShip)) {
                ResourceKey<Level> shipDimension = VSGameUtilsKt.getResourceKey(loadedServerShip.getChunkClaimDimension());
                CelestialBody celestialBody = psServer.getSolarSystem().getDimensionOfPlanet(shipDimension);

                if (celestialBody != null) {
                    teleportShipToSpace(loadedServerShip, celestialBody);
                }
            }
        });
        this.shipTeleporter.resetTeleports();
    }

    private void teleportShipToSpace(LoadedServerShip loadedServerShip, CelestialBody celestialBody) {
        ServerLevel oldLevel = ((ServerCelestialBody)celestialBody).getLevel();
        ServerLevel newLevel = psServer.getMCServer().getLevel(Level.NETHER);

//        ShipTeleportData shipTeleportData = new ShipTeleportDataImpl(new Vector3d(0d, 250d, 0d), new Quaterniond(),
//                new Vector3d(), new Vector3d(), VSGameUtilsKt.getDimensionId(newLevel),null, null);
//
//        shipTeleporter.teleportShipsWithEntities(loadedServerShip, shipTeleportData, oldLevel, newLevel);
    }

    private void checkEntityTeleportToPlanet() {
        psServer.getSolarSystem().getAllSpacecraftBodies().values().forEach(entityOrbitBody -> {
            if (entityOrbitBody.isHostOfItsSpace() && entityOrbitBody.getAltitude(entityOrbitBody.getParent()) < teleportToGroundHeight) {
                ServerLevel planetLevel = ((ServerCelestialBody)entityOrbitBody.getParent()).getLevel();
                if (planetLevel != null) {
                    Vector2d pos = PlanetBodyCalc.vectorToPlanetDimPos(entityOrbitBody.getRelativePos(), entityOrbitBody.getParent().getRadius(), entityOrbitBody.getParent().getRotation());
                    if (entityOrbitBody instanceof AbstractPlayerOrbitBody playerOrbitBody && playerOrbitBody.getPlayerEntity() != null) {
                        teleportEntity(playerOrbitBody.getPlayerEntity(), planetLevel, pos.x, teleportToGroundHeight, pos.y);
                        psServer.getSolarSystem().entityRemoveOrbital(entityOrbitBody);
                        PacketHandler.sendToAllClients(new ClientboundOrbitRemove(playerOrbitBody.getOrbitId()));
                        PacketHandler.sendToPlayer(new ClientboundHostOrbitSet(null, null), (ServerPlayer)playerOrbitBody.getPlayerEntity());
                    }
                }
            }
        });
    }

    public void teleportEntity(Entity entity, ServerLevel level, double x, double y, double z) {
        entity.teleportTo(level, x, y, z, EnumSet.noneOf(RelativeMovement.class), -85f, 0f);
    }

    public ServerSpaceshipBody planetShipToSpaceShipBodyBuilder(LoadedServerShip ship, CelestialBody celestialBody) {
        BodyKinematics bodyKinematics = ship.getKinematics();
        AbstractSpaceshipBody.ShipOrbitBuilder builder = new AbstractSpaceshipBody.ShipOrbitBuilder();
        builder.setShip(ship);

        Vector3d relativesShipPosition = OrbitalBodyUtils.getRelativePositon(bodyKinematics.getTransform().getPosition(), celestialBody);
        builder.setRelativeOrbitalPos(relativesShipPosition);

        Quaterniond rotationDifference = OrbitalBodyUtils.getSpaceRotationFromPlanetPos(relativesShipPosition, celestialBody);
        Quaterniond shipNewRot = new Quaterniond();
        bodyKinematics.getRotation().mul(rotationDifference, shipNewRot);

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
