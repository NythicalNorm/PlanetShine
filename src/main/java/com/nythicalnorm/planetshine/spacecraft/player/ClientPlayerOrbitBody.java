package com.nythicalnorm.planetshine.spacecraft.player;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.spacecraft.ServerboundPlayerHostVelUpdate;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.util.calculations.PlanetBodyCalc;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerOrbitBody extends AbstractPlayerOrbitBody {
    private Vector3d clientDeltavelLast;

    public ClientPlayerOrbitBody(PlayerOrbitBuilder playerSpacecraftBuilder) {
        super(playerSpacecraftBuilder, true);
        clientDeltavelLast = new Vector3d();
    }

    public void updatePlayerPosRot(CelestialBody currentPlanetOn) {
        updatePlanetPos(getPlayerEntity().level(), getPlayerEntity().position(), currentPlanetOn);
        updatePlanetRot(new Quaternionf(), currentPlanetOn);
    }

    private void updatePlanetRot(Quaternionf existingrotation, CelestialBody currentPlanet) {
        //quaternion to rotate the output of lookalong function to the correct -y direction.
        this.rotation = new Quaternionf(new AxisAngle4f(Mth.HALF_PI,1f,0f,0f));
        Vector3f playerRelativePos = new Vector3f((float) relativeOrbitalPos.x, (float) relativeOrbitalPos.y, (float) relativeOrbitalPos.z);
        playerRelativePos.normalize();
        Vector3f upVector = PlanetBodyCalc.getUpVectorForPlanetRot(new Vector3f(playerRelativePos), currentPlanet);
        this.rotation.lookAlong(playerRelativePos, upVector);
    }

    private void updatePlanetPos(Level level, Vec3 position, CelestialBody currentPlanetOn) {
        double seaLevel = level.getMinBuildHeight() + 127;
        position = new Vec3(position.x, position.y - seaLevel, position.z);

        relativeOrbitalPos = PlanetBodyCalc.planetDimPosToNormalizedVector(position, currentPlanetOn.getRadius(), currentPlanetOn.getRotation(), false);
        Vector3d newAbs = new Vector3d(currentPlanetOn.getAbsolutePos());
        absoluteOrbitalPos = newAbs.add(relativeOrbitalPos);
    }

    public void processLocalMovement(float inputAD, float inputSW, float inputQE, float inputShiftCTRL, float throttle, boolean SAS, boolean RCS, boolean inDockingMode) {

    }

    public void processHostMove(Vec3 deltaMovement) {
        clientDeltavelLast.add(deltaMovement.x, deltaMovement.y, deltaMovement.z);
    }

    public float getSunAngle() {
        return 0f;
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
