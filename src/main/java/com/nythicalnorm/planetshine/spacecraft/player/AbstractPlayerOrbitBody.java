package com.nythicalnorm.planetshine.spacecraft.player;

import com.nythicalnorm.planetshine.solarsystem.OrbitalBodyTypesHolder;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.planetshine.spacecraft.hostspace.PlayerHostSpace;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2ic;
import org.valkyrienskies.core.api.util.GameTickOnly;

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
        this.orbitHostSpace.set(hostSpace);
        return hostSpace;
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
