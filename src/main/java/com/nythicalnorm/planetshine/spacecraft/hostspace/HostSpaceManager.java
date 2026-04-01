package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.dimensions.SpaceServerLevel;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundEntityBodyJoinOrbital;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundHostOrbitSet;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitRemove;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.AbstractPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.ServerPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.AbstractSpaceshipBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.ServerSpaceshipBody;
import com.nythicalnorm.planetshine.spacecraft.vs.ShipTeleporter;
import com.nythicalnorm.planetshine.storage.IDataSavable;
import com.nythicalnorm.planetshine.storage.PlanetShineConfig;
import com.nythicalnorm.planetshine.util.SpaceUtils;
import com.nythicalnorm.planetshine.util.calculations.PlanetCalc;
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
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.util.GameTickOnly;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.api.world.ShipWorld;
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl;
import org.valkyrienskies.core.internal.ShipTeleportData;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.lang.Math;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HostSpaceManager implements IDataSavable<Map<OrbitId, Vector2ic>> {
    private final PSServer psServer;
    private ShipTeleporter shipTeleporter;
    private final ConcurrentMap<Vector2ic, OrbitHostSpace> activeHostSpaces;
    private final Map<OrbitId, Vector2ic> allRegisteredHostSpaces;
    private SpaceServerLevel spaceLevel;

    private static final int HOST_SPACE_GAP_SIZE = 16000;
    private static final int HOST_SPACE_DIAMETER = HOST_SPACE_GAP_SIZE / 2;

    private boolean isDirty = false;

    public HostSpaceManager(PSServer psServer, Collection<EntityOrbitBody<?>> entityOrbitBodies, Map<OrbitId, Vector2ic> allRegisteredHostSpaces) {
        this.psServer = psServer;
        this.allRegisteredHostSpaces = allRegisteredHostSpaces;
        this.activeHostSpaces = new ConcurrentHashMap<>();
        entityOrbitBodies.forEach(entityOrbitBody -> {
            if (entityOrbitBody.isHostOfItsSpace()) {
                this.getOrCreateHostSpace(entityOrbitBody);
            }
        });
    }

    public void serverStarted() {
        this.shipTeleporter = new ShipTeleporter(VSGameUtilsKt.getShipObjectWorld(psServer.getMCServer()));
    }

    public void setSpaceLevel(SpaceServerLevel spaceLevel) {
        this.spaceLevel = spaceLevel;
    }

    //Creates a new orbit space if absent and also sets the host's orbit space to be the new one.
    public OrbitHostSpace getOrCreateHostSpace(EntityOrbitBody<?> entityOrbitBody){
        if (entityOrbitBody == null) {
            return null;
        }
        Vector2ic hostSpaceLoc = allRegisteredHostSpaces.computeIfAbsent(entityOrbitBody.getOrbitId(), (k) -> {
            this.markDirty(true);
            return genNewHostSpaceLoc(allRegisteredHostSpaces.size());
        });

        return activeHostSpaces.computeIfAbsent(hostSpaceLoc,
                k -> entityOrbitBody.createHostSpace(hostSpaceLoc));
    }

    @GameTickOnly
    public ShipTeleporter getShipTeleporter() {
        return shipTeleporter;
    }

    public @Nullable OrbitHostSpace getHostSpaceAt(Vec3 spaceDimPos) {
        double xPos = spaceDimPos.x;
        double zPos = spaceDimPos.z;

        if (VSGameUtilsKt.isBlockInShipyard(spaceLevel, spaceDimPos)) {
            Ship ship = VSGameUtilsKt.getShipManagingPos(spaceLevel, spaceDimPos);
            if (ship != null) {
                Vector3d worldPos = ship.getTransform().getShipToWorld().transformPosition(VectorConversionsMCKt.toJOML(spaceDimPos));
                xPos = worldPos.x();
                zPos = worldPos.z();
            } else {
                return null;
            }
        }

        int x = (int) (Math.round(xPos / HOST_SPACE_GAP_SIZE) * HOST_SPACE_GAP_SIZE);
        int z = (int) (Math.round(zPos / HOST_SPACE_GAP_SIZE) * HOST_SPACE_GAP_SIZE);
        Vector2ic pos = new Vector2i(x,z);
        return activeHostSpaces.get(pos);
    }

    public OrbitHostSpace getHostSpaceAt(Vector3dc spaceDimPos) {
        if (spaceDimPos == null) {
            return null;
        }
        return this.getHostSpaceAt(spaceDimPos.x(), spaceDimPos.z());
    }

    public OrbitHostSpace getHostSpaceAt(double xPos, double zPos) {
        if (VSGameUtilsKt.isBlockInShipyard(spaceLevel, xPos, 0d, zPos)) {
            ServerShip ship = VSGameUtilsKt.getShipManagingPos(this.spaceLevel,  xPos, 0d, zPos);
            if (ship != null) {
                Vector3d newPos = ship.getShipToWorld().transformPosition(new Vector3d(xPos, 0d, zPos));
                xPos = newPos.x();
                zPos = newPos.z();
            }
        }

        int x = (int) (Math.round(xPos / HOST_SPACE_GAP_SIZE) * HOST_SPACE_GAP_SIZE);
        int z = (int) (Math.round(zPos / HOST_SPACE_GAP_SIZE) * HOST_SPACE_GAP_SIZE);
        Vector2ic pos = new Vector2i(x,z);
        return activeHostSpaces.get(pos);
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

    public void onGameTick() {
        activeHostSpaces.forEach((vector2ic, orbitHostSpace) -> orbitHostSpace.OnGameTick());

        // this.checkShipTeleportToSpace();
        // checkEntityTeleportToPlanet();
        if (spaceLevel.getGameTime() % 20L == 0) {
            this.checkCleanUpVSShips(ValkyrienSkies.api().getShipWorld(spaceLevel));
        }
    }

    public void onPhysTick(PhysLevel world) {
        activeHostSpaces.forEach((vector2ic, orbitHostSpace) -> orbitHostSpace.onPhysTick(world));
    }

    public void spaceEntitySpawn(Entity entity) {
        OrbitHostSpace entityHostSpace = getHostSpaceAt(entity.position());
        if (entityHostSpace != null && !(entity instanceof Player)) {
            entityHostSpace.addEntityToHostSpace(entity);
        }
    }

    public void spaceEntityLeave(Entity entity) {
        OrbitHostSpace entityHostSpace = getHostSpaceAt(entity.position());
        if (entityHostSpace != null && !(entity instanceof Player)) {
            entityHostSpace.removeEntityFromHostSpace(entity);
        }
    }

    public void handleHostPlayerMove(ServerPlayer sender, OrbitId playerBodyID, Vector3d addedVel) {
        EntityOrbitBody<?> entityOrbitBody = psServer.getSolarSystem().getSpacecraftOrbit(playerBodyID);
        if (entityOrbitBody instanceof ServerPlayerOrbitBody playerOrbitBody && sender.equals(playerOrbitBody.getBody())) {
            if (!psServer.isTimeWarping()) {
                playerOrbitBody.addVelocityForUpdate(addedVel);
            }
        }
    }

    public void removeHostSpace(OrbitHostSpace orbitHostSpace, boolean isTeleporting) {
        this.activeHostSpaces.remove(orbitHostSpace.getOriginPos2I());
        if (isTeleporting) {
            return;
        }

        // finding if there are any orphans to deal with
        EntityOrbitBody<?> orbitBody = orbitHostSpace.findNewHost();
        if (orbitBody != null) {
            OrbitHostSpace newHost = getOrCreateHostSpace(orbitBody);
            orbitHostSpace.handleHostSpaceHandover(orbitBody, newHost);
        } else {
            // if not then destroy the entities still in host space,
            orbitHostSpace.cleanUpEntities();
        }
    }

    private void checkCleanUpVSShips(ShipWorld shipWorld) {
        psServer.getSolarSystem().getAllVSships().forEach((id, spaceshipBody) -> {
            Ship ship = shipWorld.getAllShips().getById(id);
            if (ship == null) {
                this.shipLeftSpace((ServerSpaceshipBody) spaceshipBody);
            } else if ( ! SpaceUtils.isSpaceLevel(ship.getChunkClaimDimension())) {
                this.shipLeftSpace((ServerSpaceshipBody) spaceshipBody);
            }
        });
    }

    public void checkShipTeleportToSpace() {
        this.shipTeleporter.teleportEntitiesFromLastTick();
        ValkyrienSkies.api().getServerShipWorld(psServer.getMCServer()).getLoadedShips().forEach(loadedServerShip -> {
            Vector3dc currentPos = loadedServerShip.getTransform().getPositionInWorld();

            if (currentPos.y() >= PlanetShineConfig.getTeleportToSpaceHeight() && !shipTeleporter.isTeleported(loadedServerShip)) {
                ResourceKey<Level> shipDimension = VSGameUtilsKt.getResourceKey(loadedServerShip.getChunkClaimDimension());
                CelestialBody celestialBody = psServer.getSolarSystem().getDimensionOfPlanet(shipDimension);

                if (celestialBody != null) {
                    teleportShipToSpace(loadedServerShip, celestialBody);
                }
            }
        });
        this.shipTeleporter.resetTeleports();
    }

    private void teleportShipToSpace(LoadedServerShip ship, CelestialBody celestialBody) {
        Vector3d relativeOrbitPos = PlanetCalc.getPlanetRelativePosition(ship.getTransform().getPosition(), celestialBody);
        Vector3d relativeOrbitVelocity = new Vector3d(ship.getVelocity());

        Quaterniond planetToSpace = PlanetCalc.getPlanetToSpaceRotation(ship.getTransform().getPositionInWorld(), celestialBody);
        relativeOrbitVelocity.rotate(planetToSpace);

        Quaterniond shipNewRot = ship.getTransform().getRotation().mul(planetToSpace, new Quaterniond());
        // need to take into account the planets rotational velocity that is also transferred to the ship, earth moving at 1000 m/s at the equator etc...
        // though maybe I don't add this.
        OrbitalElements orbitalElements = new OrbitalElements(relativeOrbitPos, relativeOrbitVelocity, psServer.getCurrentTime(), celestialBody.getMass());

        this.psServer.shipTeleportToOrbit(celestialBody, ship, orbitalElements, shipNewRot, ship.getAngularVelocity());
    }


    private void checkEntityTeleportToPlanet() {
        psServer.getSolarSystem().getAllSpacecraftBodies().values().forEach(entityOrbitBody -> {
            if (entityOrbitBody.isHostOfItsSpace() &&  entityOrbitBody.getOrbitalElements() != null &&
                    entityOrbitBody.getOrbitalElements().getPeriapsis() <= entityOrbitBody.getParent().getRadius() &&
                    entityOrbitBody.getAltitude() < PlanetShineConfig.getTeleportToGroundHeight()) {
                ServerLevel planetLevel = entityOrbitBody.getParent().getCelestialServerData().getServerLevel();
                if (planetLevel != null && entityOrbitBody.isHostOfItsSpace() && entityOrbitBody.isBodyEntityLoaded()) {
                    Vector2d pos = PlanetCalc.getDimensionPosition(entityOrbitBody.getRelativePos(), entityOrbitBody.getParent().getRadius(), entityOrbitBody.getParent());
                    if (entityOrbitBody instanceof ServerPlayerOrbitBody playerOrbitBody) {
                        teleportEntity(playerOrbitBody.getBody(), planetLevel, pos.x, PlanetShineConfig.getTeleportToGroundHeight(), pos.y);
                        psServer.getSolarSystem().entityRemoveOrbital(entityOrbitBody, true);
                        PacketHandler.sendToAllClients(new ClientboundOrbitRemove(playerOrbitBody.getOrbitId()));
                    } else if (entityOrbitBody instanceof ServerSpaceshipBody spaceshipBody) {
                        this.teleportShipToGround(spaceshipBody, pos, this.spaceLevel, planetLevel);
                        psServer.getSolarSystem().entityRemoveOrbital(entityOrbitBody, true);
                        PacketHandler.sendToAllClients(new ClientboundOrbitRemove(spaceshipBody.getOrbitId()));
                    }
                }
            }
        });
    }

    private void teleportShipToGround(ServerSpaceshipBody spaceshipBody, Vector2d pos, SpaceServerLevel spaceLevel, ServerLevel planetLevel) {
        if (spaceshipBody.getBody() != null) {
            BodyKinematics bodyKinematics = spaceshipBody.getBody().getKinematics();
            Vector3d planetPos = new Vector3d(pos.x, PlanetShineConfig.getTeleportToGroundHeight(), pos.y);

            Quaterniond shipToSpace = PlanetCalc.getShipSpaceToPlanetRotation(planetPos, spaceshipBody.getRelativePos(), spaceshipBody.getParent());

            // don't apply this
            // Vector3d velocity = new Vector3d(spaceshipBody.getRelativeVelocity()).rotate(shipNewRot);

            ShipTeleportData shipTeleportData = new ShipTeleportDataImpl(planetPos, shipToSpace, new Vector3d(),
                    bodyKinematics.getAngularVelocity(), VSGameUtilsKt.getDimensionId(planetLevel), null, null);

            this.getShipTeleporter().teleportShipsWithEntities((LoadedServerShip) spaceshipBody.getBody(),
                    shipTeleportData, spaceLevel, planetLevel);
        }
    }

    public void teleportEntity(Entity entity, ServerLevel level, double x, double y, double z) {
        entity.teleportTo(level, x, y, z, EnumSet.noneOf(RelativeMovement.class), -85f, 0f);
    }

    // called when a new player spawns in (before playerJoined function), also called when the player teleports in or to the space dimension
    // Note - make sure to test that we don't have a stale reference to a server player if the player disconnects during login
    public void playerAddedToSpace(ServerPlayer player) {
        ServerPlayerOrbitBody serverPlayerOrbitBody = (ServerPlayerOrbitBody) psServer.getSolarSystem().getSpacecraftOrbit(new OrbitId(player));
        if (serverPlayerOrbitBody != null) {
            serverPlayerOrbitBody.entityLoadedInSpace(player, this);
        } else {
            OrbitHostSpace hostSpace = this.getHostSpaceAt(player.position());
            if (hostSpace == null) {
                PlanetShine.logError("entity: " + player.getName() + "shouldn't be in space without a host space.");
                return;
            }

            if (!hostSpace.getOrbitIdOfHost().equals(new OrbitId(player))) {
                AbstractPlayerOrbitBody.PlayerOrbitBuilder builder = new AbstractPlayerOrbitBody.PlayerOrbitBuilder();
                builder.setPlayer(player);
                builder.setStableOrbit(false);
                builder.setOrbitalElements(hostSpace.getHostBody().getOrbitalElements());
                builder.setParent(hostSpace.getHostBody().getParent());
                AbstractPlayerOrbitBody playerOrbitBody = builder.build();

                hostSpace.addPlayerToHostSpace((ServerPlayerOrbitBody) playerOrbitBody);

                psServer.getSolarSystem().entityJoinedOrbital(hostSpace.getHostBody().getParent(), playerOrbitBody);
                psServer.sendPacketsPlayerJoinOrbital(player, playerOrbitBody);
            }
        }
    }

    public void shipAddedToSpace(LoadedServerShip ship) {
        ServerSpaceshipBody serverSpaceshipBody = (ServerSpaceshipBody) psServer.getSolarSystem().getSpaceshipFromVSId(ship.getId());

        if (serverSpaceshipBody != null) {
            serverSpaceshipBody.entityLoadedInSpace(ship, this);
        } else {
            OrbitHostSpace hostSpace = this.getHostSpaceAt(ship.getTransform().getPosition());
            if (hostSpace == null) {
                PlanetShine.logError("entity: " + ship.getSlug() + "shouldn't be in space without a host space.");
                return;
            }

            if (!hostSpace.getOrbitIdOfHost().equals(new OrbitId(ship.getId()))) {
                AbstractSpaceshipBody.ShipOrbitBuilder builder = new AbstractSpaceshipBody.ShipOrbitBuilder();
                builder.setShip(ship);
                builder.setStableOrbit(false);
                builder.setOrbitalElements(hostSpace.getHostBody().getOrbitalElements());
                builder.setParent(hostSpace.getHostBody().getParent());
                AbstractSpaceshipBody spaceshipBody = builder.build();

                hostSpace.addShipToHostSpace((ServerSpaceshipBody) spaceshipBody);

                psServer.getSolarSystem().entityJoinedOrbital(hostSpace.getHostBody().getParent(), spaceshipBody);
                PacketHandler.sendToAllClients(new ClientboundEntityBodyJoinOrbital(spaceshipBody));
            }
        }
    }

    public void playerLeftSpace(ServerPlayer player, Entity.RemovalReason removalReason) {
        if (removalReason.equals(Entity.RemovalReason.CHANGED_DIMENSION) || removalReason.equals(Entity.RemovalReason.DISCARDED)) {
            EntityOrbitBody<?> entitySpacecraftBody = psServer.getSolarSystem().getSpacecraftOrbit(new OrbitId(player));

            if (entitySpacecraftBody instanceof ServerPlayerOrbitBody serverPlayerSpacecraftBody) {
                psServer.getSolarSystem().entityRemoveOrbital(serverPlayerSpacecraftBody, false);

                // For some reason this don't get received on the client during this phase, so putting it on the next tick for now
                PSServer.addGameTickRunnable(() -> PacketHandler.sendToAllClients(new ClientboundOrbitRemove(entitySpacecraftBody.getOrbitId())));
                PSServer.addGameTickRunnable(() -> PacketHandler.sendToPlayer(new ClientboundHostOrbitSet(null, null), player));
            }
        }
    }

    public void shipLeftSpace (ServerSpaceshipBody spaceshipBody) {
        psServer.getSolarSystem().entityRemoveOrbital(spaceshipBody, false);
        PSServer.addGameTickRunnable(() -> PacketHandler.sendToAllClients(new ClientboundOrbitRemove(spaceshipBody.getOrbitId())));
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

    public void close() {
        //this.spaceLevel.close();
    }
}
