package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.mixinducks.CelestialBodyAccessor;
import com.nythicalnorm.planetshine.storage.PlanetShineConfig;
import com.nythicalnorm.planetshine.util.calculations.MiscCalc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBdc;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl;
import org.valkyrienskies.core.internal.ShipTeleportData;
import org.valkyrienskies.core.internal.world.VsiServerShipWorld;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShipCrashHandler {
    private final PSServer psServer;
    private final List<ShipToCrash> shipToCrashList = new ArrayList<>();
    private final List<CrashedShip> alreadyCrashed = new ArrayList<>();

    public ShipCrashHandler(PSServer psServer) {
        this.psServer = psServer;
    }

    public void handleUpcomingCrash(Ship ship, ServerLevel level, Vector3d velocity) {
        shipToCrashList.add(new ShipToCrash(ship.getId(), level.dimension(), velocity));
    }

    public void onGameTick() {
        this.shipToCrashList.removeIf(this::checkShipToCrash);
        this.alreadyCrashed.removeIf(this::checkAlreadyCrashed);

        for(CrashedShip crashedShip : this.alreadyCrashed) {
            crashedShip.setTicksTaken(crashedShip.getTicksTaken() + 1);
        }
    }

    private boolean checkShipToCrash(ShipToCrash crashingShip) {
        ServerLevel level = psServer.getMCServer().getLevel(crashingShip.dimension());
        VsiServerShipWorld serverShipWorld = VSGameUtilsKt.getShipObjectWorld(level);
        LoadedServerShip loadedServerShip = serverShipWorld.getLoadedShips().getById(crashingShip.id());
        if (loadedServerShip == null || level == null) {
            return false;
        }

        Vector3dc shipPos = loadedServerShip.getKinematics().getPosition();
        String dimensionID = VSGameUtilsKt.getDimensionId(level);

        if (loadedServerShip.getChunkClaimDimension().equals(dimensionID) &&
                level.hasChunkAt((int) shipPos.x(), (int) shipPos.z())) {
            if (crashingShip.velocity().length() > PlanetShineConfig.getSpeedForShipCrash()) {
                this.alreadyCrashed.add(new CrashedShip(crashingShip));
                return true;
            }

            ShipTeleportData shipTeleportData = new ShipTeleportDataImpl(
                    shipPos,
                    loadedServerShip.getKinematics().getRotation(),
                    crashingShip.velocity(),
                    loadedServerShip.getKinematics().getAngularVelocity(),
                    dimensionID,
                    null,
                    null
            );

            serverShipWorld.teleportShip(loadedServerShip, shipTeleportData);
            return true;
        }
        return false;
    }

    private boolean checkAlreadyCrashed(CrashedShip crashedShip) {
        ServerLevel level = psServer.getMCServer().getLevel(crashedShip.getShip().dimension());
        VsiServerShipWorld serverShipWorld = VSGameUtilsKt.getShipObjectWorld(level);
        LoadedServerShip loadedServerShip = serverShipWorld.getLoadedShips().getById(crashedShip.getShip().id());
        if (loadedServerShip == null || level == null) {
            return false;
        }

        Vector3dc shipPos = loadedServerShip.getKinematics().getPosition();
        int chunkX = SectionPos.blockToSectionCoord((int) Math.round(shipPos.x()));
        int chunkZ = SectionPos.blockToSectionCoord((int) Math.round(shipPos.z()));

        if (crashedShip.getTicksTaken() >= 2 && level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) != null) {
            this.crashThisShip(level, loadedServerShip, serverShipWorld, crashedShip.getShip().velocity());
            return true;
        }
        return false;
    }

    // hell yeah!!
    private void crashThisShip(ServerLevel level, LoadedServerShip loadedServerShip, VsiServerShipWorld serverShipWorld, Vector3d velocity) {
        double impactVelocity = velocity.length();
        int shipLength = Math.toIntExact(Math.round(Math.cbrt(MiscCalc.getShipVolume(loadedServerShip))));
        AABBic shipAABBic = loadedServerShip.getShipAABB();
        //serverShipWorld.deleteShip(loadedServerShip);

        AABBdc shipWorldAABB = loadedServerShip.getWorldAABB();
        AABB entityAABB = new AABB(shipWorldAABB.minX(), shipWorldAABB.minY(), shipWorldAABB.minZ(), shipWorldAABB.maxX(),
                shipWorldAABB.maxY(), shipWorldAABB.maxZ()).inflate(5d);
        List<Entity> allEntities = level.getEntities((Entity) null, entityAABB, entity -> true);

        for (Entity entity : allEntities) {
            if (!(entity instanceof Player) && entity.isVehicle()) {
                entity.kill();
            } else {
                doEntityCrashDamage(entity, impactVelocity);
            }
        }

        for (int i = 0; i < shipLength; i++) {
            int randomX = level.random.nextIntBetweenInclusive(shipAABBic.minX(), shipAABBic.maxX());
            int randomY = level.random.nextIntBetweenInclusive(shipAABBic.minY(), shipAABBic.maxY());
            int randomZ = level.random.nextIntBetweenInclusive(shipAABBic.minZ(), shipAABBic.maxZ());
            Vector3d randomPosInWorld = ValkyrienSkies.positionToWorld(loadedServerShip, new Vector3d(randomX, randomY, randomZ));

            level.explode(
                    null,
                    level.damageSources().magic(),
                    new ShipCrashExplosionCalculater(impactVelocity),
                    randomPosInWorld.x(), randomPosInWorld.y(), randomPosInWorld.z(),
                    (float) Math.min(impactVelocity / 100.0f, 32.0f),
                    false,
                    Level.ExplosionInteraction.TNT,
                    true
            );
        }

        if (loadedServerShip.getSlug() != null) {
            this.sendShipCrashMessage(Component.literal(loadedServerShip.getSlug()), level);
        }
    }

    public void doEntityCrashDamage(Entity entity, double impactVelocity) {
        if (entity != null) {
            float halfDamage = (float) (impactVelocity - 3.0f) * 0.5f;
            entity.hurt(entity.level().damageSources().fall(), halfDamage);
            entity.hurt(entity.level().damageSources().inFire(), halfDamage);
        }
    }

    public Vector3d getCrashTeleportPos(ServerLevel planetLevel, Vector3d shipPos) {
        int chunkX = SectionPos.blockToSectionCoord((int) Math.round(shipPos.x()));
        int chunkZ = SectionPos.blockToSectionCoord((int) Math.round(shipPos.z()));

        ChunkAccess chunkAccess = planetLevel.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);

        int yHeight = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE, (int) shipPos.x() & 15, (int) shipPos.z() & 15);

        return new Vector3d(shipPos.x(), yHeight, shipPos.z());
    }

    public void sendShipCrashMessage(Component shipName, ServerLevel crashLevel) {
        if (crashLevel instanceof CelestialBodyAccessor celestialBodyAccessor && celestialBodyAccessor.ps$isPlanet() && shipName != null) {
            crashLevel.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                    "planetshine.ui.ship_crash_message",
                    shipName,
                    celestialBodyAccessor.ps$getCelestialBody().getDisplayName()),
                    false
            );
        }
    }

    public static class ShipCrashExplosionCalculater extends ExplosionDamageCalculator {
        private final double impactVelocity;

        public ShipCrashExplosionCalculater(double impactVelocity) {
            this.impactVelocity = impactVelocity;
        }

        @Override // this kinda crap calculation, could make it better probably
        public @NotNull Optional<Float> getBlockExplosionResistance(@NotNull Explosion pExplosion, @NotNull BlockGetter pReader, @NotNull BlockPos pPos, BlockState pState, @NotNull FluidState pFluid) {
            if (pState.isAir() && pFluid.isEmpty()) {
                return Optional.empty();
            }

            float ogBlockResistance = Math.max(pState.getExplosionResistance(pReader, pPos, pExplosion), pFluid.getExplosionResistance(pReader, pPos, pExplosion));
            if ((this.impactVelocity * 0.25d) > ogBlockResistance) {
                return Optional.of(0.5f);
            } else {
                return Optional.of(ogBlockResistance);
            }
        }
    }

    private record ShipToCrash(long id, ResourceKey<Level> dimension, Vector3d velocity) {}

    private static class CrashedShip {
        private final ShipToCrash crashingShip;
        private int ticksTaken;

        public CrashedShip(ShipToCrash crashingShip) {
            this.crashingShip = crashingShip;
            this.ticksTaken = 0;
        }

        public ShipToCrash getShip() {
            return crashingShip;
        }

        public int getTicksTaken() {
            return ticksTaken;
        }

        public void setTicksTaken(int ticksTaken) {
            this.ticksTaken = ticksTaken;
        }
    }
}
