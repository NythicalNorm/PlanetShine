package com.nythicalnorm.planetshine.spacecraft.spaceship;

import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.OrbitalBodyTypesHolder;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.planetshine.spacecraft.hostspace.ShipHostSpace;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniondc;
import org.joml.Vector2ic;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;

public abstract class AbstractSpaceshipBody extends EntityOrbitBody {
    protected Ship ship;

    public AbstractSpaceshipBody(ShipOrbitBuilder shipOrbitBuilder, boolean isClientSide) {
        super(shipOrbitBuilder, shipOrbitBuilder.currentHostSpace, shipOrbitBuilder.soiIntercept, isClientSide);
        this.ship = shipOrbitBuilder.ship;
    }

    @Override
    public OrbitalBodyType<? extends OrbitalBody, ? extends Builder<?>> getType() {
        return OrbitalBodyTypesHolder.SPACESHIP_BODY;
    }

    @Override
    public OrbitHostSpace createHostSpace(Vector2ic posNew) {
        OrbitHostSpace hostSpace = new ShipHostSpace(this.id, posNew, this);
        this.setHostOrbitSpace(hostSpace);
        return hostSpace;
    }

    @Override
    public boolean isBodyEntityLoaded() {
        return this.ship != null;
    }

    @Override
    public Vector3dc getMcPosition() {
        if (this.ship != null) {
            return this.ship.getKinematics().getPosition();
        }
        return null;
    }

    @Override
    public Vector3dc getMcVelocity() {
        if (this.ship != null) {
            return this.ship.getKinematics().getVelocity();
        }
        return null;
    }

    @Override
    public Quaterniondc getMCRotation() {
        if (this.ship != null) {
            return ship.getKinematics().getRotation();
        }
        return null;
    }

    public void setShip(@Nullable Ship ship) {
        this.ship = ship;
    }

    public @Nullable Ship getShip() {
        return ship;
    }

    public static class ShipOrbitBuilder extends Builder<AbstractSpaceshipBody> {
        Ship ship;
        OrbitId currentHostSpace;
        OrbitalCalc.SOIIntercept soiIntercept;

        public ShipOrbitBuilder() {
        }

        public void setShip(@NotNull Ship ship) {
            this.ship = ship;
            this.id = new OrbitId(ship);
        }

        public void setSoiIntercept(OrbitalCalc.SOIIntercept soiIntercept) {
            this.soiIntercept = soiIntercept;
        }

        public void setHostSpace(OrbitId orbitId) {
            this.currentHostSpace = orbitId;
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
    }
}
