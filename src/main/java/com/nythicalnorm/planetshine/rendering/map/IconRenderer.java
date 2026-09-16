package com.nythicalnorm.planetshine.rendering.map;

import com.nythicalnorm.planetshine.PlanetShine;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@OnlyIn(Dist.CLIENT)
public class IconRenderer {
    private static final ResourceLocation MAP_ICON_TEXTURES = ResourceLocation.fromNamespaceAndPath(PlanetShine.MODID,
            "textures/gui/map_icons.png");

    public static final Vector2i DEFAULT_SPACESHIP_ICON = new Vector2i(0, 0);
    public static final Vector2i DEFAULT_PLANET_ICON = new Vector2i(48, 0);

    private static final float MAP_ICONS_SCALE = 1.0f / 8.0f;

    public static void drawIcon(GuiGraphics graphics, Vector2i icon, Vector2ic pos) {
        graphics.pose().pushPose();
        graphics.pose().translate(pos.x(), pos.y(), 0.0f);
        graphics.pose().scale(MAP_ICONS_SCALE, MAP_ICONS_SCALE, MAP_ICONS_SCALE);

        int iconMult = (int) (0.5f / MAP_ICONS_SCALE);
        int centerDist = Math.round(256f * MAP_ICONS_SCALE);

        graphics.blit(
                MAP_ICON_TEXTURES,
                -centerDist, -centerDist,
                icon.x() * iconMult, icon.y() * iconMult,
                64, 64
        );

        graphics.pose().popPose();
    }
}
