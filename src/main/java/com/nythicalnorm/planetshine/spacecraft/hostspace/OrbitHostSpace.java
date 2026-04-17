package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitSOIChange;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.AbstractPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.ServerPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.AbstractSpaceshipBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.ServerSpaceshipBody;
import com.nythicalnorm.planetshine.util.calculations.MiscCalc;
import com.nythicalnorm.planetshine.util.calculations.DayNightCycleCalc;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.util.GameTickOnly;
import org.valkyrienskies.core.api.util.PhysTickOnly;
import org.valkyrienskies.core.api.world.PhysLevel;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public abstract class OrbitHostSpace implements OrbitHostAccessor {
    protected final OrbitId orbitIdOfHost;
    protected final Vector2ic originPos;
    protected final EntityOrbitBody<?> hostBody;

    @GameTickOnly
    protected final ConcurrentLinkedQueue<Entity> nonHostEntities;
    protected final ConcurrentLinkedQueue<ServerPlayerOrbitBody> playerOrbitBodies;

    protected final ConcurrentLinkedQueue<Vector3dc> velocityForLastGameTick;

    protected float sunOcclusion;

    public OrbitHostSpace(OrbitId orbitIdOfHost, Vector2ic originPos, EntityOrbitBody<?> entityOrbitBody) {
        this.orbitIdOfHost = orbitIdOfHost;
        this.originPos = originPos;
        this.nonHostEntities = new ConcurrentLinkedQueue<>();
        this.playerOrbitBodies = new ConcurrentLinkedQueue<>();
        this.hostBody = entityOrbitBody;
        this.velocityForLastGameTick = new ConcurrentLinkedQueue<>();
    }

    @Override
    public EntityOrbitBody<?> getHostBody() {
        return hostBody;
    }

    @Override
    public Vector3d getOriginPos() {
        return new Vector3d(originPos.x(), 128, originPos.y());
    }

    @Override
    public OrbitId getOrbitIdOfHost() {
        return orbitIdOfHost;
    }

    public float getSunOcclusion() {
        return sunOcclusion;
    }

    public void OnGameTick() {
        this.sunOcclusion = DayNightCycleCalc.getSunOcclusionForSpacecraft(this.hostBody);
        Vector3d velocity = MiscCalc.pollVectorQueue(velocityForLastGameTick);

        if (velocity.x() == 0.0d && velocity.y() == 0.0d && velocity.z() == 0.0d) {
            return;
        }

        Iterator<Entity> entityIterator = this.nonHostEntities.iterator();

        while (entityIterator.hasNext()) {
            Entity entity = entityIterator.next();
            if (PSServer.get().getHostSpaceManager().getHostSpacePos(entity.position()).equals(this.originPos.x(), this.originPos.y())
                    && !entity.isPassenger()) {
                Vec3 ogVel = entity.getDeltaMovement();
                entity.setDeltaMovement(ogVel.x - velocity.x(), ogVel.y - velocity.y(),
                        ogVel.z - velocity.z());
            } else {
                removeEntityFromHostSpace(entity);
            }
        }
    }

    @PhysTickOnly
    public void onPhysTick(PhysLevel physLevel) {
    }

    public void removeOrbitBody(EntityOrbitBody<?> entityOrbitBody, boolean isTeleporting) {
        if (entityOrbitBody.equals(this.getHostBody())) {
            this.hostLeft(isTeleporting);
            return;
        }
        if (entityOrbitBody instanceof ServerPlayerOrbitBody serverPlayerOrbitBody) {
            this.playerOrbitBodies.remove(serverPlayerOrbitBody);
        }
    }

    public void hostLeft(boolean isTeleporting) {
        PSServer.get().getHostSpaceManager().removeHostSpace(this, isTeleporting);
    }

    public Vector2ic getOriginPos2I() {
        return originPos;
    }

    public void addEntityToHostSpace(Entity entity) {
        nonHostEntities.add(entity);
    }

    public void addPlayerToHostSpace(ServerPlayerOrbitBody player) {
        if (player != null) {
            player.setHostOrbitSpace(this);
            if (! this.hostBody.getOrbitId().equals(player.getOrbitId())) {
                this.playerOrbitBodies.add(player);
            }
        }
    }

    public void addShipToHostSpace(ServerSpaceshipBody serverSpaceshipBody) {
        // weird case where a ship is added to a player host space.
    }

    public void removeEntityFromHostSpace(Entity entity) {
        nonHostEntities.remove(entity);
    }

    public void applyHostVelocity(Vector3dc addedVel) {
        this.velocityForLastGameTick.add(addedVel);
    }

    public void changeSOI(OrbitId newParent, OrbitalElementsc orbitalElements) {
        SolarSystem solarSystem = PSServer.get().getSolarSystem();
        solarSystem.entityChangeOrbitalSOIs(this.getHostBody(), newParent, orbitalElements);
        PacketHandler.sendToAllClients(new ClientboundOrbitSOIChange(this.getHostBody().getOrbitId(), newParent, orbitalElements));

        this.playerOrbitBodies.forEach(playerOrbit -> {
            OrbitalElements nonHostOrbit = new OrbitalElements(orbitalElements);
            solarSystem.entityChangeOrbitalSOIs(playerOrbit, newParent, nonHostOrbit);
            PacketHandler.sendToAllClients(new ClientboundOrbitSOIChange(playerOrbit.getOrbitId(), newParent, nonHostOrbit));
        });
    }

    public @Nullable EntityOrbitBody<?> findNewHost() {
        EntityOrbitBody<?> newHost = null;
        double distance = Double.POSITIVE_INFINITY;

        for (ServerPlayerOrbitBody orbitBody : this.playerOrbitBodies) {
            if (orbitBody.isBodyEntityLoaded() && orbitBody.getMcPosition().distance(this.getOriginPos()) < distance) {
                newHost = orbitBody;
            }
        }

        return newHost;
    }

    protected static Vector3d getHostPosDifference(OrbitHostSpace oldSpace, OrbitHostSpace newSpace, EntityOrbitBody<?> entityOrbitBody) {
        Vector3d originOffset = new Vector3d(newSpace.getOriginPos()).sub(oldSpace.getOriginPos());
        Vector3d entityBodyOffset = new Vector3d(oldSpace.getOriginPos()).sub(entityOrbitBody.getMcPosition());

        return originOffset.add(entityBodyOffset);
    }

    public void handleHostSpaceHandover(EntityOrbitBody<?> orbitBody, OrbitHostSpace newHost) {
        Vector3dc originDifference = getHostPosDifference(this, newHost, orbitBody);

        this.nonHostEntities.forEach(entity -> {
            if (!entity.isPassenger()) {
                entity.teleportTo(entity.position().x() + originDifference.x(),
                        entity.position().y() + originDifference.y(),
                        entity.position().z() + originDifference.z());
            }
            newHost.addEntityToHostSpace(entity);
        });

        this.playerOrbitBodies.forEach(playerOrbitBody -> {
            Player player = playerOrbitBody.getBody();

            if (playerOrbitBody.isBodyEntityLoaded() && !player.isPassenger()) {
                playerOrbitBody.getBody().teleportTo(player.position().x() + originDifference.x(),
                        player.position().y() + originDifference.y(),
                        player.position().z() + originDifference.z());
            }
            newHost.addPlayerToHostSpace(playerOrbitBody);
        });
    }

    public void cleanUpEntities() {
        this.nonHostEntities.forEach(entity -> {
            if (!(entity instanceof Player)) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        });
    }

    // Be careful cause you can call this on game and physics threads. so make sure you are doing proper operations on them.
    public void affectShips(Consumer<AbstractSpaceshipBody> orbitBodyConsumer) {
    }

    public void affectMCEntities(Consumer<Entity> orbitBodyConsumer) {
        for (Entity entity : this.nonHostEntities) {
            orbitBodyConsumer.accept(entity);
        }
        for (AbstractPlayerOrbitBody playerOrbitBody : this.playerOrbitBodies) {
            if (playerOrbitBody.isBodyEntityLoaded()) {
                orbitBodyConsumer.accept(playerOrbitBody.getBody());
            }
        }
    }

    @Override
    public boolean isUnloadedHostSpace() {
        return false;
    }
}
