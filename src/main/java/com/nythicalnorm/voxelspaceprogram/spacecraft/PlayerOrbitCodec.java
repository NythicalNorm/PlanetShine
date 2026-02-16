package com.nythicalnorm.voxelspaceprogram.spacecraft;

import com.nythicalnorm.voxelspaceprogram.solarsystem.OrbitId;
import com.nythicalnorm.voxelspaceprogram.solarsystem.orbits.OrbitCodec;
import com.nythicalnorm.voxelspaceprogram.spacecraft.player.AbstractPlayerOrbitBody;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;

public class PlayerOrbitCodec extends OrbitCodec<AbstractPlayerOrbitBody, AbstractPlayerOrbitBody.PlayerOrbitBuilder> {

    @Override
    public void encodeBuffer(AbstractPlayerOrbitBody orbit, FriendlyByteBuf byteBuf) {
        super.encodeBuffer(orbit, byteBuf);
        byteBuf.writeOptional(orbit.getCurrentHostSpace(), OrbitId::encodeToBuffer);
    }

    @Override
    public AbstractPlayerOrbitBody.PlayerOrbitBuilder decodeBuffer(AbstractPlayerOrbitBody.PlayerOrbitBuilder playerSpacecraft, FriendlyByteBuf byteBuf) {
        super.decodeBuffer(playerSpacecraft, byteBuf);
        Optional<OrbitId> id = byteBuf.readOptional(OrbitId::new);
        id.ifPresent(playerSpacecraft::setHostSpace);
        return playerSpacecraft;
    }

    @Override
    public CompoundTag encodeNBT(AbstractPlayerOrbitBody orbit) {
        CompoundTag tag = super.encodeNBT(orbit);
        if (orbit.currentHostSpace != null) {
            orbit.currentHostSpace.encodeToNBT(tag, "current_host_space");
        }
        return tag;
    }

    @Override
    public AbstractPlayerOrbitBody.PlayerOrbitBuilder decodeNBT(AbstractPlayerOrbitBody.PlayerOrbitBuilder orbit, CompoundTag tag) {
        super.decodeNBT(orbit, tag);
        if (tag.contains("current_host_space")) {
            orbit.setHostSpace(new OrbitId(tag, "current_host_space"));
        }
        return orbit;
    }
}
