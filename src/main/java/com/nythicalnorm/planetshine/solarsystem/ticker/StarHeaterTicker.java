package com.nythicalnorm.planetshine.solarsystem.ticker;

import com.nythicalnorm.planetshine.dimensions.SpaceServerLevel;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.AbstractPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.spaceship.AbstractSpaceshipBody;
import com.nythicalnorm.planetshine.util.calculations.HeatCalc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.primitives.AABBic;
import org.valkyrienskies.core.api.ships.LoadedServerShip;

import java.util.ArrayList;
import java.util.List;

public class StarHeaterTicker implements CelestialBodyTicker {
    private final double heatAffectingRadius;
    private final double coreStarLuminosity;

    public StarHeaterTicker(double heatAffectingRadius, double coreStarLuminosity) {
        this.heatAffectingRadius = heatAffectingRadius;
        this.coreStarLuminosity = coreStarLuminosity;
    }

    @Override
    public void onServerTick(CelestialBody celestialBody, SolarSystem solarSystem, SpaceServerLevel spaceLevel) {
        celestialBody.getEntityChildren().forEach(entityOrbitBody -> {
            double distSquared = entityOrbitBody.getRelativePos().lengthSquared();

            if (distSquared < (heatAffectingRadius * heatAffectingRadius) && entityOrbitBody.isBodyEntityLoaded()) {
                if (entityOrbitBody instanceof AbstractPlayerOrbitBody playerOrbitBody) {

                } else if (entityOrbitBody instanceof AbstractSpaceshipBody spaceshipBody) {
                    affectHeatOnShip((LoadedServerShip) spaceshipBody.getShip(), entityOrbitBody, Math.sqrt(distSquared), spaceLevel, solarSystem);
                }
            }
        });
    }

    private void affectHeatOnShip(LoadedServerShip ship, EntityOrbitBody entityOrbitBody, double distanceFromCenter, SpaceServerLevel spaceServerLevel, SolarSystem solarSystem) {
        double tempOfShip = HeatCalc.getTemperatureInSpaceFromStar(coreStarLuminosity, entityOrbitBody.getAltitude(), 0.4f);
        int rayCount = (int) Math.floor((tempOfShip - 273.15d) / 100d);
        List<BlockPos> blockPosHits = this.getSunRayedBlocks(rayCount, new Vector3d(entityOrbitBody.getAbsolutePos()), ship, spaceServerLevel);

        blockPosHits.forEach(blockPos -> {
            BlockState blockState = spaceServerLevel.getBlockState(blockPos);
            float explosionResistance = blockState.getBlock().getExplosionResistance();
            int flammability = blockState.getFlammability(spaceServerLevel, blockPos, Direction.UP);
            float destroyTime = blockState.getDestroySpeed(spaceServerLevel, blockPos);

            if (HeatCalc.isBlockAboveMeltingPoint(tempOfShip, blockState, explosionResistance, flammability, destroyTime)) {
                spaceServerLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                spaceServerLevel.levelEvent(1501, blockPos, 0); // plays the fluid fizz effect
            }
        });
    }

    private List<BlockPos> getSunRayedBlocks(int rayCount, Vector3d sunPos, LoadedServerShip ship, SpaceServerLevel spaceServerLevel) {
        AABBic shipAABBic = ship.getShipAABB();
        List<BlockPos> blockPosHits = new ArrayList<>();

        if (shipAABBic == null || rayCount <= 0) {
            return blockPosHits;
        }

        sunPos.normalize();
        ship.getTransform().getRotation().transformInverse(sunPos.negate());
        sunPos.mul(64);

        for (int i = 0; i < rayCount; i++) {
            int randomX = spaceServerLevel.random.nextIntBetweenInclusive(shipAABBic.minX(), shipAABBic.maxX());
            int randomY = spaceServerLevel.random.nextIntBetweenInclusive(shipAABBic.minY(), shipAABBic.maxY());
            int randomZ = spaceServerLevel.random.nextIntBetweenInclusive(shipAABBic.minZ(), shipAABBic.maxZ());

            Vec3 startPos = new Vec3(randomX, randomY, randomZ);
            Vec3 endPos = new Vec3(startPos.x + sunPos.x, startPos.y + sunPos.y, startPos.z + sunPos.z);

            ClipContext clipContext = new ClipContext(endPos, startPos,
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, null);

            BlockHitResult blockHitResult = spaceServerLevel.clip(clipContext);
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                blockPosHits.add(blockHitResult.getBlockPos());
            }
        }
        return blockPosHits;
    }
}
