package com.nythicalnorm.planetshine.util.calculations;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;

public class HeatCalc {
    public static final double stephanBoltzmannConstant =  5.670374419e-8d;

    public static double getTemperatureInSpaceFromStar(double starLuminosity, double altitude, float albedo) {
        double tempOfShip = ((1.0d - albedo) * starLuminosity) / (16.0d * Math.PI * stephanBoltzmannConstant * altitude * altitude);
        tempOfShip = Math.pow(tempOfShip, 0.25d);
        return tempOfShip;
    }

    public static boolean isBlockAboveMeltingPoint(double temperature, BlockState blockState,
                                                   float explosionResistance, int flammability, float destroyTime) {
        double meltingPoint;

        if (explosionResistance > 1000d) {
            meltingPoint = 2.1d * explosionResistance;
        } else if (flammability > 1) {
            meltingPoint = 100.0d - flammability;
        } else if (blockState.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            meltingPoint = 2000d * Math.log(destroyTime);
        } else if (blockState.is(BlockTags.NEEDS_IRON_TOOL)) {
            meltingPoint = 1600d * Math.log(destroyTime);
        } else if (blockState.is(BlockTags.NEEDS_STONE_TOOL)) {
            meltingPoint = 1000d * Math.log(destroyTime);
        } else {
            meltingPoint = 1000d * Math.log(destroyTime);
        }

        if (meltingPoint < 0) {
            meltingPoint = 0d;
        }
        meltingPoint = meltingPoint + 273.25d;

        return meltingPoint <= temperature;
    }
}
