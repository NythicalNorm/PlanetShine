package com.nythicalnorm.planetshine.spacecraft;

import com.nythicalnorm.planetshine.network.NetworkEncoders;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitCodec;
import com.nythicalnorm.planetshine.spacecraft.irlship.AbstractIrlSpacecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class IRLSpacecraftCodec extends OrbitCodec<AbstractIrlSpacecraft, AbstractIrlSpacecraft.IRLSpacecraftBuilder> {
    @Override
    public void encodeBuffer(AbstractIrlSpacecraft orbit, FriendlyByteBuf byteBuf) {
        super.encodeBuffer(orbit, byteBuf);
        NetworkEncoders.writeUTF8(byteBuf, orbit.getBody());
    }

    @Override
    public AbstractIrlSpacecraft.IRLSpacecraftBuilder decodeBuffer(AbstractIrlSpacecraft.IRLSpacecraftBuilder orbit, FriendlyByteBuf byteBuf) {
        super.decodeBuffer(orbit, byteBuf);
        orbit.setJplId(NetworkEncoders.readUTF8(byteBuf));

        return orbit;
    }

    @Override
    public CompoundTag encodeNBT(AbstractIrlSpacecraft orbit) {
        CompoundTag tag = super.encodeNBT(orbit);
        tag.putString("jpl_id", orbit.getBody());

        return tag;
    }

    @Override
    public AbstractIrlSpacecraft.IRLSpacecraftBuilder decodeNBT(AbstractIrlSpacecraft.IRLSpacecraftBuilder orbit, CompoundTag tag) {
        super.decodeNBT(orbit, tag);
        orbit.setJplId(tag.getString("jpl_id"));
        return orbit;
    }
}
