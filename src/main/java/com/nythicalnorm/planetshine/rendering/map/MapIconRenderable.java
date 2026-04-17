package com.nythicalnorm.planetshine.rendering.map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@OnlyIn(Dist.CLIENT)
public interface MapIconRenderable {
    Vector2ic getLatestMapPos();
    void setLatestMapPos(Vector2i pos);

    void drawIcon(GuiGraphics graphics, Vector2i screenPos, int i);

    boolean shouldDraw();
}
