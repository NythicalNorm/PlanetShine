package com.nythicalnorm.planetshine.storage;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.util.UniverseStage;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class PSCommonSaveData extends SavedData {
    private long currentTime;
    private long timeWarp;

    public PSCommonSaveData() {
        currentTime = UniverseStage.WORLD_START_TIME;
        timeWarp = TimeCalc.TickToMilliTick;
        if (PSServer.get() != null) {
            currentTime = PSServer.get().getCurrentTime();
            timeWarp = PSServer.get().getTimePassPerTick();
        }
    }

    public PSCommonSaveData(long currentTime, long timeWarp) {
        this.currentTime = currentTime;
        this.timeWarp = timeWarp;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public long getTimeWarp() {
        return timeWarp;
    }

    public static PSCommonSaveData load(CompoundTag pCompoundTag) {
        long currTime = pCompoundTag.getLong("current_time");
        long currTimeWarp = pCompoundTag.getLong("current_time_warp");

        if (!UniverseStage.timeWarpSettings.contains(TimeCalc.TimePerMilliTickToTick(currTimeWarp))) {
            currTimeWarp = TimeCalc.TickToMilliTick;
        }

        return new PSCommonSaveData(currTime, currTimeWarp);
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag pCompoundTag) {
        PSServer psServer = PSServer.get();
        pCompoundTag.putLong("current_time", psServer.getCurrentTime());
        pCompoundTag.putLong("current_time_warp", psServer.getTimePassPerTick());
        return pCompoundTag;
    }

    @Override
    public boolean isDirty() {
        return PSServer.get() != null;
    }
}
