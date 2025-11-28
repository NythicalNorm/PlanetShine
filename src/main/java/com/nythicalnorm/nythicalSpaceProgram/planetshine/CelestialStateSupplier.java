package com.nythicalnorm.nythicalSpaceProgram.planetshine;

import com.nythicalnorm.nythicalSpaceProgram.network.PacketHandler;
import com.nythicalnorm.nythicalSpaceProgram.network.ServerBoundTimeWarpChange;
import com.nythicalnorm.nythicalSpaceProgram.planet.PlanetDimensions;
import com.nythicalnorm.nythicalSpaceProgram.planet.PlanetaryBody;
import com.nythicalnorm.nythicalSpaceProgram.planet.Planets;
import com.nythicalnorm.nythicalSpaceProgram.util.Calcs;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

import java.lang.Math;

@OnlyIn(Dist.CLIENT)
public class CelestialStateSupplier {
    private double serverSideSolarSystemTime = 0;
    private double clientSideSolarSystemTime = 0;
    private long clientSideTickTime = 0L;

    public double lastUpdatedTimeWarpPerSec = 0;

    private Vector3d playerAbsolutePositon;
    private Vector3d playerRelativePositon;
    private Quaternionf playerRotation;
    private PlanetaryBody currentPlanet;

    public CelestialStateSupplier() {
        playerAbsolutePositon = new Vector3d();
        playerRelativePositon = new Vector3d();
    }

    public void UpdateState(double currentTime, double TimePassedPerSec){
        serverSideSolarSystemTime = currentTime;
        lastUpdatedTimeWarpPerSec = TimePassedPerSec;

        if (Math.abs(clientSideSolarSystemTime - serverSideSolarSystemTime) >  lastUpdatedTimeWarpPerSec*0.01d)
        {
            clientSideSolarSystemTime = serverSideSolarSystemTime;
        }
    }

    public void UpdatePlanetaryBodies() {
        long currentTime = Util.getMillis();

        if (!Minecraft.getInstance().isPaused()) {
            float timeDiff = (float) (currentTime - clientSideTickTime) / 1000;
            clientSideSolarSystemTime = clientSideSolarSystemTime + timeDiff * lastUpdatedTimeWarpPerSec;
        }

        clientSideTickTime = currentTime;
        Planets.UpdatePlanets(clientSideSolarSystemTime);
        updatePlayerPos();
        updatePlayerRot();
    }

    private void updatePlayerPos() {
        LocalPlayer plr = Minecraft.getInstance().player;
        if (isOnPlanet() && plr.level() != null) {
            currentPlanet = PlanetDimensions.getDimPlanet(plr.level().dimension());
            if (currentPlanet != null) {
                playerRelativePositon = Calcs.planetDimPosToNormalizedVector(plr.position(), currentPlanet.getRadius(), currentPlanet.getPlanetRotation(), false);
                Vector3d newAbs = currentPlanet.getPlanetAbsolutePos();
                playerAbsolutePositon = newAbs.add(playerRelativePositon);
            }
        }
    }

    private void updatePlayerRot() {
        if (isOnPlanet()) {
            //quaternion to rotate the output of lookalong function to the correct -y direction.
            this.playerRotation = new Quaternionf(new AxisAngle4f(Calcs.hPI,1f,0f,0f));
            Vector3f playerRelativePos = new Vector3f((float) playerRelativePositon.x, (float) playerRelativePositon.y, (float) playerRelativePositon.z);
            playerRelativePos.normalize();
            Vector3f upVector = Calcs.getUpVectorForPlanetRot(new Vector3f(playerRelativePos));
            this.playerRotation.lookAlong(playerRelativePos, upVector);
        }
    }

    public Vector3d getPlayerAbsolutePositon() {
        return playerAbsolutePositon;
    }

    public Vector3d getPlayerRelativePositon() {
        return new Vector3d(playerRelativePositon);
    }

    public Quaternionf getPlayerRotation() {
        return new Quaternionf(playerRotation);
    }

    public double getLastUpdatedTimeWarpPerSec() {
        return lastUpdatedTimeWarpPerSec;
    }

    public void TryChangeTimeWarp(boolean DoInc) {
        double sign = 2;
        if (!DoInc) {
            sign = 0.5;
        }
        PacketHandler.sendToServer(new ServerBoundTimeWarpChange(sign * lastUpdatedTimeWarpPerSec));
    }

    public boolean doRender() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return false;
        }
        return PlanetDimensions.isDimensionPlanet(mc.level.dimension()) || PlanetDimensions.isDimensionSpace(mc.level.dimension());
    }

    public boolean isOnPlanet()
    {
        return PlanetDimensions.isDimensionPlanet(Minecraft.getInstance().level.dimension());
    }

    public PlanetaryBody getDimPlanet() {
        return currentPlanet;
    }

    public PlanetaryBody getCurrentPlanet() {
        return currentPlanet;
    }
}
