package com.nythicalnorm.planetshine.spacecraft;

import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitCodec;
import com.nythicalnorm.planetshine.spacecraft.spaceship.AbstractSpaceshipBody;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;

public class SpaceshipBodyCodec extends OrbitCodec<AbstractSpaceshipBody, AbstractSpaceshipBody.ShipOrbitBuilder> {
    @Override
    public void encodeBuffer(AbstractSpaceshipBody orbit, FriendlyByteBuf byteBuf) {
        super.encodeBuffer(orbit, byteBuf);
        byteBuf.writeOptional(orbit.getHostSpaceID(), OrbitId::encodeToBuffer);
    }

    @Override
    public AbstractSpaceshipBody.ShipOrbitBuilder decodeBuffer(AbstractSpaceshipBody.ShipOrbitBuilder spacecraftBody, FriendlyByteBuf byteBuf) {
        super.decodeBuffer(spacecraftBody, byteBuf);
        Optional<OrbitId> id = byteBuf.readOptional(OrbitId::new);
        id.ifPresent(spacecraftBody::setHostSpace);
        return spacecraftBody;
    }

    @Override
    public CompoundTag encodeNBT(AbstractSpaceshipBody orbit) {
        CompoundTag tag = super.encodeNBT(orbit);
        if (orbit.hostSpaceID != null) {
            orbit.hostSpaceID.get().encodeToNBT(tag, "current_host_space");
        }
        return tag;
    }

    @Override
    public AbstractSpaceshipBody.ShipOrbitBuilder decodeNBT(AbstractSpaceshipBody.ShipOrbitBuilder orbit, CompoundTag tag) {
        super.decodeNBT(orbit, tag);
        if (tag.contains("current_host_space")) {
            orbit.setHostSpace(new OrbitId(tag, "current_host_space"));
        }
        return orbit;
    }
}