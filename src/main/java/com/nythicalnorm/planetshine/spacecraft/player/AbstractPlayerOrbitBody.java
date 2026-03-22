package com.nythicalnorm.planetshine.spacecraft.player;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundHostOrbitSet;
import com.nythicalnorm.planetshine.solarsystem.OrbitalBodyTypesHolder;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.hostspace.HostSpaceManager;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.planetshine.spacecraft.hostspace.PlayerHostSpace;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Optional;

public abstract class AbstractPlayerOrbitBody extends EntityOrbitBody<Player> {

    public AbstractPlayerOrbitBody(PlayerOrbitBuilder playerSpacecraftBuilder, boolean isClientSide) {
        super(playerSpacecraftBuilder, playerSpacecraftBuilder.currentHostSpace, playerSpacecraftBuilder.soiIntercept, isClientSide);
        this.body = playerSpacecraftBuilder.player;
        if (this.body != null) {
            ((PlayerOrbitAccessor) body).setOrbitalBody(this);
        }
    }

    @Override
    public OrbitalBodyType<? extends OrbitalBody, ? extends Builder<?>> getType() {
        return OrbitalBodyTypesHolder.PLAYER_ORBITAL_BODY;
    }

    // server side only start

    @Override
    public OrbitHostSpace createHostSpace(Vector2ic posNew) {
        OrbitHostSpace hostSpace = new PlayerHostSpace(this.id, posNew, this);
        this.setHostOrbitSpace(hostSpace);
        return hostSpace;
    }

    @Override
    public void entityLoadedInSpace(Player player, HostSpaceManager hostSpaceManager) {
        this.setBody(player);
        Optional<OrbitId> hostSpaceID = this.getHostSpaceID();
        if (hostSpaceID.isPresent()) {
            OrbitHostSpace entityHostSpace = hostSpaceManager.getHostSpaceAt(this.getMcPosition());
            if (entityHostSpace != null) {
                entityHostSpace.addPlayerToHostSpace((ServerPlayerOrbitBody) this);
            }
        } else {
            PlanetShine.logError(player.getName() + " player is in space without a host space.");
        }
    }

    @Override
    public void setHostOrbitSpace(OrbitHostSpace hostSpace) {
        super.setHostOrbitSpace(hostSpace);
        if (this.body != null && !this.isClientSide) {
            PacketHandler.sendToPlayer(new ClientboundHostOrbitSet(hostSpace.getOrbitIdOfHost(),
                    hostSpace.getOriginPos()), (ServerPlayer) body);
        } else {
            PlanetShine.log("no players to set host space to");
        }
    }

    // server side only end

    @Override
    public Vector3dc getMcPosition() {
        if (this.body != null) {
            LoadedShip ship = VSGameUtilsKt.getShipMountedTo(this.body);
            if (ship == null) {
                return new Vector3d(this.body.position().x, this.body.position().y, this.body.position().z);
            } else {
                return ship.getKinematics().getPosition();
            }
        }
        return null;
    }

    @Override
    public Vector3dc getMcVelocity() {
        if (this.body != null) {
            LoadedShip ship = VSGameUtilsKt.getShipMountedTo(this.body);
            if (ship == null) {
                return new Vector3d(this.body.getDeltaMovement().x, this.body.getDeltaMovement().y, this.body.getDeltaMovement().z);
            } else {
                return ship.getKinematics().getVelocity();
            }
        }
        return null;
    }

    @Override
    public Quaterniondc getMCRotation() {
        if (this.body != null) {
            LoadedShip ship = VSGameUtilsKt.getShipMountedTo(this.body);
            if (ship == null) {
                return new Quaterniond().rotateY(this.body.getYRot()).rotateX(this.body.getXRot());
            } else {
                return ship.getKinematics().getRotation();
            }
        }
        return null;
    }

    @Override
    public void setBody(Player player) {
        super.setBody(player);
        if (player != null) {
            ((PlayerOrbitAccessor) player).setOrbitalBody(this);
        }
    }

    public void playerLeft() {
        this.body = null;
    }

    public abstract boolean isPlayerLoggedIn();

    public static class PlayerOrbitBuilder extends OrbitalBody.Builder<AbstractPlayerOrbitBody> {
        Player player = null;
        OrbitId currentHostSpace;
        OrbitalCalc.SOIIntercept soiIntercept;

        public PlayerOrbitBuilder() {
        }

        public void setPlayer(Player player) {
            this.player = player;
            this.displayName = player.getDisplayName();
            this.id = new OrbitId(player);
        }

        public void setHostSpace(OrbitId orbitId) {
            this.currentHostSpace = orbitId;
        }

        public void setSoiIntercept(OrbitalCalc.SOIIntercept soiIntercept) {
            this.soiIntercept = soiIntercept;
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
    }
}
