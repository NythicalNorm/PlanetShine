package com.nythicalnorm.planetshine.gui.widgets;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.planet.PlanetaryBody;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

@OnlyIn(Dist.CLIENT)
public class TimeWarpWidget extends AbstractWidget {
    private static final ResourceLocation TIME_WARP_TEXTURE = ResourceLocation.fromNamespaceAndPath(PlanetShine.MODID,
            "textures/gui/timewarpwidget.png");

    public TimeWarpWidget(int pX, int pY, Component pMessage) {
        super(pX, pY, 136, 34, pMessage);
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.blit(TIME_WARP_TEXTURE, getX(), getY(),0,0,136,34);

        PSClient.getInstance().ifPresent(psClient -> {
            Component timeComp = parseTime(psClient.getCurrentTimeInSec(), psClient.getSolarSystem().getOverworldPlanet());
            pGuiGraphics.drawString(Minecraft.getInstance().font, timeComp,13, 6, 0x00ff2b, false);

            int timeWarpSettingAmount = psClient.getCurrentTimeWarpSetting() + 1;
            int ArrowXVal = 15;

            for (int i = 0; i < timeWarpSettingAmount; i++) {
                pGuiGraphics.blit(TIME_WARP_TEXTURE, getX() + ArrowXVal, getY() + 20,15,34,6,9);
                ArrowXVal += 10;
            }
        });
    }

    private Component parseTime(Double currentTime, CelestialBody overworldPlanet) {
        if (overworldPlanet == null || overworldPlanet.getOrbitalElements() == null) {
            return Component.empty();
        }

       double yearTime = overworldPlanet.getOrbitalElements().getOrbitalPeriod();
       // this is not sidereal rotation period the values need to be changed and this calculation also needs to be changed.
       double dayTime = TimeCalc.timeLongToDouble(((PlanetaryBody)overworldPlanet).getRotationPeriod());
       double hourTime = dayTime/24;
       double minuteTime = hourTime/60;

       int year = (int) Math.floor(currentTime/yearTime) + 1;
       int remainderYear = (int) (currentTime%yearTime);
       int day = (int) Math.floor(remainderYear/dayTime);
       int remainderHour = (int) (remainderYear % dayTime);
       int hour = (int) Math.floor(remainderHour/hourTime);
       int remainderMinute = (int) (remainderHour % hourTime);
       int minute = (int) Math.floor(remainderMinute/minuteTime);

        DecimalFormat twodigits = new DecimalFormat("00");

        return Component.translatable("planetshine.screen.time",
                year, day, twodigits.format(hour), twodigits.format(minute));
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {
    }
}
