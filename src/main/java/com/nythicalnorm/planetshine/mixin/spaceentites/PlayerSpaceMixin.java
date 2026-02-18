package com.nythicalnorm.planetshine.mixin.spaceentites;

import com.nythicalnorm.planetshine.spacecraft.player.AbstractPlayerOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.PlayerOrbitAccessor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public class PlayerSpaceMixin implements PlayerOrbitAccessor {
    @Unique
    private AbstractPlayerOrbitBody playerOrbitBody;

    @Unique
    @Override
    public AbstractPlayerOrbitBody getOrbitalBody() {
        return playerOrbitBody;
    }

    @Unique
    @Override
    public void setOrbitalBody(AbstractPlayerOrbitBody abstractPlayerOrbitBody) {
        this.playerOrbitBody = abstractPlayerOrbitBody;
    }

}
