package com.nythicalnorm.planetshine.rendering.renderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.util.RenderingCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class MapRenderableIcon extends MapRenderable {
    private final ResourceLocation spacecraftTextureLoc;
    private final EntityOrbitBody spacecraftBody;
    private int[] screenPos;

    public MapRenderableIcon(EntityOrbitBody playerBody, ResourceLocation playerHeadTex, MapRelativeState mapRelativeState, OrbitalBody parentBody) {
        super(mapRelativeState, parentBody);
        this.spacecraftTextureLoc = playerHeadTex;
        this.spacecraftBody = playerBody;
        screenPos = new int[2];
    }

    //This doesn't actually render it just calculates the position so it can be rendered with guiGraphics in the future
    @Override
    public Vector3f render(PoseStack poseStack, Matrix4f projectionMatrix, OrbitalBody currentFocusedBody) {
        if (currentFocusedBody == null) {
            return null;
        }
        Vector3f pos = getPos(spacecraftBody, currentFocusedBody);
        Screen screen = Minecraft.getInstance().screen;
        if (screen != null) {
            screenPos = RenderingCommon.worldToScreenCoordinate(pos,
                    poseStack, projectionMatrix, screen.width, screen.height);
        }
        return null;
    }

    public int[] getScreenPos() {
        return screenPos;
    }

    public EntityOrbitBody getPlayerBody() {
        return spacecraftBody;
    }

    public ResourceLocation getPlayerTextureLoc() {
        return spacecraftTextureLoc;
    }
}
