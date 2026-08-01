package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitSOIChange;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.PlayerOrbitAccessor;
import com.nythicalnorm.planetshine.spacecraft.player.ServerPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.AbstractSpaceshipBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.ServerSpaceshipBody;
import com.nythicalnorm.planetshine.spacecraft.vs.ShipTeleporter;
import com.nythicalnorm.planetshine.util.calculations.MiscCalc;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl;
import org.valkyrienskies.core.internal.ShipTeleportData;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class ShipHostSpace extends OrbitHostSpace {
    protected final ConcurrentLinkedQueue<Vector3dc> velocityForLastPhysTick;
    protected final ConcurrentLinkedQueue<ServerSpaceshipBody> nonHostShips;

    public ShipHostSpace(OrbitId orbitIdOfHost, Vector2ic originPos, EntityOrbitBody<?> entityOrbitBody) {
        super(orbitIdOfHost, originPos, entityOrbitBody);
        this.velocityForLastPhysTick = new ConcurrentLinkedQueue<>();
        this.nonHostShips = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void onPhysTick(PhysLevel world) {
        Vector3d velocity = MiscCalc.pollVectorQueue(this.velocityForLastPhysTick);
        Iterator<ServerSpaceshipBody> shipIterator = this.nonHostShips.iterator();
        if (velocity.x() == 0.0d && velocity.y() == 0.0d && velocity.z() == 0.0d) {
            return;
        }

        while (shipIterator.hasNext()) {
            ServerSpaceshipBody spaceshipBody = shipIterator.next();
            if (spaceshipBody.getBody() == null) {
                continue;
            }

            PhysShip physShip = world.getShipById(spaceshipBody.getBody().getId());
            if (physShip == null) {
                continue;
            }

            Vector3dc shipPos = physShip.getKinematics().getPosition();

            if (PSServer.get().getHostSpaceManager().getHostSpacePos(shipPos.x(), shipPos.z()).equals(this.originPos.x(), this.originPos.y()))
            {
                physShip.applyWorldForce(new Vector3d(velocity).mul(physShip.getMass() * TimeCalc.PhysTickPerSec).negate(), shipPos);
            }
        }
    }

    @Override
    public void OnGameTick() {
        super.OnGameTick();
        Iterator<ServerSpaceshipBody> shipIterator = this.nonHostShips.iterator();
        double maxDistToCenter = this.getMaxDistToHostCenter();

        while (shipIterator.hasNext()) {
            ServerSpaceshipBody spaceshipBody = shipIterator.next();
            if (spaceshipBody.getBody() == null) {
                continue;
            }

            Vector3dc shipPos = spaceshipBody.getBody().getTransform().getPosition();
            if (this.getOriginPos().distance(shipPos) > maxDistToCenter) {
                shipOrbitBodyLeft(spaceshipBody);
            }
        }

    }

    public void shipOrbitBodyLeft(ServerSpaceshipBody spaceshipBody) {
        OrbitHostSpace newPlayerHost = PSServer.get().getHostSpaceManager().getOrCreateHostSpace(spaceshipBody);
        Vector3dc originDifference = getHostPosDifference(this, newPlayerHost, spaceshipBody);
        LoadedServerShip serverShip = (LoadedServerShip)spaceshipBody.getBody();
        HostSpaceManager hostSpaceManager = PSServer.get().getHostSpaceManager();

        if (serverShip != null) {
            Vector3d newShipPos = new Vector3d(
                    serverShip.getTransform().getPosition().x() + originDifference.x(),
                    serverShip.getTransform().getPosition().y() + originDifference.y(),
                    serverShip.getTransform().getPosition().z() + originDifference.z()
            );

            ShipTeleportData shipTeleportData = new ShipTeleportDataImpl(
                    newShipPos,
                    serverShip.getKinematics().getRotation(),
                    new Vector3d(),
                    serverShip.getKinematics().getAngularVelocity(),
                    VSGameUtilsKt.getDimensionId(hostSpaceManager.getSpaceLevel()),
                    null,
                    null
            );

            List<Entity> entityList = hostSpaceManager.getShipTeleporter().teleportInSameDimension(
                    serverShip,
                    shipTeleportData,
                    hostSpaceManager.getSpaceLevel());

            for (Entity entity : entityList) {
                if (entity instanceof PlayerOrbitAccessor playerOrbitAccessor) {
                    if (playerOrbitAccessor.getOrbitalBody() != null) {
                        this.playerOrbitBodies.removeIf(playerOrbitBody ->
                                playerOrbitAccessor.getOrbitalBody().equals(playerOrbitBody));
                        newPlayerHost.addPlayerToHostSpace((ServerPlayerOrbitBody) playerOrbitAccessor.getOrbitalBody());
                    }
                } else {
                    this.nonHostEntities.removeIf(nonHostEntity -> nonHostEntity.equals(entity));
                    newPlayerHost.addEntityToHostSpace(entity);
                }
            }
        }
        this.nonHostShips.removeIf(ship -> ship.getOrbitId() == spaceshipBody.getOrbitId());
    }

    @Override
    public void addShipToHostSpace(ServerSpaceshipBody ship) {
        if (ship != null) {
            ship.setHostOrbitSpace(this);
            if (! this.hostBody.getOrbitId().equals(ship.getOrbitId())) {
                this.nonHostShips.add(ship);
            }
        }
    }

    @Override
    public void applyHostVelocity(Vector3dc addedVel) {
        super.applyHostVelocity(addedVel);
        this.velocityForLastPhysTick.add(addedVel);
    }

    @Override
    public void changeSOI(OrbitId newParent, OrbitalElementsc orbitalElements) {
        super.changeSOI(newParent, orbitalElements);
        SolarSystem solarSystem = PSServer.get().getSolarSystem();

        this.nonHostShips.forEach(spaceshipBody -> {
            OrbitalElements nonHostOrbit = new OrbitalElements(orbitalElements);
            solarSystem.entityChangeOrbitalSOIs(spaceshipBody, newParent, nonHostOrbit);
            PacketHandler.sendToAllClients(new ClientboundOrbitSOIChange(spaceshipBody.getOrbitId(), newParent, nonHostOrbit));
        });
    }

    @Override
    public void removeOrbitBody(EntityOrbitBody<?> entityOrbitBody, boolean isTeleporting) {
        super.removeOrbitBody(entityOrbitBody, isTeleporting);
        if (entityOrbitBody instanceof ServerSpaceshipBody spaceshipBody) {
            this.nonHostShips.remove(spaceshipBody);
        }
    }

    @Override
    protected double getMaxDistToHostCenter() {
        double shipSize = 0.0d;
        if (this.getHostBody().isBodyEntityLoaded()) {
            shipSize = MiscCalc.getShipMaxLength((Ship)this.hostBody.getBody());
        }
        return super.getMaxDistToHostCenter() + shipSize;
    }

    @Override
    public @Nullable EntityOrbitBody<?> findNewHost() {
        if (!this.nonHostShips.isEmpty()) {
            EntityOrbitBody<?> newHost = null;

            int biggestVolumeFound = 0;

            for (ServerSpaceshipBody spaceshipBody : this.nonHostShips) {
                if (spaceshipBody.isBodyEntityLoaded()) {
                    int shipVolume = MiscCalc.getShipVolume(spaceshipBody.getBody());
                    if (shipVolume > biggestVolumeFound) {
                        newHost = spaceshipBody;
                    }
                }
            }

            return newHost;
        }
        else {
            return super.findNewHost();
        }
    }

    @Override
    public void handleHostSpaceHandover(EntityOrbitBody<?> orbitBody, OrbitHostSpace newHost) {
        Vector3dc originDifference = getHostPosDifference(this, newHost, orbitBody);
        ShipTeleporter shipTeleporter = PSServer.get().getHostSpaceManager().getShipTeleporter();

        this.nonHostShips.forEach(serverSpaceshipBody -> {
            Ship ship = serverSpaceshipBody.getBody();

            if (!serverSpaceshipBody.isBodyEntityLoaded()) {// This is a part where we chack if it has joints connected somewhere i think, && !player.isPassenger()) {
                Vector3d newPos = new Vector3d(ship.getTransform().getPosition().x() + originDifference.x(),
                        ship.getTransform().getPosition().y() + originDifference.y(),
                        ship.getTransform().getPosition().z() + originDifference.z());

                shipTeleporter.teleportShipInSpaceDim(ship, newPos, serverSpaceshipBody.isHostOfItsSpace());
            }
            newHost.addShipToHostSpace(serverSpaceshipBody);
        });
        super.handleHostSpaceHandover(orbitBody, newHost);
    }

    @Override
    public void affectShips(Consumer<AbstractSpaceshipBody> orbitBodyConsumer) {
        orbitBodyConsumer.accept((AbstractSpaceshipBody) this.hostBody);
        for (AbstractSpaceshipBody entityOrbitBody : this.nonHostShips) {
            orbitBodyConsumer.accept(entityOrbitBody);
        }
    }
}