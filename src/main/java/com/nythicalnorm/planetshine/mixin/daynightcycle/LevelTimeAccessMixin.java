package com.nythicalnorm.planetshine.mixin.daynightcycle;


import com.nythicalnorm.planetshine.dimensions.SpaceServerLevel;
import com.nythicalnorm.planetshine.mixinducks.PlanetTimeAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelTimeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelTimeAccess.class)
public interface LevelTimeAccessMixin extends LevelReader {

    @Shadow
    long dayTime();
    /**
     * @author NythicalNorm
     * @reason  This is the easiest way to change the apparent dayTime on the client side I would use inject if I could
     * but this is an interface. The use of the original function is still there and I will make sure the  function
     * doesn't crash the whole thing. And even if there is another mod replacing this it won't crash the game, I mean
     * changing a float output shouldn't cause problems, I will make sure my value is clamped to the original 0 - 1.
     */
    @Overwrite
    default float getTimeOfDay(float pPartialTick) {
        if (!(this instanceof SpaceServerLevel)) {
            if (this instanceof PlanetTimeAccessor planetTimeAccessor) {
                return planetTimeAccessor.ps$getSunAngle(0d, 0d);
            }
            return this.dimensionType().timeOfDay(this.dayTime());
        } else {
            return this.dimensionType().timeOfDay(6000L);
        }
    }
}
