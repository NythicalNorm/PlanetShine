package com.nythicalnorm.planetshine.spacecraft.player;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.spacecraft.ServerboundPlayerHostVelUpdate;
import com.nythicalnorm.planetshine.rendering.map.MapIconRenderable;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostAccessor;
import com.nythicalnorm.planetshine.util.calculations.PlanetCalc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerOrbitBody extends AbstractPlayerOrbitBody implements MapIconRenderable {
    private final Vector3d clientDeltaVelLast;
    private final Quaterniond playerRotation;
    private PlayerInfo playerInfo;
    private Vector2i mapPos;

    public ClientPlayerOrbitBody(PlayerOrbitBuilder playerSpacecraftBuilder) {
        super(playerSpacecraftBuilder, true);
        this.clientDeltaVelLast = new Vector3d();
        this.playerRotation = new Quaterniond();
    }

    @Override
    public void init() {
        super.init();
        this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getOrbitId().getUUID());
    }

    public void updatePlayerPosRot(CelestialBody currentPlanetOn) {
        updatePlanetPos(getBody().level(), getBody().position(), currentPlanetOn);
        updatePlanetRot(currentPlanetOn);
    }

    private void updatePlanetRot(CelestialBody currentPlanet) {
        this.playerRotation.set(PlanetCalc.getPlanetToSpaceRotation(this.getMcPosition(), currentPlanet));
    }

    private void updatePlanetPos(Level level, Vec3 position, CelestialBody currentPlanetOn) {
        double seaLevel = level.getMinBuildHeight() + 127;

        this.relativeOrbitalPos.set(PlanetCalc.getPlanetRelativePosition(
                position.x, position.y - seaLevel, position.z, currentPlanetOn, false)
        );
        this.absoluteOrbitalPos.set(currentPlanetOn.getAbsolutePos()).add(relativeOrbitalPos);
    }

    public void updateSurfaceDownRotation() {
        Vector3d down = new Vector3d(this.relativeOrbitalPos.x, this.relativeOrbitalPos.y, this.relativeOrbitalPos.z)
                .negate()
                .normalize();

        Vector3d forward = new Vector3d(0, -1.0f, 0);

        // If forward is nearly parallel to down, choose another vector.
//        if (Math.abs(down.dot(forward)) > 0.999f)
//            forward.set(-1.0f, 0, 0);

        Vector3d right = forward.cross(down, new Vector3d()).normalize();
        forward = down.cross(right, new Vector3d()).normalize();

        // Negate because local down is -Y
        Matrix3d m = new Matrix3d(
                right.x, -down.x, forward.x,
                right.y, -down.y, forward.y,
                right.z, -down.z, forward.z
        );

        this.playerRotation.setFromNormalized(m).invert().normalize();
    }

    public Quaterniondc getPlayerRotation() {
        if (PSClient.get().weInSpaceDim()) {
            return new Quaterniond();
        } else {
            return playerRotation;
        }
    }

    public Quaterniondc getSurfaceDownRot() {
        if (PSClient.get().weInSpaceDim()) {
            return this.playerRotation;
        } else {
            return new Quaterniond();
        }
    }

    @Override
    public OrbitHostAccessor getHostSpaceAccess() {
        return PSClient.get().getCurrentHostSpace();
    }

    public void clearRotation() {
        this.playerRotation.identity();
    }

    public void processHostMove(Vec3 deltaMovement) {
        clientDeltaVelLast.add(deltaMovement.x, deltaMovement.y, deltaMovement.z);
    }

    public void sendMovementPacket() {
        if (PSClient.get().isTimeWarping()) {
            return;
        }

        if (clientDeltaVelLast.length() > tolerance) {
            PacketHandler.sendToServer(new ServerboundPlayerHostVelUpdate(this.id, clientDeltaVelLast));
            clientDeltaVelLast.zero();
        }
    }

    @Override
    public void setBody(@NotNull Player player) {
        super.setBody(player);
        this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(player.getUUID());
    }

    @Override
    public boolean isBodyEntityLoaded() {
        return super.isBodyEntityLoaded() || this.playerInfo != null;
    }

    public void playerJoined(PlayerInfo pPlayerInfo) {
        this.playerInfo = pPlayerInfo;
    }

    @Override
    public void playerLeft() {
        super.playerLeft();
        this.playerInfo = null;
    }

    private ResourceLocation getSkinTexture() {
        if (this.playerInfo != null) {
            return playerInfo.getSkinLocation();
        } else if (this.body != null) {
            return ((AbstractClientPlayer)this.body).getSkinTextureLocation();
        } else {
            return MissingTextureAtlasSprite.getLocation();
        }
    }

    @Override
    public Vector2i getLatestMapPos() {
        return mapPos;
    }

    @Override
    public void setLatestMapPos(Vector2i pos) {
        this.mapPos = pos;
    }

    @Override
    public void drawIcon(GuiGraphics graphics, Vector2i screenPos, int size) {
        this.setLatestMapPos(screenPos);
        PlayerFaceRenderer.draw(graphics, this.getSkinTexture(), (screenPos.x - (size/2)), (screenPos.y - (size/2)), size);
    }

    @Override
    public boolean shouldDrawIcon() {
        return this.isBodyEntityLoaded();
    }
}
