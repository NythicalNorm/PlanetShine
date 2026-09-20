package com.nythicalnorm.planetshine.mixin.player_rotation;

import com.nythicalnorm.planetshine.mixinducks.SpaceRotationAccessor;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public class PlayerMixin implements SpaceRotationAccessor {
    @Unique
    float planetshine$adjustedXRot = 0.0f;
    @Unique
    float planetshine$adjustedYRot = 0.0f;

    @Unique
    Quaternionf planetShine$spaceRotationOffset = new Quaternionf();

    @Unique
    public Quaternionf planetShine$getSpaceRotationOffset() {
        return planetShine$spaceRotationOffset;
    }

    @Override
    public Quaternionf planetShine$setSpaceRotationOffset(Quaternionfc rotation) {
        return planetShine$spaceRotationOffset.set(rotation);
    }

    @Unique
    public boolean planetShine$canRotateRoll() {
        Player player = (Player) (Object) this;
        //return SpaceUtils.isSpaceLevel(player.level()) && player.isFallFlying() && !player.getAbilities().flying;
        return !player.isPassenger();
    }

    @Override
    public void planetshine$setAdjustedRots(float xRot, float yRot) {
        this.planetshine$adjustedXRot = xRot;
        this.planetshine$adjustedYRot = yRot;
    }

    @Override
    public float planetshine$getAdjustedX() {
        return this.planetshine$adjustedXRot;
    }

    @Override
    public float planetshine$getAdjustedY() {
        return this.planetshine$adjustedYRot;
    }
}
