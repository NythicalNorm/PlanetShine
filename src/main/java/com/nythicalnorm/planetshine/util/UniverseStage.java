package com.nythicalnorm.planetshine.util;

import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.storage.PSCommonConfig;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;

import java.util.List;

public abstract class UniverseStage {
    private static UniverseStage instance;
    public static final long WORLD_START_TIME = 0;
    public static final List<Long> timeWarpSettings = List.of(1L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L);
    public static SolarSystem anySolarSystem;

    protected volatile long currentTime = WORLD_START_TIME; // time passed since start in 1000 times currentTick, in milliTicks if you will.
    protected volatile long timePassPerTick = TimeCalc.TickToMilliTick;
    protected volatile int currentTimeWarpSetting = 0;
    protected final SolarSystem solarSystem;
    protected final PSCommonConfig psCommonConfig;

    protected UniverseStage(SolarSystem solarSystem, PSCommonConfig psCommonConfig) {
        this.solarSystem = solarSystem;
        this.solarSystem.setStage(this);
        this.psCommonConfig = psCommonConfig;
        instance = this;
        if (anySolarSystem == null) {
            anySolarSystem = solarSystem;
        }
    }

    public static UniverseStage get() {
        return instance;
    }

    public PSCommonConfig getPsCommonConfig() {
        return psCommonConfig;
    }

    protected void initPlanets() {
        this.solarSystem.getRootStar().initCalcs(this.solarSystem);
    }

    // Returns the server solar system when on Singleplayer & on Dedicated Server,
    // and returns the client solar system in Multiplayer client.
    public static SolarSystem getAnySolarSystem() {
        return anySolarSystem;
    }

    protected static void close() {
        anySolarSystem = null;
    }

    public SolarSystem getSolarSystem() {
        return solarSystem;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public double getCurrentTimeInSec() {
        return TimeCalc.timeLongToDouble(currentTime);
    }

    public int getCurrentTimeWarpSetting() {
        return currentTimeWarpSetting;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public boolean isTimeWarping() {
        return currentTimeWarpSetting > 0;
    }

    public long getTimePassPerTick() {
        return timePassPerTick;
    }

    public void setTimePassPerTick(long timePassPerTick) {
        this.timePassPerTick = timePassPerTick;
        this.currentTimeWarpSetting = timeWarpSettings.indexOf(TimeCalc.TimePerMilliTickToTick(getTimePassPerTick()));
    }
}
