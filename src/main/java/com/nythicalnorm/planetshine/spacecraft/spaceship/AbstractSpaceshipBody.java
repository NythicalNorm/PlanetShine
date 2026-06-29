package com.nythicalnorm.planetshine.spacecraft.spaceship;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.OrbitalBodyTypeRegistry;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.hostspace.HostSpaceManager;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.planetshine.spacecraft.hostspace.ShipHostSpace;
import com.nythicalnorm.planetshine.util.calculations.MiscCalc;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniondc;
import org.joml.Vector2ic;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;

import java.util.Optional;

public abstract class AbstractSpaceshipBody extends EntityOrbitBody<Ship> {
    public AbstractSpaceshipBody(ShipOrbitBuilder shipOrbitBuilder, boolean isClientSide) {
        super(shipOrbitBuilder, shipOrbitBuilder.currentHostSpace, shipOrbitBuilder.soiIntercept, isClientSide);
        this.body = shipOrbitBuilder.ship;
    }

    @Override
    public RegistryObject<OrbitalBodyType<? extends OrbitalBody, ? extends Builder<?>>> getType() {
        return OrbitalBodyTypeRegistry.SPACESHIP_BODY;
    }

    // server side only start

    @Override
    public OrbitHostSpace createHostSpace(Vector2ic posNew) {
        OrbitHostSpace hostSpace = new ShipHostSpace(this.id, posNew, this);
        this.setHostOrbitSpace(hostSpace);
        return hostSpace;
    }

    @Override
    public void entityLoadedInSpace(Ship ship, HostSpaceManager hostSpaceManager) {
        this.setBody(ship);
        Optional<OrbitId> hostSpaceID = this.getHostSpaceID();
        if (hostSpaceID.isPresent()) {
            OrbitHostSpace entityHostSpace = hostSpaceManager.getHostSpaceAt(this.getMcPosition());
            if (entityHostSpace != null) {
                entityHostSpace.addShipToHostSpace((ServerSpaceshipBody) this);
            }
        } else {
            PlanetShine.logError(ship.getSlug() + "ship is in space not near any host spaces");
        }
    }

    // server side only end

    @Override
    public Vector3dc getMcPosition() {
        if (this.body != null) {
            return this.body.getKinematics().getPosition();
        }
        return null;
    }

    @Override
    public Vector3dc getMcVelocity() {
        if (this.body != null) {
            return this.body.getKinematics().getVelocity();
        }
        return null;
    }

    @Override
    public Quaterniondc getMCRotation() {
        if (this.body != null) {
            return body.getKinematics().getRotation();
        }
        return null;
    }

    @Override
    public double getCrossSectionalArea(Vector3d airVelocity) {
        if (this.isBodyEntityLoaded()) {
            double sideLength = Math.cbrt(MiscCalc.getShipVolume(this.body));
            return sideLength * sideLength;
        } else {
            return 0.0d;
        }
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
