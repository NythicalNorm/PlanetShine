package com.nythicalnorm.planetshine.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;

@OnlyIn(Dist.CLIENT)
public class RenderingCommon {
    public static @Nullable Vector2i worldToScreenCoordinate(Vector3f pos, Matrix4f poseStack,
                                                             Matrix4f projectionMatrix, int width, int height) {
        Matrix4f clip_Pos = new Matrix4f(projectionMatrix).mul(poseStack);
        Vector4f clipVec = new Vector4f(pos.x, pos.y, pos.z, 1f).mul(clip_Pos);
        float x = clipVec.x/ clipVec.w;
        float y = -clipVec.y/ clipVec.w;

        int pixelX = Math.round((x+1)*0.5f*width);
        int pixelY = (int) Math.floor((y+1)*0.5f*height);
        if (clipVec.z > 0f) {
            return new Vector2i(pixelX, pixelY);
        } else {
            return null;
        }
    }
}
