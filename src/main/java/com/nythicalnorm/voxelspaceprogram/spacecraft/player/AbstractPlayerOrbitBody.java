package com.nythicalnorm.voxelspaceprogram.spacecraft.player;

import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitalBodyTypesHolder;
import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitId;
import com.nythicalnorm.voxelspaceprogram.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.voxelspaceprogram.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.voxelspaceprogram.spacecraft.EntityOrbitBody;
import com.nythicalnorm.voxelspaceprogram.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.voxelspaceprogram.spacecraft.hostspace.ShipHostSpace;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

public abstract class AbstractPlayerOrbitBody extends EntityOrbitBody {
    protected static final float JetpackRotationalForce = 0.1f;
    protected static final double JetpackTranslationForce = 1d;
    protected static final double JetpackThrottleForce = 25d;
    protected Player player;

    public AbstractPlayerOrbitBody(PlayerOrbitBuilder playerSpacecraftBuilder, boolean isClientSide) {
        super(playerSpacecraftBuilder, isClientSide);
        this.player = playerSpacecraftBuilder.player;
        if (this.player != null) {
            ((PlayerOrbitAccessor)player).setOrbit(this);
        }
    }

    @Override
    public OrbitalBodyType<? extends OrbitalBody, ? extends Builder<?>> getType() {
        return OrbitalBodyTypesHolder.PLAYER_ORBITAL_BODY;
    }

    @Override
    public OrbitHostSpace createHostSpace(Vector3d posNew) {
        return new ShipHostSpace(this.id, posNew, this);
    }

    public void setPlayer(@NotNull Player player) {
        this.player = player;
        ((PlayerOrbitAccessor)player).setOrbit(this);
    }

    public Player getPlayerEntity() {
        return player;
    }

    public static class PlayerOrbitBuilder extends OrbitalBody.Builder<AbstractPlayerOrbitBody> {
        Player player = null;

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
    }
}
