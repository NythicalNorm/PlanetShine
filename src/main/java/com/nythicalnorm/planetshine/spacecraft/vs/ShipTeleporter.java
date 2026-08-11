package com.nythicalnorm.planetshine.spacecraft.vs;

import com.nythicalnorm.planetshine.dimensions.SpaceServerLevel;
import com.nythicalnorm.planetshine.util.SpaceUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.phys.AABB;
import org.joml.*;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl;
import org.valkyrienskies.core.api.ships.ShipTeleportData;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.IEntityDraggingInformationProvider;

import java.util.*;

public class ShipTeleporter {
    public static final double shipExtraRange = 20;
    private final VsiServerShipWorld serverShipWorld;
    private final List<Long> alreadyTeleported;
    private final Queue<ShipToTeleport> shipTeleportDataQueue;
    private final Queue<Entity> shipyardEntityTeleportQueue;
    private final Queue<EntityToTeleport> entityTeleportDataQueue;

    public ShipTeleporter(VsiServerShipWorld serverShipWorld) {
        this.serverShipWorld = serverShipWorld;
        this.alreadyTeleported = new ArrayList<>();
        this.shipTeleportDataQueue = new ArrayDeque<>();
        this.shipyardEntityTeleportQueue = new ArrayDeque<>();
        this.entityTeleportDataQueue = new ArrayDeque<>();
    }

    public void teleportShipsWithEntities(
            LoadedServerShip serverShip,
            ShipTeleportData shipTeleportData,
            ServerLevel levelOld,
            ServerLevel levelNew,
            boolean teleportNearbyShips
    ) {
        AABBdc shipWorldAABB = serverShip.getWorldAABB();

        AABBdc shipAABBInflated = new AABBd(shipWorldAABB.minX() - shipExtraRange, shipWorldAABB.minY() - shipExtraRange,
                shipWorldAABB.minZ() - shipExtraRange, shipWorldAABB.maxX() + shipExtraRange,
                shipWorldAABB.maxY() + shipExtraRange, shipWorldAABB.maxZ() + shipExtraRange);

        AABB entityAABB = new AABB(shipWorldAABB.minX(), shipWorldAABB.minY(), shipWorldAABB.minZ(), shipWorldAABB.maxX(),
                shipWorldAABB.maxY(), shipWorldAABB.maxZ()).inflate(5d);

        Iterable<LoadedServerShip> serverShipIterable;

        if (teleportNearbyShips) {
            serverShipIterable = serverShipWorld.getLoadedShips().getIntersecting(shipAABBInflated,
                    VSGameUtilsKt.getDimensionId(levelOld));
        } else {
            serverShipIterable = List.of(serverShip);
        }

        LoadedServerShip biggestShip = this.collectIntersectingShips(serverShipIterable, shipTeleportData);

        if (biggestShip == null) {
            return;
        }
        List<Entity> allNonPassengerEntities = levelOld.getEntities((Entity) null, entityAABB, (entity) -> !entity.isPassenger());
        this.collectEntities(allNonPassengerEntities, biggestShip.getId(), biggestShip.getTransform(), shipTeleportData, levelOld, levelNew);

        this.telepostShips();
        this.teleportShipyardEntities(levelNew);
    }

