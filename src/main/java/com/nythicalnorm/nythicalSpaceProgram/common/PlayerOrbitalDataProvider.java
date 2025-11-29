package com.nythicalnorm.nythicalSpaceProgram.common;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerOrbitalDataProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    public static Capability<PlayerOrbitalData> ORBITAL_DATA = CapabilityManager.get(new CapabilityToken<>() {
    });

    private PlayerOrbitalData orbitalData = null;
    private final LazyOptional<PlayerOrbitalData> optional = LazyOptional.of(this::getPlayerOrbitalData);

    private @NotNull PlayerOrbitalData getPlayerOrbitalData() {
        if (this.orbitalData == null) {
            this.orbitalData = new PlayerOrbitalData();
        }

        return this.orbitalData;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ORBITAL_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt = getPlayerOrbitalData().saveNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getPlayerOrbitalData().loadNBT(nbt);
    }
}
