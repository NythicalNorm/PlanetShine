package com.nythicalnorm.planetshine.server;

import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.time.ClientboundTimeWarpUpdate;
import com.nythicalnorm.planetshine.spacecraft.player.AbstractPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.PlayerOrbitAccessor;
import com.nythicalnorm.planetshine.storage.PlanetShineConfig;
import com.nythicalnorm.planetshine.util.SpaceUtils;
import com.nythicalnorm.planetshine.util.UniverseStage;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TimeWarpManager {
    private final PSServer psServer;
    private volatile boolean sleepTimeWarping = false;


    public TimeWarpManager(PSServer psServer) {
        this.psServer = psServer;
    }

    public boolean isSleepTimeWarping() {
        return sleepTimeWarping;
    }

    public void setSleepTimeWarping(boolean sleepTimeWarping) {
        this.sleepTimeWarping = sleepTimeWarping;
    }

    public void TryChangeTimeWarp(long proposedSetTimeWarpSpeed, boolean allowOnPlanet, ServerPlayer player) {
        TimeWarpAllowanceReason allowanceReason = changeTimeWarp(proposedSetTimeWarpSpeed, allowOnPlanet, player);
        if (!allowanceReason.isAllowed()) {
            player.sendSystemMessage(allowanceReason.text, true);
        }
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

    private boolean isStartingTimeWarp(long proposedSetTimeWarpSpeed) {
        return proposedSetTimeWarpSpeed > UniverseStage.timeWarpSettings.get(0) && psServer.getCurrentTimeWarpSetting() == 0;
    }

    private TimeWarpAllowanceReason isPlayerAllowedToTimeWarp(ServerPlayer player, boolean allowOnPlanet) {
        if (player.getAbilities().instabuild || SpaceUtils.isSpaceLevel(player.level())) {
            return new TimeWarpAllowanceReason(null, true);
        } else if (PlanetShineConfig.doAllowTimeWarpOnPlanets()) {
            if (allowOnPlanet) {
                return monstersNearbyCheck(player);
            } else {
                return new TimeWarpAllowanceReason(Component.translatable("planetshine.timewarp.not_in_space"), false);
            }
        } else {
            return new TimeWarpAllowanceReason(Component.translatable("planetshine.timewarp.not_allowed_on_planets"), false);
        }
    }

    public void onGameTick(boolean timeWarping) {
        if (timeWarping) {
            TimeWarpAllowanceReason reason = checkIfTimeWarpIsPossible();
            if (!reason.isAllowed()) {
                long timePassPerTick = TimeCalc.TimePerTickToTimePerMilliTick(UniverseStage.timeWarpSettings.get(0));
                psServer.setTimePassPerTick(timePassPerTick);
                PacketHandler.sendToAllClients(new ClientboundTimeWarpUpdate(true, timePassPerTick));
                psServer.getMCServer().getPlayerList().broadcastSystemMessage(reason.text(), true);
            }
        }
    }

    public TimeWarpAllowanceReason checkIfTimeWarpIsPossible() {
        List<Component> inAtmosphereNames = new ArrayList<>();

        for (ServerPlayer player : psServer.getMCServer().getPlayerList().getPlayers()) {
            if (player instanceof PlayerOrbitAccessor playerOrbitAccessor && playerOrbitAccessor.getOrbitalBody() != null &&
                    playerOrbitAccessor.getOrbitalBody().isBodyEntityLoaded() && playerOrbitAccessor.getOrbitalBody().getParent() != null) {
                AbstractPlayerOrbitBody playerOrbitBody = playerOrbitAccessor.getOrbitalBody();
                double playerAltitude = playerOrbitBody.getAltitude();
                if (playerAltitude <= playerOrbitBody.getParent().getAtmosphere().getAtmosphereHeight() || playerAltitude <= 10000.0d) {
                    inAtmosphereNames.add(player.getName());
                }
            }
        }

        Component allowanceResult = null;

        if (!inAtmosphereNames.isEmpty()) {
            if (inAtmosphereNames.size() == 1) {
                allowanceResult = Component.translatable("planetshine.timewarp.not_possible").append(inAtmosphereNames.get(0))
                        .append(Component.translatable("planetshine.timewarp.player_reasons_singular"));
            } else {
                MutableComponent combinedNames = Component.empty();
                for (int i = 0; i < (inAtmosphereNames.size() - 1); i++) {
                    combinedNames.append(inAtmosphereNames.get(i)).append(Component.literal(", "));
                }
                combinedNames.append(inAtmosphereNames.get(inAtmosphereNames.size() - 1));

                allowanceResult = Component.translatable("planetshine.timewarp.not_possible").append(combinedNames)
                        .append(Component.translatable("planetshine.timewarp.player_reasons_plural"));
            }
        }

        if (allowanceResult == null) {
            return new TimeWarpAllowanceReason(null, true);
        } else {
            return new TimeWarpAllowanceReason(allowanceResult, false);
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
