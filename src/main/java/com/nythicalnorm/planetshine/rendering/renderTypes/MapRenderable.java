package com.nythicalnorm.planetshine.rendering.renderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class MapRenderable {
    protected final MapRelativeState relativeState;
    OrbitalBody parentBody;
    protected List<MapRenderable> childRenderables;

    public MapRenderable(MapRelativeState mapRelativeState, OrbitalBody parentBody) {
        this.relativeState = mapRelativeState;
        this.parentBody = parentBody;
        this.childRenderables = new ArrayList<>();
    }

    public void addChildRenderable(MapRenderable renderableInMap) {
        this.childRenderables.add(renderableInMap);
    }

    public void propagateRender(GuiGraphics graphics, PoseStack poseStack, Matrix4f projectionMatrix, Vector3f parentPos, OrbitalBody currentFocusedBody) {
        poseStack.pushPose();
        if (relativeState.equals(MapRelativeState.AlwaysParentRelative)) {
            poseStack.translate(parentPos.x, parentPos.y, parentPos.z);
        }
        Vector3f parentBodyPos = render(graphics, poseStack, projectionMatrix, currentFocusedBody);
        poseStack.popPose();

        for (MapRenderable childRenderable : childRenderables) {
            childRenderable.propagateRender(graphics, poseStack, projectionMatrix, parentBodyPos, currentFocusedBody);
        }
    }

    public abstract Vector3f render(GuiGraphics graphics, PoseStack poseStack, Matrix4f projectionMatrix, OrbitalBody currentFocusedBody);
}

