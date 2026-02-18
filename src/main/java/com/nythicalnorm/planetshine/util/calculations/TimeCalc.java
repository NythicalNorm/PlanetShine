package com.nythicalnorm.planetshine.util.calculations;

public class TimeCalc {
    private static final long longTicksPerSecond = 6000;
    public static final long TickToMilliTick = 100;
    public static final int PhysTickPerSec = 60;

    public static long TimePerTickToTimePerMilliTick(long timePassPerSec) {
        return timePassPerSec * TickToMilliTick;
    }

    public static long TimePerTickToTimePerMilliTick(double timePassPerSec) {
        return (long) (timePassPerSec * TickToMilliTick);
    }

    public static long TimePerMilliTickToTick(long timePassPerTick) {
        return timePassPerTick / TickToMilliTick;
    }

    public static double timeLongToDouble(long diff) {
        return (double) diff / longTicksPerSecond;
    }

    public static long timeDoubleToLong(double diff) {
        return (long) (diff * longTicksPerSecond);
    }
}
