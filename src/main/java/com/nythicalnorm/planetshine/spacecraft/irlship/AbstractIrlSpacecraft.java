package com.nythicalnorm.planetshine.spacecraft.irlship;

import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.OrbitalBodyTypeRegistry;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.hostspace.HostSpaceManager;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostSpace;
import com.nythicalnorm.planetshine.spacecraft.hostspace.ShipHostSpace;
import com.nythicalnorm.planetshine.util.calculations.OrbitalCalc;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniondc;
import org.joml.Vector2ic;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;

public abstract class AbstractIrlSpacecraft extends EntityOrbitBody<String> {
    public AbstractIrlSpacecraft(IRLSpacecraftBuilder orbitalBuilder, boolean isClientSide) {
        super(orbitalBuilder, orbitalBuilder.hostSpaceID, orbitalBuilder.soiIntercept, isClientSide);
        this.body = orbitalBuilder.jplId;
    }

    @Override
    public RegistryObject<OrbitalBodyType<? extends OrbitalBody, ? extends Builder<?>>> getType() {
        return OrbitalBodyTypeRegistry.IRL_SPACECRAFT_BODY;
    }

    @Override
    public @Nullable Vector3dc getMcPosition() {
        return null;
    }

    @Override
    public @Nullable Vector3dc getMcVelocity() {
        return null;
    }

    @Override
    public @Nullable Quaterniondc getMCRotation() {
        return null;
    }

    @Override
    public OrbitHostSpace createHostSpace(Vector2ic posNew) {
        return new ShipHostSpace(this.id, posNew, this);
    }

    @Override
    public void entityLoadedInSpace(String entity, HostSpaceManager hostSpaceManager) {
    }

    public static class IRLSpacecraftBuilder extends Builder<AbstractIrlSpacecraft> {
        String jplId;
        OrbitId hostSpaceID;
        OrbitalCalc.SOIIntercept soiIntercept;

        public IRLSpacecraftBuilder() {
        }

        @Override
        public void setId(OrbitId id) {
            super.setId(id);
            this.hostSpaceID = id;
        }

        public void setShip(@NotNull Ship ship) {
            this.id = new OrbitId(ship);
        }

        public void setJplId(String jplId) {
            this.jplId = jplId;
        }

        public void setSoiIntercept(OrbitalCalc.SOIIntercept soiIntercept) {
            this.soiIntercept = soiIntercept;
        }

        @Override
        public AbstractIrlSpacecraft build() {
            return new ServerIrlSpacecraft(this);
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public AbstractIrlSpacecraft buildClientSide() {
            return new ClientIrlSpacecraft(this);
        }
    }
}
