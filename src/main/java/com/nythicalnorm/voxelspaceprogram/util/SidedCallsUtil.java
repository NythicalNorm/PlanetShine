package com.nythicalnorm.voxelspaceprogram.util;

import com.nythicalnorm.voxelspaceprogram.PSClient;

import java.util.concurrent.Callable;

public class SidedCallsUtil {
    public static Callable<Float> getPlayerSunAngle() {
        return () -> {
            if (PSClient.getInstance().isPresent()) {
                if (PSClient.getInstance().get().isOnPlanet()) {
                    return PSClient.getInstance().get().getSunAngle();
                }
            }
            return null;
        };
    }
}
