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
    private Vector3f playerRotation;
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

    public double UpdatePlanetaryBodies() {
        long currentTime = Util.getMillis();

        if (!Minecraft.getInstance().isPaused()) {
            float timeDiff = (float) (currentTime - clientSideTickTime) / 1000;
            clientSideSolarSystemTime = clientSideSolarSystemTime + timeDiff * lastUpdatedTimeWarpPerSec;
        }

        clientSideTickTime = currentTime;
        Planets.UpdatePlanets(clientSideSolarSystemTime);
        updatePlayerPos();
        return clientSideSolarSystemTime;
    }

    public void updatePlayerPos() {
        LocalPlayer plr = Minecraft.getInstance().player;
        if (PlanetDimensions.isDimensionPlanet(plr.level().dimension())) {
            currentPlanet = PlanetDimensions.getDimPlanet(plr.level().dimension());
            playerRelativePositon = Calcs.planetDimPosToNormalizedVector(plr.position(), currentPlanet.getRadius(),false);
            Vector3d newAbs = currentPlanet.getPlanetAbsolutePos();
            playerAbsolutePositon = newAbs.add(playerRelativePositon);

        }
    }

    public Vector3d getPlayerAbsolutePositon() {
        return playerAbsolutePositon;
    }

    public Vector3d getPlayerRelativePositon() {
        return new Vector3d(playerRelativePositon);
    }

    public Quaternionf getPlayerRotation() {
        playerRotation = new Vector3f((float) playerRelativePositon.x,(float) playerRelativePositon.y,(float) playerRelativePositon.z);
        playerRotation.normalize();
        Quaternionf rotation = new Quaternionf();
        Vector3f newVector = new Vector3f(playerRotation.x, playerRotation.y, playerRotation.z);
        rotation.rotateX((float) Math.PI/2f);
        rotation.lookAlong(newVector, new Vector3f(0f,0f, 1f).normalize());
        return rotation;
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
        return PlanetDimensions.isDimensionPlanet(mc.level.dimension()) || PlanetDimensions.isDimensionSpace(mc.level.dimension());
    }

    public boolean isOnPlanet()
    {
        return PlanetDimensions.isDimensionPlanet(Minecraft.getInstance().level.dimension());
    }

    public PlanetaryBody getDimPlanet() {
        return currentPlanet;
    }
}
