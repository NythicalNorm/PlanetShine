package com.nythicalnorm.voxelspaceprogram.storage;

import com.nythicalnorm.voxelspaceprogram.PSServer;
import com.nythicalnorm.voxelspaceprogram.util.Stage;
import com.nythicalnorm.voxelspaceprogram.util.calculations.TimeCalc;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class VSPCommonSaveData extends SavedData {
    private long currentTime;
    private long timeWarp;

    public VSPCommonSaveData() {
        currentTime = Stage.WORLD_START_TIME;
        timeWarp = TimeCalc.TickToMilliTick;
        if (PSServer.get() != null) {
            currentTime = PSServer.get().getCurrentTime();
            timeWarp = PSServer.get().getTimePassPerTick();
        }
    }

    public VSPCommonSaveData(long currentTime, long timeWarp) {
        this.currentTime = currentTime;
        this.timeWarp = timeWarp;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public long getTimeWarp() {
        return timeWarp;
    }

    public static VSPCommonSaveData load(CompoundTag pCompoundTag) {
        long currTime = pCompoundTag.getLong("current_time");
        long currTimeWarp = pCompoundTag.getLong("current_time_warp");

        if (!Stage.timeWarpSettings.contains(TimeCalc.TimePerMilliTickToTick(currTimeWarp))) {
            currTimeWarp = TimeCalc.TickToMilliTick;
        }

        return new VSPCommonSaveData(currTime, currTimeWarp);
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
