package com.nythicalnorm.planetshine.solarsystem;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitRemove;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.ServerCelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.planetshine.spacecraft.player.AbstractPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.AbstractSpaceshipBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.ServerSpaceshipBody;
import com.nythicalnorm.planetshine.spacecraft.vs.ShipTeleporter;
import com.nythicalnorm.planetshine.storage.IDataSavable;
import com.nythicalnorm.planetshine.util.calculations.PlanetBodyCalc;
import com.nythicalnorm.planetshine.util.OrbitalBodyUtils;
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
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl;
import org.valkyrienskies.core.internal.ShipTeleportData;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.lang.Math;
import java.util.EnumSet;
import java.util.Map;

public class HostSpaceManager implements IDataSavable<Map<OrbitId, Vector2ic>> {

    private final PSServer psServer;
    private final ShipTeleporter shipTeleporter;
    private final Map<Vector2ic, OrbitHostSpace> loadedHostSpaces;
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
        this.loadedHostSpaces = new Object2ObjectOpenHashMap<>();
        this.spaceLevel = psServer.getSpaceLevel();
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

        return loadedHostSpaces.get(pos);
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
            entityHostSpace.removeEntityFromHostSpace(entity);
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

        ShipTeleportData shipTeleportData = new ShipTeleportDataImpl(new Vector3d(0d, 250d, 0d), new Quaterniond(),
                new Vector3d(), new Vector3d(), VSGameUtilsKt.getDimensionId(newLevel),null, null);

        shipTeleporter.teleportShipsWithEntities(loadedServerShip, shipTeleportData, oldLevel, newLevel);
    }

    private void checkEntityTeleportToPlanet() {
        psServer.getSolarSystem().getAllSpacecraftBodies().values().forEach(entityOrbitBody -> {
            if (entityOrbitBody.isHostOfItsSpace() && entityOrbitBody.getAltitude(entityOrbitBody.getParent()) < teleportToGroundHeight) {
                ServerLevel planetLevel = ((ServerCelestialBody)entityOrbitBody.getParent()).getLevel();
                if (planetLevel != null) {
                    Vector2d pos = PlanetBodyCalc.vectorToPlanetDimPos(entityOrbitBody.getRelativePos(), entityOrbitBody.getParent().getRadius(), entityOrbitBody.getParent().getRotation());
                    if (entityOrbitBody instanceof AbstractPlayerOrbitBody playerOrbitBody) {
                        teleportEntity(playerOrbitBody.getPlayerEntity(), planetLevel, pos.x, teleportToGroundHeight, pos.y);
                        psServer.getSolarSystem().entityRemoveOrbital(entityOrbitBody);
                        PacketHandler.sendToAllClients(new ClientboundOrbitRemove(playerOrbitBody.getOrbitId()));
                    }
                }
            }
        });
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