    private LoadedServerShip collectIntersectingShips(Iterable<LoadedServerShip> serverShipIterable, ShipTeleportData parentTeleportData) {
        List<LoadedServerShip> allChildShips = new ArrayList<>();
        LoadedServerShip parentShip = null;
        double maxVolumeSearched = 0d;

        for (LoadedServerShip serverShip : serverShipIterable) {
            allChildShips.add(serverShip);
            if (serverShip.getShipAABB() == null) {
                continue;
            }
            double volume = (serverShip.getShipAABB().maxX() - serverShip.getShipAABB().minX()) *
                    (serverShip.getShipAABB().maxY() - serverShip.getShipAABB().minY()) *
                    (serverShip.getShipAABB().maxZ() - serverShip.getShipAABB().minZ());
            if (volume > maxVolumeSearched) {
                maxVolumeSearched = volume;
                parentShip = serverShip;
            }
        }
        if (parentShip == null) {
            return null;
        }
        allChildShips.remove(parentShip);

        ShipTransform oldParentTransform = parentShip.getTransform();

        shipTeleportDataQueue.add(new ShipToTeleport(parentShip, parentTeleportData)); //serverShipWorld.teleportShip(parentShip, parentTeleportData);
        alreadyTeleported.add(parentShip.getId());

        for (LoadedServerShip childShip : allChildShips) {
//            Vector3d posNew = oldParentTransform.getWorldToShip().transformPosition(new Vector3d(childShip.getTransform().getPositionInWorld()));
            Vector3d posNew = transformPos(new Vector3d(childShip.getTransform().getPositionInWorld()),
                    oldParentTransform.getPositionInWorld(), oldParentTransform.getRotation(),
                    parentTeleportData.getNewPos(), parentTeleportData.getNewRot());

            Quaterniond rotNew = transformRot(new Quaterniond(childShip.getTransform().getRotation()),
                    oldParentTransform.getRotation(), parentTeleportData.getNewRot());

            ShipTeleportDataImpl childShipData = new ShipTeleportDataImpl(posNew, rotNew, new Vector3d(childShip.getVelocity()).add(parentTeleportData.getNewVel()),
                    childShip.getAngularVelocity(),
                    parentTeleportData.getNewDimension(), null, null);
            shipTeleportDataQueue.add(new ShipToTeleport(childShip, childShipData));
            alreadyTeleported.add(childShip.getId());
        }

        return parentShip;
    }

    private void collectEntities(List<Entity> entities, long ogShipId, ShipTransform oldShipTransform, ShipTeleportData newShipTransform, ServerLevel levelOld, ServerLevel levelNew) {

        for (Entity entity : entities) {
            Vector3d posNew;

            if (!VSGameUtilsKt.isBlockInShipyard(levelOld, entity.position())) {
                posNew = new Vector3d(entity.position().x, entity.position().y, entity.position().z);
                oldShipTransform.getWorldToShip().transformPosition(posNew);
                ((IEntityDraggingInformationProvider) entity).getDraggingInformation().setLastShipStoodOn(null);
                Quaterniond entityRotation = new Quaterniond().rotationX(entity.getXRot()).rotationY(entity.getYRot());
                Vector3d entityEuler = transformRot(entityRotation, oldShipTransform.getRotation(), newShipTransform.getNewRot()).getEulerAnglesXYZ(new Vector3d());
                float yRot = (float) entityEuler.y();
                float xRot = (float) entityEuler.x();

                entityTeleportDataQueue.add(new EntityToTeleport(entity, posNew.x, posNew.y, posNew.z, yRot, xRot, levelNew, ogShipId));
            } else if (!levelOld.equals(levelNew)) {
                shipyardEntityTeleportQueue.add(entity);
            }
        }
    }

    public void teleportEntitiesFromLastTick() {
        teleportEntities();
    }

    private void telepostShips() {
        ShipToTeleport shipToTeleport;

        while ((shipToTeleport = shipTeleportDataQueue.poll()) != null) {
            this.serverShipWorld.teleportShip(shipToTeleport.serverShip(), shipToTeleport.data());
        }
    }

    private void teleportEntities() {
        EntityToTeleport entityToTeleport;

        while ((entityToTeleport = entityTeleportDataQueue.poll()) != null) {
            List<Entity> passengerList = List.copyOf(entityToTeleport.entity().getPassengers());
            Vector3d pos = new Vector3d(entityToTeleport.x(), entityToTeleport.y(), entityToTeleport.z());
            ServerShip ship = this.serverShipWorld.getAllShips().getById(entityToTeleport.shipID());
            if (ship == null) {
                return;
            }
            ship.getShipToWorld().transformPosition(pos);
            ((IEntityDraggingInformationProvider) entityToTeleport.entity()).getDraggingInformation().setLastShipStoodOn(null);

            entityToTeleport.entity().teleportTo(entityToTeleport.levelNew(), pos.x(), pos.y(), pos.z(),
                    EnumSet.noneOf(RelativeMovement.class), entityToTeleport.yRot(), entityToTeleport.xRot());

            Entity postTeleportEntity = entityToTeleport.levelNew().getEntity(entityToTeleport.entity().getUUID());

            if (postTeleportEntity != null) {
                teleportPassengers(postTeleportEntity, passengerList, entityToTeleport.levelNew());
            }
        }
    }
    private void teleportShipyardEntities(ServerLevel levelNew) {
        Entity entity;

        while ((entity = shipyardEntityTeleportQueue.poll()) != null) {
            List<Entity> passengerList = List.copyOf(entity.getPassengers());

            entity.teleportTo(levelNew, entity.position().x(), entity.position().y(), entity.position().z(),
                    EnumSet.noneOf(RelativeMovement.class), entity.getYRot(), entity.getXRot());

            Entity postTeleportEntity = levelNew.getEntity(entity.getUUID());

            if (postTeleportEntity != null) {
                teleportPassengers(postTeleportEntity, passengerList, levelNew);
            }
        }
    }

