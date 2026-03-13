package com.nythicalnorm.planetshine.mixinducks;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface SpaceRotationAccessor {
    Quaternionf planetShine$getSpaceRotationOffset();
    void planetShine$setSpaceRotationOffset(Quaternionf rotation);
    boolean planetShine$canRotateRoll();

    default void planetShine$rotateRoll(double roll, float xRot, float yRot) {
        Quaternionf rotToMul = new Quaternionf();
        Vec3 mcRotation = planetShine$getRotatedViewVector(xRot, yRot);

        rotToMul.setAngleAxis((float) roll, (float) mcRotation.x(), (float) mcRotation.y(), (float) mcRotation.z());
        this.planetShine$getSpaceRotationOffset().mul(rotToMul);
    }

    default Vec3 planetShine$getRotatedViewVector(float pXRot, float pYRot) {
        float radPitch = pXRot * ((float)Math.PI / 180F);
        float radYaw = -pYRot * ((float)Math.PI / 180F);
        float f2 = Mth.cos(radYaw);
        float f3 = Mth.sin(radYaw);
        float f4 = Mth.cos(radPitch);
        float f5 = Mth.sin(radPitch);

        Vector3f viewVector = new Vector3f(f3 * f4, -f5, f2 * f4);
        planetShine$getSpaceRotationOffset().transformInverse(viewVector);

        return new Vec3(viewVector.x(), viewVector.y(), viewVector.z());
    }
}
