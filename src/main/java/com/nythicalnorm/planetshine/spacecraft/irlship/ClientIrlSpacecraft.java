package com.nythicalnorm.planetshine.spacecraft.irlship;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2i;

@OnlyIn(Dist.CLIENT)
public class ClientIrlSpacecraft extends AbstractIrlSpacecraft {
    private static final ResourceLocation SPACECRAFT_ICONS_TEXTURE = ResourceLocation.fromNamespaceAndPath(PlanetShine.MODID,
            "textures/gui/spacecraft_icons.png");

    private static final Vector2i CAPSULE_ICON = new Vector2i(0, 0);
    private static final Vector2i ISS_ICON = new Vector2i(8, 0);
    private static final Vector2i SATELLITE_ICON = new Vector2i(16, 0);


    public ClientIrlSpacecraft(IRLSpacecraftBuilder orbitalBuilder) {
        super(orbitalBuilder, true);
    }

    @Override
    public OrbitHostAccessor getHostSpaceAccess() {
        return null;
    }

    @Override
    public boolean drawIcon(GuiGraphics graphics, Vector2i screenPos, int size) {
        int xPos = (screenPos.x - (size / 2));
        int yPos = (screenPos.y - (size / 2));

        if (this.body.equals("1998-067A") || this.body.trim().toLowerCase().contains("spacestation")) {
            drawSpecificIcon(graphics, ISS_ICON, xPos, yPos);
        } else if (this.body.equals("-1024") || this.body.trim().toLowerCase().contains("artemis")) {
            drawSpecificIcon(graphics, CAPSULE_ICON, xPos, yPos);
        } else {
            drawSpecificIcon(graphics, SATELLITE_ICON, xPos, yPos);
        }
        return true;
    }

    private void drawSpecificIcon(GuiGraphics graphics, Vector2i icon, int xPos, int yPos) {
        int navballMult = 1;

        graphics.blit(
                SPACECRAFT_ICONS_TEXTURE,
                xPos, yPos,
                icon.x() * navballMult, icon.y() * navballMult,
                8, 8
        );
    }}
