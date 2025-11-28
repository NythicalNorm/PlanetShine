package com.nythicalnorm.nythicalSpaceProgram.mixin;


import com.nythicalnorm.nythicalSpaceProgram.NythicalSpaceProgram;
import com.nythicalnorm.nythicalSpaceProgram.util.Calcs;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelTimeAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@OnlyIn(Dist.CLIENT)
@Mixin(LevelTimeAccess.class)
public interface LevelTimeAccessMixin extends LevelReader {

    @Shadow
    long dayTime();
    /**
     * @author NythicalNorm
     * @reason  This is the easiest way to change the apparent dayTime on the client side I would use inject if I could
     * but this is an interface. The use of the original function is still there and I will make sure the  function
     * doesn't crash the whole thing. And even if there is another mod replacing this it won't crash the game, I mean
     * changing a float output shouldn't cause problems, I will make sure my value is clamped to the original 0 = 24000.
     */
    @Overwrite
    default float getTimeOfDay(float pPartialTick) {
        if (this.isClientSide()) {
            if (NythicalSpaceProgram.getCelestialStateSupplier().isOnPlanet()) {
                float sunAngle = NythicalSpaceProgram.getCelestialStateSupplier().getSunAngle();
                sunAngle = sunAngle + 1.0f;
                sunAngle = (sunAngle * 0.5f) - 0.25f;
                if (sunAngle > 0.75f) {
                    sunAngle = 1f - sunAngle;
                }
                return Calcs.clamp(0f, 1f, sunAngle);
            }
        }
        else {
            float f = 0;
        }
        return this.dimensionType().timeOfDay(this.dayTime());
    }
}
