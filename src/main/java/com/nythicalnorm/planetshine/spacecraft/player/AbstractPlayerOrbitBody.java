package com.nythicalnorm.planetshine.spacecraft.player;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundHostOrbitSet;
import com.nythicalnorm.planetshine.solarsystem.OrbitalBodyTypesHolder;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.planetshine.spacecraft.hostspace.PlayerHostSpace;
import com.nythicalnorm.planetshine.util.Calc;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.util.GameTickOnly;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public abstract class AbstractPlayerOrbitBody extends EntityOrbitBody {
    protected Player player;

    public AbstractPlayerOrbitBody(PlayerOrbitBuilder playerSpacecraftBuilder, boolean isClientSide) {
        super(playerSpacecraftBuilder, playerSpacecraftBuilder.currentHostSpace, isClientSide);
        this.player = playerSpacecraftBuilder.player;
        if (this.player != null) {
            ((PlayerOrbitAccessor)player).setOrbitalBody(this);
        }
    }

    @Override
    public OrbitalBodyType<? extends OrbitalBody, ? extends Builder<?>> getType() {
        return OrbitalBodyTypesHolder.PLAYER_ORBITAL_BODY;
    }

    @Override
    public OrbitHostSpace createHostSpace(Vector2ic posNew) {
        OrbitHostSpace hostSpace = new PlayerHostSpace(this.id, posNew, this);
        this.setHostOrbitSpace(hostSpace);
        return hostSpace;
    }

    @Override
    public void setHostOrbitSpace(OrbitHostSpace playerHostSpace) {
        super.setHostOrbitSpace(playerHostSpace);
        if (this.player != null) {
            PacketHandler.sendToPlayer(new ClientboundHostOrbitSet(playerHostSpace.getOrbitIdOfHost(),
                    playerHostSpace.getOriginPos()), (ServerPlayer) player);
        } else {
            PlanetShine.log("no players to set host space to");
        }
    }

    @Override
    public boolean isBodyEntityLoaded() {
        return this.player != null;
    }

    @Override
    public Vector3dc getMcPosition() {
        if (this.player != null) {
            LoadedShip ship = VSGameUtilsKt.getShipMountedTo(this.player);
            if (ship == null) {
                return new Vector3d(this.player.position().x, this.player.position().y, this.player.position().z);
            } else {
                return ship.getKinematics().getPosition();
            }
        }
        return null;
    }

    @Override
    public Vector3dc getMcVelocity() {
        if (this.player != null) {
            LoadedShip ship = VSGameUtilsKt.getShipMountedTo(this.player);
            if (ship == null) {
                return new Vector3d(this.player.getDeltaMovement().x, this.player.getDeltaMovement().y, this.player.getDeltaMovement().z);
            } else {
                return ship.getKinematics().getVelocity();
            }
        }
        return null;
    }

    @Override
    public Quaterniondc getMCRotation() {
        if (this.player != null) {
            LoadedShip ship = VSGameUtilsKt.getShipMountedTo(this.player);
            if (ship == null) {
                return Calc.mcRotationToQuaterniond(this.player.getYRot(), this.player.getXRot());
            } else {
                return ship.getKinematics().getRotation();
            }
        }
        return null;
    }

    public void setPlayer(@NotNull Player player) {
        this.player = player;
        ((PlayerOrbitAccessor)player).setOrbitalBody(this);
    }

    public void playerLeft() {
        this.player = null;
    }

    public abstract boolean isPlayerLoggedIn();

    @GameTickOnly
    public Player getPlayerEntity() {
        return player;
    }

    public static class PlayerOrbitBuilder extends OrbitalBody.Builder<AbstractPlayerOrbitBody> {
        Player player = null;
        OrbitId currentHostSpace;

        public PlayerOrbitBuilder() {
        }

        public void setPlayer(Player player) {
            this.player = player;
            this.displayName = player.getDisplayName();
            this.id = new OrbitId(player);
        }

        @Override
        public AbstractPlayerOrbitBody build() {
            return new ServerPlayerOrbitBody(this);
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public AbstractPlayerOrbitBody buildClientSide() {
            return new ClientPlayerOrbitBody(this);
        }

        public void setHostSpace(OrbitId orbitId) {
            this.currentHostSpace = orbitId;
        }
    }
}
