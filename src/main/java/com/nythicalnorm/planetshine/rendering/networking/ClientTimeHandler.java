package com.nythicalnorm.planetshine.rendering.networking;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientTimeHandler {
    private static volatile long serverUpdatedSolarSystemTime;
    private static long serverSolarSystemTimeTarget;
    private static long serverSolarSystemTimePrevTick;

    public void UpdateState(long serverTime){
        serverUpdatedSolarSystemTime = serverTime;
    }

    public void tick() {
        serverSolarSystemTimePrevTick = serverSolarSystemTimeTarget;
        serverSolarSystemTimeTarget = serverUpdatedSolarSystemTime;
    }

    public long calculateCurrentTime(float partialTick) {
        return lerpTime(partialTick, serverSolarSystemTimePrevTick, serverSolarSystemTimeTarget);
    }

    public static long lerpTime(double pDelta, long pStart, long pEnd) {
        double diff = (double) (pEnd - pStart);
        return pStart + (long) (pDelta * diff);
    }
}
