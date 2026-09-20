package com.nythicalnorm.planetshine.mixinducks;

import com.nythicalnorm.planetshine.util.calculations.MiscCalc;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public interface SpaceRotationAccessor {
    Quaternionf planetShine$getSpaceRotationOffset();
    Quaternionf planetShine$setSpaceRotationOffset(Quaternionfc rotation);

    boolean planetShine$canRotateRoll();
    void planetshine$setAdjustedRots(float xRot, float yRot);
    float planetshine$getAdjustedX();
    float planetshine$getAdjustedY();

    default void planetShine$rotateRoll(double roll) {
        Quaternionf rotToMul = new Quaternionf();
        Vector3f mcRotation = planetShine$getRotatedViewVector(this.planetshine$getAdjustedX(), this.planetshine$getAdjustedY());
        rotToMul.setAngleAxis((float) roll, mcRotation.x(), mcRotation.y(), mcRotation.z());
        this.planetShine$getSpaceRotationOffset().mul(rotToMul);
    }

    default void planetshine$updateAdjustedRots(double yMove, double xMove) {
        float xSmall = (float)xMove * 0.15F;
        float ySmall = (float)yMove * 0.15F;
        float xRot = this.planetshine$getAdjustedX() + xSmall;
        xRot = Mth.clamp(xRot, -90.0F, 90.0F);
        float yRot = this.planetshine$getAdjustedY() + ySmall;
        this.planetshine$setAdjustedRots(xRot, yRot);
    }

    default void setEntityMCRots(Entity entity, double yMove, double xMove) {
        Vec2 mcRot = this.rotateEntityMCRotation(this.planetshine$getAdjustedX(), this.planetshine$getAdjustedY());

        entity.setXRot(mcRot.x);
        entity.setYRot(mcRot.y);
        entity.setXRot(Mth.clamp(entity.getXRot(), -90.0F, 90.0F));

        entity.setYHeadRot(entity.getYRot());
        entity.xRotO = entity.getXRot();
        entity.xRotO = Mth.clamp(entity.xRotO, -90.0F, 90.0F);
        entity.yRotO = entity.getYRot();
    }

    default Vector3f planetShine$getRotatedViewVector(float pXRot, float pYRot) {
        float pitch = (float) Math.toRadians(pXRot);
        float yaw   = (float) Math.toRadians(pYRot);

        Vector3f viewVector = new Vector3f(
                -(float) Math.sin(yaw) * (float) Math.cos(pitch),
                -(float) Math.sin(pitch),
                (float) Math.cos(yaw) * (float) Math.cos(pitch)
        );

        planetShine$getSpaceRotationOffset().transformInverse(viewVector);
        return new Vector3f(viewVector.x(), viewVector.y(), viewVector.z());
    }

    default Vec2 rotateEntityMCRotation(float xRot, float yRot) {
        Vector3f direction = this.planetShine$getRotatedViewVector(xRot, yRot);

        float newXRot = (float) Math.toDegrees(-Math.asin(Math.max(-1.0f, Math.min(1.0f, direction.y))));
        float newYRot = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        newYRot = MiscCalc.unwrapAngle(newYRot, yRot);

        return new Vec2(newXRot, newYRot);
    }
}
