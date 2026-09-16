package com.nythicalnorm.planetshine.solarsystem.orbits;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Map;

public class OrbitalBodyType<T extends OrbitalBody, M extends OrbitalBody.Builder<T>> {
    private final OrbitCodec<T, M> codec;
    private final Supplier<M> builder;

    public OrbitalBodyType(OrbitCodec<T, M> codec, Supplier<M> builder) {
        this.codec = codec;
        this.builder = builder;
    }

    public void encodeToBuffer(OrbitalBody orbit, FriendlyByteBuf friendlyByteBuf) {
        codec.encodeBuffer((T) orbit, friendlyByteBuf);
    }

    public M decodeFromBuffer(FriendlyByteBuf friendlyByteBuf) {
       return codec.decodeBuffer(builder.getInstance(), friendlyByteBuf);
    }

    public CompoundTag encodeToNBT(OrbitalBody orbitalBody) {
        return codec.encodeNBT((T) orbitalBody);
    }

    public M decodeFromNBT(CompoundTag tag) {
        return codec.decodeNBT(builder.getInstance(), tag);
    }

    public M readCelestialBodyDataPack(String name, JsonObject jsonObj,  Map<String, String[]> tempChildPlanetsMap) {
        return codec.readCelestialBodyDatapack(getInstance(), name, jsonObj, tempChildPlanetsMap);
    }

    public M getInstance() {
        return builder.getInstance();
    }

    @FunctionalInterface
    public interface Supplier<M extends OrbitalBody.Builder<?>> {
        M getInstance();
    }
}
