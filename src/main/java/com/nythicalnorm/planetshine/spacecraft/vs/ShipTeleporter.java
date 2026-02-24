package com.nythicalnorm.planetshine.spacecraft.vs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.phys.AABB;
import org.joml.*;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.properties.IShipActiveChunksSet;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl;
import org.valkyrienskies.core.internal.ShipTeleportData;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.IEntityDraggingInformationProvider;

import java.util.*;

public class ShipTeleporter {
    public static final int TELEPORT_Y_HEIGHT = 500;
    public static final double shipExtraRange = 20;
    private final VsiServerShipWorld serverShipWorld;
    private final List<Long> alreadyTeleported;
    private final Queue<ShipToTeleport> shipTeleportDataQueue;

    public ShipTeleporter(VsiServerShipWorld serverShipWorld) {
        this.serverShipWorld = serverShipWorld;
        this.alreadyTeleported = new ArrayList<>();
        this.shipTeleportDataQueue = new ArrayDeque<>();
    }

    public void teleportShipsWithEntities(LoadedServerShip serverShip, ShipTeleportData shipTeleportData, ServerLevel levelOld, ServerLevel levelNew) {
        AABBdc shipWorldAABB = serverShip.getWorldAABB();
        AABBdc shipWorldInflated = new AABBd(shipWorldAABB.minX() - shipExtraRange, shipWorldAABB.minY() - shipExtraRange, shipWorldAABB.minZ() - shipExtraRange,
                shipWorldAABB.maxX() + shipExtraRange, shipWorldAABB.maxY() + shipExtraRange, shipWorldAABB.maxZ() + shipExtraRange);

        AABB entityAABB = new AABB(shipWorldAABB.minX(), shipWorldAABB.minY(), shipWorldAABB.minZ(), shipWorldAABB.maxX(), shipWorldAABB.maxY(), shipWorldAABB.maxZ()).inflate(5d);
        List<Entity> allNonPassengerEntities = levelOld.getEntities((Entity) null, entityAABB, (entity) -> !entity.isPassenger());

        ShipTransform oldShipTransform = teleportIntersectingShips(serverShipWorld.getLoadedShips().getIntersecting(shipWorldInflated, VSGameUtilsKt.getDimensionId(levelOld)),
                shipTeleportData, levelOld, levelNew);

        if (oldShipTransform == null) {
            return;
        }

        this.teleportEntities(allNonPassengerEntities, oldShipTransform, shipTeleportData, levelOld, levelNew);
    }

    private ShipTransform teleportIntersectingShips(Iterable<LoadedServerShip> serverShipIterable, ShipTeleportData parentTeleportData, ServerLevel levelOld, ServerLevel levelNew) {
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

        shipTeleportDataQueue.add(new ShipToTeleport(parentShip, parentTeleportData, levelOld, levelNew)); //serverShipWorld.teleportShip(parentShip, parentTeleportData);
        alreadyTeleported.add(parentShip.getId());

        for (LoadedServerShip childShip : allChildShips) {
            Vector3d posNew = transformPos(new Vector3d(childShip.getTransform().getPositionInWorld()),
                    oldParentTransform.getPositionInWorld(), oldParentTransform.getRotation(),
                    parentTeleportData.getNewPos(), parentTeleportData.getNewRot());

            Quaterniond rotNew = transformRot(new Quaterniond(childShip.getTransform().getRotation()),
                    oldParentTransform.getRotation(), parentTeleportData.getNewRot());

            ShipTeleportDataImpl childShipData = new ShipTeleportDataImpl(posNew, rotNew, new Vector3d(), childShip.getAngularVelocity(),
                    parentTeleportData.getNewDimension(), null, null);
            shipTeleportDataQueue.add(new ShipToTeleport(childShip, childShipData, levelOld, levelNew));
            alreadyTeleported.add(childShip.getId());
        }

        return oldParentTransform;
    }

    public void telepostShipsFromLastTick() {
        ShipToTeleport shipToTeleport;

        while ((shipToTeleport = shipTeleportDataQueue.poll()) != null) {
            List<Entity> allNonPassengerEntities = this.getShipYardEntities(shipToTeleport.levelOld(), shipToTeleport.serverShip());
            this.serverShipWorld.teleportShip(shipToTeleport.serverShip(), shipToTeleport.data());
            this.teleportShipyardEntities(allNonPassengerEntities, shipToTeleport.levelNew());
        }
    }

    private void teleportEntities(List<Entity> entities, ShipTransform oldShipTransform, ShipTeleportData newShipTransform, ServerLevel oldLevel,  ServerLevel levelNew) {
        for (Entity entity : entities) {
            if (VSGameUtilsKt.isBlockInShipyard(oldLevel, entity.position())) {
                continue;
            }

            ((IEntityDraggingInformationProvider) entity).getDraggingInformation().setLastShipStoodOn(null);
            Vector3d posNew = transformPos(new Vector3d(entity.position().x, entity.position().y, entity.position().z),
                    oldShipTransform.getPositionInWorld(), oldShipTransform.getRotation(), newShipTransform.getNewPos(), newShipTransform.getNewRot());

            Quaterniond entityRotation = new Quaterniond().rotationX(entity.getXRot()).rotationY(entity.getYRot());
            Vector3d entityEuler = transformRot(entityRotation, oldShipTransform.getRotation(), newShipTransform.getNewRot()).getEulerAnglesXYZ(new Vector3d());
            List<Entity> passengerList = List.copyOf(entity.getPassengers());

            float yRot = (float) entityEuler.y();
            float xRot = (float) entityEuler.x();

            if (entity.teleportTo(levelNew, posNew.x, posNew.y, posNew.z, EnumSet.noneOf(RelativeMovement.class), yRot, xRot)) {
                Entity postTeleportEntity = levelNew.getEntity(entity.getUUID());
                teleportPassengers(postTeleportEntity, passengerList, levelNew);
            }
        }
    }

    private List<Entity> getShipYardEntities(ServerLevel level, ServerShip ship) {
        IShipActiveChunksSet iShipActiveChunksSet = ship.getActiveChunksSet();
        Vector3i minChunks = new Vector3i();
        Vector3i maxChunks = new Vector3i();

        iShipActiveChunksSet.getMinMaxWorldPos(minChunks, maxChunks, VSGameUtilsKt.getYRange(level));
        AABB entitySearchAABB = new AABB(minChunks.x, minChunks.y, minChunks.z, maxChunks.x, maxChunks.y, maxChunks.z);
        return level.getEntities((Entity) null, entitySearchAABB, (entity) -> !entity.isPassenger());
    }

    private void teleportShipyardEntities(List<Entity> entities, ServerLevel levelNew) {
        for (Entity entity : entities) {
            List<Entity> passengerList = List.copyOf(entity.getPassengers());
            entity.teleportTo(levelNew, entity.position().x, entity.position().y, entity.position().z, EnumSet.noneOf(RelativeMovement.class), entity.getYRot(), entity.getXRot());
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

                postTeleportPassenger.startRiding(parentEntity, true);
                teleportPassengers(postTeleportPassenger, subPassengerList, levelNew);
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

    private record ShipToTeleport(LoadedServerShip serverShip, ShipTeleportData data, ServerLevel levelOld, ServerLevel levelNew) {}
}
