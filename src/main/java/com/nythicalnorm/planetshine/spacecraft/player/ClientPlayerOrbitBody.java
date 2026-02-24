package com.nythicalnorm.planetshine.spacecraft.player;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.spacecraft.ServerboundPlayerHostVelUpdate;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.util.calculations.PlanetBodyCalc;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.*;

import java.lang.Math;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerOrbitBody extends AbstractPlayerOrbitBody {
    private final Vector3d clientDeltavelLast;
    private final Quaterniond playerOnPlanetRotation;

    public ClientPlayerOrbitBody(PlayerOrbitBuilder playerSpacecraftBuilder) {
        super(playerSpacecraftBuilder, true);
        this.clientDeltavelLast = new Vector3d();
        this.playerOnPlanetRotation = new Quaterniond();
    }

    public void updatePlayerPosRot(CelestialBody currentPlanetOn) {
        updatePlanetPos(getPlayerEntity().level(), getPlayerEntity().position(), currentPlanetOn);
        updatePlanetRot(currentPlanetOn);
    }

    private void updatePlanetRot(CelestialBody currentPlanet) {
        //quaternion to rotate the output of lookalong function to the correct -y direction.
        this.playerOnPlanetRotation.set(new AxisAngle4d(Math.PI*0.5d,1d,0d,0d));
        Vector3d playerRelativePos = new Vector3d(this.getRelativePos());
        playerRelativePos.normalize();
        Vector3d upVector = PlanetBodyCalc.getUpVectorForPlanetRot(new Vector3d(playerRelativePos), currentPlanet);
        this.playerOnPlanetRotation.lookAlong(playerRelativePos, upVector);
    }

    private void updatePlanetPos(Level level, Vec3 position, CelestialBody currentPlanetOn) {
        double seaLevel = level.getMinBuildHeight() + 127;
        position = new Vec3(position.x, position.y - seaLevel, position.z);

        this.relativeOrbitalPos.set(PlanetBodyCalc.planetDimPosToNormalizedVector(position, currentPlanetOn.getRadius(), currentPlanetOn.getRotation(), false));
        this.absoluteOrbitalPos.set(currentPlanetOn.getAbsolutePos()).add(relativeOrbitalPos);
    }

    public Quaterniondc getPlayerOnPlanetRotation() {
        return playerOnPlanetRotation;
    }

    public void clearRotation() {
        this.playerOnPlanetRotation.identity();
    }

    public void processHostMove(Vec3 deltaMovement) {
        clientDeltavelLast.add(deltaMovement.x, deltaMovement.y, deltaMovement.z);
    }

    public void sendMovementPacket() {
        if (PSClient.get().isTimeWarping()) {
            return;
        }

        if (clientDeltavelLast.length() > tolerance) {
            PacketHandler.sendToServer(new ServerboundPlayerHostVelUpdate(this.id, clientDeltavelLast));
            clientDeltavelLast.zero();
        }
    }
}