    private void teleportPassengers(Entity parentEntity, List<Entity> passengerList, ServerLevel levelNew) {
        for (Entity passenger : passengerList) {
            List<Entity> subPassengerList = List.copyOf(passenger.getPassengers());

            if (passenger.teleportTo(levelNew, parentEntity.position().x, parentEntity.position().y, parentEntity.position().z, EnumSet.noneOf(RelativeMovement.class), 0f, 0f)) {
                Entity postTeleportPassenger = levelNew.getEntity(passenger.getUUID());
                if (postTeleportPassenger == null) {
                    continue;
                }

                postTeleportPassenger.startRiding(parentEntity, true);
                this.teleportPassengers(postTeleportPassenger, subPassengerList, levelNew);
            }
        }
    }

    private static Vector3d transformPos(Vector3d pos, Vector3dc oldOriginPos, Quaterniondc oldRot, Vector3dc newOriginPos, Quaterniondc newRot) {
        pos.sub(oldOriginPos);
        oldRot.transformInverse(pos);
        newRot.transform(pos);
        pos.add(newOriginPos);
        return pos;
    }

    private static Quaterniond transformRot(Quaterniond rot, Quaterniondc oldRot,Quaterniondc newRot) {
        rot.mul(new Quaterniond(oldRot).invert());
        rot.mul(newRot);
        return rot;
    }

    public boolean isTeleported(LoadedServerShip loadedServerShip) {
        return alreadyTeleported.contains(loadedServerShip.getId());
    }

    public void resetTeleports() {
        alreadyTeleported.clear();
    }

    public void teleportShipInSpaceDim(Ship ship, Vector3d newPos, boolean isHost) {
        ShipTeleportDataImpl teleportData;

        if (isHost) {
            teleportData = new ShipTeleportDataImpl(newPos, ship.getTransform().getRotation(), new Vector3d(), ship.getAngularVelocity(),
                    SpaceUtils.getSpaceLevelString(), null, null);
        } else {
            teleportData = new ShipTeleportDataImpl(newPos, ship.getTransform().getRotation(), ship.getVelocity(), ship.getAngularVelocity(),
                    SpaceUtils.getSpaceLevelString(), null, null);
        }

        serverShipWorld.teleportShip((ServerShip) ship, teleportData);
    }

    public List<Entity> teleportInSameDimension(LoadedServerShip serverShip, ShipTeleportData shipTeleportData, SpaceServerLevel spaceLevel) {
        AABBdc shipWorldAABB = serverShip.getWorldAABB();
        Vector3dc shipPos = serverShip.getTransform().getPosition();

        AABB entityAABB = new AABB(shipWorldAABB.minX(), shipWorldAABB.minY(), shipWorldAABB.minZ(), shipWorldAABB.maxX(),
                shipWorldAABB.maxY(), shipWorldAABB.maxZ()).inflate(5d);
        List<Entity> allEntities = spaceLevel.getEntities((Entity) null, entityAABB, (entity) -> true);

        for (Entity entity : allEntities) {
            if (!VSGameUtilsKt.isBlockInShipyard(spaceLevel, entity.position())) {
                Vector3d posNew;
                posNew = new Vector3d(entity.position().x - shipPos.x(),
                        entity.position().y - shipPos.y(),
                        entity.position().z - shipPos.z());

                ((IEntityDraggingInformationProvider) entity).getDraggingInformation().setLastShipStoodOn(null);
                posNew.add(shipTeleportData.getNewPos());
                entity.teleportTo(posNew.x, posNew.y, posNew.z);
            }
        }

        this.serverShipWorld.teleportShip(serverShip, shipTeleportData);
        return allEntities;
    }

    private record ShipToTeleport(LoadedServerShip serverShip, ShipTeleportData data) {}

    private record EntityToTeleport(Entity entity, double x, double y, double z, float yRot, float xRot, ServerLevel levelNew, long shipID) {
    }
}
