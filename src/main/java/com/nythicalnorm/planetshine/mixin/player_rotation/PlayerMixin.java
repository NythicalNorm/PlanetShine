package com.nythicalnorm.planetshine.mixin.player_rotation;

import com.nythicalnorm.planetshine.mixinducks.SpaceRotationAccessor;
import com.nythicalnorm.planetshine.util.SpaceUtils;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public class PlayerMixin implements SpaceRotationAccessor {
    @Unique
    Quaternionf planetShine$spaceRotationOffset = new Quaternionf();

    @Unique
    public Quaternionf planetShine$getSpaceRotationOffset() {
        return planetShine$spaceRotationOffset;
    }

    @Override
    public void planetShine$setSpaceRotationOffset(Quaternionf rotation) {
        planetShine$spaceRotationOffset.set(rotation);
    }

    @Unique
    public boolean planetShine$canRotateRoll() {
        Player player = (Player) (Object) this;
        return SpaceUtils.isSpaceLevel(player.level()) && player.isFallFlying() && !player.getAbilities().flying;
    }
}
