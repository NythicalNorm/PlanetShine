package com.nythicalnorm.planetshine.rendering.renderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class MapRenderable {
    protected final MapRelativeState relativeState;
    protected Vector3f currentBodyPos;
    protected List<MapRenderable> childRenderables;

    public MapRenderable(MapRelativeState mapRelativeState) {
        this.relativeState = mapRelativeState;
        this.childRenderables = new ArrayList<>();
    }

    public void addChildRenderable(MapRenderable renderableInMap) {
        this.childRenderables.add(renderableInMap);
    }

    public void propagateRender(GuiGraphics graphics, PoseStack poseStack, Matrix4f projectionMatrix, Vector3f parentPos, OrbitalBody currentFocusedBody) {
        poseStack.pushPose();

        this.currentBodyPos = render(graphics, poseStack, projectionMatrix, currentFocusedBody);
        poseStack.popPose();

        for (MapRenderable childRenderable : childRenderables) {
            childRenderable.propagateRender(graphics, poseStack, projectionMatrix, this.currentBodyPos, currentFocusedBody);
        }
    }

    public Vector3fc getMapPos() {
        return currentBodyPos;
    }

    public abstract Vector3f render(GuiGraphics graphics, PoseStack poseStack, Matrix4f projectionMatrix, OrbitalBody currentFocusedBody);
}

