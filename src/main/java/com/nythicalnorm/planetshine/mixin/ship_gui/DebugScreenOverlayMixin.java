package com.nythicalnorm.planetshine.mixin.ship_gui;

import com.llamalad7.mixinextras.sugar.Local;
import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import com.nythicalnorm.planetshine.util.SpaceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2dc;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "getGameInformation", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 4))
    private void addOrbitalPositionInfo(CallbackInfoReturnable<List<String>> cir, @Local List<String> list) {
        PSClient css = PSClient.get();
        if (css != null && css.doRender()) {
            Vector3dc relativeVelocity = css.getPlayerOrbit().getRelativeVelocity();
            Vector3dc relativePos = css.getPlayerOrbit().getRelativePos();
            OrbitalElementsc orbitalElements = css.getPlayerOrbit().getOrbitalElements();

            if (css.getCurrentPlanet().isPresent()) {
                Vector2dc latLong = SpaceUtils.getLatLongCoordinates(minecraft.player.position(), css.getCurrentPlanet().get());
                list.add(String.format(Locale.ROOT, "Lat: %.5f, Long: %.5f", latLong.x(), latLong.y()));
            } else if (orbitalElements != null && relativeVelocity != null) {
                list.add(String.format(Locale.ROOT, "Relative Velocity XYZ: %.2f / %.2f / %.2f", relativeVelocity.x(), relativeVelocity.y(), relativeVelocity.z()));
                list.add(String.format(Locale.ROOT, "Relative Pos Orbital XYZ: %.0f / %.0f / %.0f", relativePos.x(), relativePos.y(), relativePos.z()));

                list.add(String.format(Locale.ROOT, "Orbital Elements: e: %.4f / a: %.0f / i: %.2f° / Ω: %.2f° / ω: %.2f°",
                        orbitalElements.getEccentricity(), orbitalElements.getSemiMajorAxis(),
                        Math.toDegrees(orbitalElements.getInclination()),
                        Math.toDegrees(orbitalElements.getLongitudeOfAscendingNode()),
                        Math.toDegrees(orbitalElements.getArgumentOfPeriapsis())
                ));

//                OrbitalCalc.SOIIntercept timeToEscape = OrbitalCalc.findAllRelativePlanetIntercepts(css.getPlayerOrbit(), css.getCurrentTime(),
//                        css.getPlayerOrbit().getParent().getPlanetChildren());
////                OrbitalCalc.SOIIntercept timeToEscape = css.getPlayerOrbit().getOrbitalElements().findOrbitEscapeIntercept(css.getPlayerOrbit().getParent(), css.getCurrentTime());
//                if (timeToEscape != null) {
//                    list.add(String.format(Locale.ROOT, "Time To escape: %d", timeToEscape.timeElapsed()));
//                    list.add(String.format(Locale.ROOT, "currentTime: %d", css.getCurrentTime()));
//                }
//                list.add(String.format(Locale.ROOT, "tick time: %d", OrbitalCalc.tickTime));
            }
        }
    }
}
