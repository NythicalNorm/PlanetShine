package com.nythicalnorm.planetshine.mixin.worldborder;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nythicalnorm.planetshine.mixinducks.PlanetWorldBorder;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.util.UniverseStage;
import com.nythicalnorm.planetshine.util.calculations.PlanetCalc;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(value = WorldBorder.class, priority = 800)
public class WorldBorderMixin implements PlanetWorldBorder {
    @Unique
    VoxelShape ps$planetBorderShape;

    @Unique
    double ps$cellSize;

//    @Override
//    public VoxelShape ps$getPlanetBorder() {
//        return ps$worldBorderShape;
//    }

    @Unique
    private boolean ps$isPlanetWorldBorder() {
        return ps$planetBorderShape != null && UniverseStage.get().getPsCommonConfig().isOverrideVanillaWorldBorder();
    }

    @Override
    public void ps$setPlanetBorder(CelestialBody celestialBody) {
        double cellSize = PlanetCalc.getSquareCellSize(celestialBody.getRadius());
        this.ps$cellSize = cellSize;
        double halfSize = cellSize / 2.0d;
        double minY = Double.NEGATIVE_INFINITY;
        double maxY = Double.POSITIVE_INFINITY;

        VoxelShape[] cubes = {
                Shapes.box(-halfSize, -halfSize, -halfSize, halfSize, maxY, halfSize), // center
                Shapes.box(-halfSize, halfSize, halfSize, halfSize, maxY, halfSize + cellSize), // top
                Shapes.box(-halfSize, minY, -halfSize - cellSize, halfSize, maxY, -halfSize), // bottom
                Shapes.box(-halfSize - cellSize, minY, -halfSize, -halfSize, maxY, halfSize), // left
                Shapes.box(halfSize, minY, -halfSize, halfSize + cellSize, maxY, halfSize), // right
                Shapes.box(halfSize + cellSize, minY, -halfSize, halfSize + cellSize + cellSize, maxY, halfSize) // more right
        };

        this.ps$planetBorderShape = Shapes.empty();
        for (VoxelShape cube : cubes) {
            this.ps$planetBorderShape = Shapes.join(ps$planetBorderShape, cube, BooleanOp.OR);
        }

        this.ps$planetBorderShape = Shapes.join(Shapes.INFINITY, this.ps$planetBorderShape, BooleanOp.ONLY_FIRST);
    }

    @ModifyReturnValue(method = "isWithinBounds(Lnet/minecraft/core/BlockPos;)Z", at = @At("RETURN"))
    public boolean isWithinPlanetBounds(final boolean original, final BlockPos pPos) {
        if (ps$isPlanetWorldBorder()) {
            return PlanetCalc.isPosInsidePlanetBounds(pPos.getX(), pPos.getZ(), ps$cellSize);
        } else {
            return original;
        }
    }

    @ModifyReturnValue(method = "isWithinBounds(DD)Z", at = @At("RETURN"))
    public boolean isWithinPlanetBounds(final boolean original, final double pX, final double pZ) {
        if (ps$isPlanetWorldBorder()) {
            return PlanetCalc.isPosInsidePlanetBounds(pX, pZ, ps$cellSize);
        } else {
            return original;
        }
    }

    @ModifyReturnValue(method = "isWithinBounds(DDD)Z", at = @At("RETURN"))
    public boolean isWithinPlanetBounds(final boolean original, final double pX, final double pZ, final double pOffset) {
        if (ps$isPlanetWorldBorder()) {
            return PlanetCalc.isPosInsidePlanetBounds(pX, pZ, ps$cellSize);
        } else {
            return original;
        }
    }

    @ModifyReturnValue(method = "isWithinBounds(Lnet/minecraft/world/phys/AABB;)Z", at = @At("RETURN"))
    public boolean isWithinPlanetBounds(final boolean original, final AABB pBox) {
        if (ps$isPlanetWorldBorder()) {
            return PlanetCalc.isPosInsidePlanetBounds(pBox.minX, pBox.minZ, ps$cellSize) &&
                    PlanetCalc.isPosInsidePlanetBounds(pBox.minX, pBox.maxZ, ps$cellSize) &&
                    PlanetCalc.isPosInsidePlanetBounds(pBox.maxX, pBox.maxZ, ps$cellSize) &&
                    PlanetCalc.isPosInsidePlanetBounds(pBox.maxX, pBox.minZ, ps$cellSize);
        } else {
            return original;
        }
    }

    @ModifyReturnValue(method = "isWithinBounds(Lnet/minecraft/world/level/ChunkPos;)Z", at = @At("RETURN"))
    public boolean isWithinPlanetBounds(final boolean original, final ChunkPos pChunkPos) {
        if (ps$isPlanetWorldBorder()) {
            return PlanetCalc.isPosInsidePlanetBounds(pChunkPos.getMinBlockX(), pChunkPos.getMinBlockZ(), ps$cellSize) &&
                    PlanetCalc.isPosInsidePlanetBounds(pChunkPos.getMinBlockX(), pChunkPos.getMaxBlockZ(), ps$cellSize) &&
                    PlanetCalc.isPosInsidePlanetBounds(pChunkPos.getMaxBlockX(), pChunkPos.getMaxBlockZ(), ps$cellSize) &&
                    PlanetCalc.isPosInsidePlanetBounds(pChunkPos.getMaxBlockX(), pChunkPos.getMinBlockZ(), ps$cellSize);
        } else {
            return original;
        }
    }

    @WrapMethod(method = "getDistanceToBorder(DD)D")
    public double distanceToPlanetBorder(double pX, double pZ, Operation<Double> original) {
        if (ps$isPlanetWorldBorder()) {
            int xCell = PlanetCalc.getCellIndex(ps$cellSize, pX);
            int zCell = PlanetCalc.getCellIndex(ps$cellSize, pZ);
            double halfCell = ps$cellSize / 2;

            List<Double> distances = new ArrayList<>();

            if (xCell == 0) {
                distances.add(Math.abs(pZ - (halfCell + ps$cellSize)));
                distances.add(Math.abs(pZ - (-halfCell - ps$cellSize)));

                if (zCell != 0) {
                    distances.add(Math.abs(pX - halfCell));
                    distances.add(Math.abs(pX + halfCell));
                }
            } else {
                distances.add(Math.abs(pZ - halfCell));
                distances.add(Math.abs(pZ + halfCell));

                distances.add(Math.abs(pX - halfCell + (2*ps$cellSize)));
                distances.add(Math.abs(pZ - (-halfCell - ps$cellSize)));
            }

            return Collections.min(distances);
        } else {
            return original.call(pX, pZ);
        }
    }

    @WrapMethod(method = "getCollisionShape")
    public VoxelShape getPlanetCollisionShape(Operation<VoxelShape> original) {
        if (ps$isPlanetWorldBorder()) {
            return ps$planetBorderShape;
        } else {
            return original.call();
        }
    }
}
