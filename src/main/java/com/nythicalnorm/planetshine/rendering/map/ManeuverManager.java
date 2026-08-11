package com.nythicalnorm.planetshine.rendering.map;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElementsc;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ManeuverManager {
    private final PSClient psClient;
    private static final int ORBIT_PREDICTION_DEPTH = 3;
    private final List<PredictedSOIChange> predictedSOIChangesList;

    public ManeuverManager(PSClient psClient) {
        this.psClient = psClient;
        this.predictedSOIChangesList = new ArrayList<>();
    }

    public List<PredictedSOIChange> getPredictedSOIChangesList() {
        return predictedSOIChangesList;
    }

    public void calculateSOIChanges(@Nullable EntityOrbitBody<?> controllingBody) {
        this.predictedSOIChangesList.clear();
        if (controllingBody == null || controllingBody.getNextOrbitIntercept() == null || controllingBody.getOrbitalElements() == null) {
            return;
        }
        OrbitalCalc.SOIIntercept intercept = controllingBody.getNextOrbitIntercept();
        OrbitalElements newOrbit = new OrbitalElements(controllingBody.getOrbitalElements());
        CelestialBody lastParent = controllingBody.getParent();

        for (int i = 0; i < ORBIT_PREDICTION_DEPTH; i ++) {
            if (intercept == null) {
                break;
            }
            lastParent = OrbitalCalc.calculateSOIChange(intercept, lastParent, newOrbit, newOrbit);
            OrbitId planetBody = intercept.interceptingBody();

            double startingAnomaly = OrbitalCalc.getTrueAnomalyAtTime(newOrbit, intercept.timeElapsed());

            // now calculating the next next orbit after soiChange
            intercept = OrbitalCalc.calculateIntercepts(newOrbit, startingAnomaly, lastParent, intercept.timeElapsed());

            PredictedSOIChange predictedSOIChange = new ManeuverManager.PredictedSOIChange(
                    planetBody, new OrbitalElements(newOrbit), startingAnomaly, intercept
            );

            this.predictedSOIChangesList.add(predictedSOIChange);

        }
    }

    public record PredictedSOIChange(
            OrbitId parentPlanet,
            OrbitalElementsc newOrbit,
            double startingAnomaly,
            @Nullable OrbitalCalc.SOIIntercept nextIntercept
    ) { }
}
