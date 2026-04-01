package com.nythicalnorm.planetshine.server;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.time.ClientboundTimeWarpUpdate;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.util.SpaceUtils;
import com.nythicalnorm.planetshine.util.UniverseStage;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

public class TimeWarpManager {
    private final PSServer psServer;
    private final SolarSystem solarSystem;
    private volatile boolean sleepTimeWarping = false;


    public TimeWarpManager(PSServer psServer) {
        this.psServer = psServer;
        this.solarSystem = psServer.getSolarSystem();
    }

    public boolean isSleepTimeWarping() {
        return sleepTimeWarping;
    }

    public void setSleepTimeWarping(boolean sleepTimeWarping) {
        this.sleepTimeWarping = sleepTimeWarping;
    }

    public void TryChangeTimeWarp(long proposedSetTimeWarpSpeed, boolean allowOnPlanet, ServerPlayer player) {
        Component nope = Component.literal("Nope :)");
        player.sendSystemMessage(nope, true);
        //changeTimeWarp(proposedSetTimeWarpSpeed, allowOnPlanet, player);
    }

    public TimeWarpAllowanceReason changeTimeWarp(long proposedSetTimeWarpSpeed, boolean allowOnPlanet, ServerPlayer player) {
        long timePassPerSec = (long) Mth.clamp(proposedSetTimeWarpSpeed, 0, 5000000);
        timePassPerSec = TimeCalc.TimePerTickToTimePerMilliTick(timePassPerSec);

        TimeWarpAllowanceReason isPlayerAllowed = isPlayerAllowedToTimeWarp(player, allowOnPlanet);
        if (!isPlayerAllowed.isAllowed()) {
            return isPlayerAllowed;
        }
        if (isStartingTimeWarp(proposedSetTimeWarpSpeed)) {
            TimeWarpAllowanceReason checkIfBlocked = checkIfTimeWarpIsPossible();
            if (!checkIfBlocked.isAllowed()) {
                return checkIfBlocked;
            }
        }

        psServer.setTimePassPerTick(timePassPerSec);

        psServer.getMCServer().getPlayerList().broadcastSystemMessage(Component.translatable("planetshine.timewarp.set",
                proposedSetTimeWarpSpeed), true);
        PacketHandler.sendToAllClients(new ClientboundTimeWarpUpdate(true, timePassPerSec));
        return new TimeWarpAllowanceReason(null, true);
    }

    public long getCurrentPCTime() {
        long nowInMilli = Instant.now().toEpochMilli();
        return nowInMilli * 6L;
    }

    public static float getCurrentTimeEarthAngle(Vector3f relativePos, Quaternionfc northPoleRot) {
        relativePos.normalize();
        relativePos.rotate(new Quaternionf(northPoleRot));

        double angleToSun = Math.atan2(relativePos.z(), relativePos.x());

        return (float) -angleToSun + getLocalTimeToAngle();
    }

    private static float getLocalTimeToAngle() {
        LocalTime now = LocalTime.now();

        long millisSinceMidnight =
                now.toNanoOfDay() / 1_000_000;

        float value = millisSinceMidnight / 86_400_000f;
        return value * Mth.TWO_PI;
    }

    private TimeWarpAllowanceReason checkIfTimeWarpIsPossible() {
//        for (EntityOrbitBody<?> entityOrbitBody : solarSystem.getAllEntitiesOrbitsList()) {
//
//        }

        return new TimeWarpAllowanceReason(null, true);
    }

    private boolean isStartingTimeWarp(long proposedSetTimeWarpSpeed) {
        return proposedSetTimeWarpSpeed > UniverseStage.timeWarpSettings.get(0) && psServer.getCurrentTimeWarpSetting() == 0;
    }

    private TimeWarpAllowanceReason isPlayerAllowedToTimeWarp(ServerPlayer player, boolean allowOnPlanet) {
        if (player.getAbilities().instabuild) {
            return new TimeWarpAllowanceReason(null, true);
        }
        if (SpaceUtils.isSpaceLevel(player.level())) {
            return new TimeWarpAllowanceReason(null, true);
        } else if (allowOnPlanet) {
            return monstersNearbyCheck(player);
        } else {
            return new TimeWarpAllowanceReason(Component.translatable("planetshine.timewarp.not_in_space"), false);
        }

    }

    private TimeWarpAllowanceReason monstersNearbyCheck(ServerPlayer serverPlayer) {
        Vec3 pos = Vec3.atBottomCenterOf(serverPlayer.blockPosition());
        AABB toSearch = new AABB(pos.x() - 8.0D, pos.y() - 5.0D, pos.z() - 8.0D, pos.x() + 8.0D, pos.y() + 5.0D, pos.z() + 8.0D);
        List<Monster> list = serverPlayer.level().getEntitiesOfClass(Monster.class, toSearch, (monster) -> monster.isPreventingPlayerRest(serverPlayer));
        if (!list.isEmpty()) {
            return new TimeWarpAllowanceReason(Component.translatable("planetshine.timewarp.mobs_nearby"), false);
        } else {
            return new TimeWarpAllowanceReason(null, true);
        }
    }

    public record TimeWarpAllowanceReason(@Nullable Component text, boolean isAllowed) {}
}
