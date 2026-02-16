package com.nythicalnorm.voxelspaceprogram.spacecraft.spaceship;

import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitId;
import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitalBodyTypesHolder;
import com.nythicalnorm.voxelspaceprogram.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.voxelspaceprogram.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.voxelspaceprogram.spacecraft.EntityOrbitBody;
import com.nythicalnorm.voxelspaceprogram.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.voxelspaceprogram.spacecraft.hostspace.PlayerHostSpace;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;

public abstract class AbstractSpaceshipBody extends EntityOrbitBody {
    protected Ship ship;

    public AbstractSpaceshipBody(ShipOrbitBuilder shipOrbitBuilder, boolean isClientSide) {
        super(shipOrbitBuilder, shipOrbitBuilder.currentHostSpace, isClientSide);
    }

    @Override
    public OrbitalBodyType<? extends OrbitalBody, ? extends Builder<?>> getType() {
        return OrbitalBodyTypesHolder.SPACESHIP_BODY;
    }

    @Override
    public OrbitHostSpace createHostSpace(Vector3d posNew) {
        return new PlayerHostSpace(this.id, posNew, this);
    }

    public void setShip(@NotNull Ship ship) {
        this.ship = ship;
    }

    public Ship getShip() {
        return ship;
    }

    public static class ShipOrbitBuilder extends Builder<AbstractSpaceshipBody> {
        Ship ship = null;
        OrbitId currentHostSpace;

        public ShipOrbitBuilder() {
        }

        public void setShip(@NotNull Ship ship) {
            this.ship = ship;
            this.id = new OrbitId(ship);
        }

        @Override
        public AbstractSpaceshipBody build() {
            return new ServerSpaceshipBody(this);
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public AbstractSpaceshipBody buildClientSide() {
            return new ClientSpaceshipBody(this);
        }

        public void setHostSpace(OrbitId orbitId) {
            this.currentHostSpace = orbitId;
        }
    }
}
