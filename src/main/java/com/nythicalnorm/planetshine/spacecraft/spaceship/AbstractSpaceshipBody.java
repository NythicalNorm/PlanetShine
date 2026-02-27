package com.nythicalnorm.planetshine.spacecraft.spaceship;

import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.OrbitalBodyTypesHolder;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.planetshine.spacecraft.hostspace.ShipHostSpace;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;
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
    public OrbitHostSpace createHostSpace(Vector2ic posNew) {
        OrbitHostSpace hostSpace = new ShipHostSpace(this.id, posNew, this);
        this.orbitHostSpace.set(hostSpace);
        return hostSpace;
    }

    public void setShip(@Nullable Ship ship) {
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
