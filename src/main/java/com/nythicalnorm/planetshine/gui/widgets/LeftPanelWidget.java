package com.nythicalnorm.planetshine.gui.widgets;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.gui.screen.ISpacecraftDataDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class LeftPanelWidget extends AbstractWidget {
    private static final ResourceLocation LEFTPANEL_GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(PlanetShine.MODID,
            "textures/gui/leftpanelwidget.png");

    public LeftPanelWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage);
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int y = getY() - 35;
        int x = getX();

        pGuiGraphics.blit(LEFTPANEL_GUI_TEXTURE, x, y,0,0,66,35);

        if (PSClient.getInstance().get().getScreenManager().getSpacecraftScreen() instanceof  ISpacecraftDataDisplay spacecraftDataDisplay) {
           if (spacecraftDataDisplay.isDockingMode()) {
               drawDockingMode(spacecraftDataDisplay, pGuiGraphics, x, y);
           } else {
               drawNormalMode(spacecraftDataDisplay, pGuiGraphics, x, y);
           }
        }
    }

    private void drawDockingMode(ISpacecraftDataDisplay spacecraftScreen, GuiGraphics pGuiGraphics, int x, int y) {
        pGuiGraphics.blit(LEFTPANEL_GUI_TEXTURE, x + 7, y + 23,7,36,10,10);
        pGuiGraphics.blit(LEFTPANEL_GUI_TEXTURE, x + 23, y + 2,23,36,41,32);

        int xOffset = (int) (spacecraftScreen.getADAxis().getPositiveAxisValue()*23f - 2.5f);
        int yOffset = (int) ((1 - spacecraftScreen.getSWAxis().getPositiveAxisValue())*23f - 2.5f);

        pGuiGraphics.blit(LEFTPANEL_GUI_TEXTURE, x + 24 + xOffset, y + 9 + yOffset,67,13,5,5);

        int yAxis = (int) (spacecraftScreen.getCtrlShiftAxis().getPositiveAxisValue()*23.5f);
        pGuiGraphics.blit(LEFTPANEL_GUI_TEXTURE, x + 51, y + 29 - yAxis,67,0,7,5);
    }

    private void drawNormalMode(ISpacecraftDataDisplay spacecraftScreen,GuiGraphics pGuiGraphics, int x, int y) {
        int pitch = (int) (spacecraftScreen.getSWAxis().getPositiveAxisValue()*23.5f);
        pGuiGraphics.blit(LEFTPANEL_GUI_TEXTURE, x + 51, y + 29 - pitch,67,0,7,5);

        int roll = (int) (spacecraftScreen.getQEAxis().getPositiveAxisValue()*24f);

        pGuiGraphics.blit(LEFTPANEL_GUI_TEXTURE, x + 21 + roll, y + 10,67,5,5,7);

        int yaw = (int) (spacecraftScreen.getADAxis().getPositiveAxisValue()*24f);
        pGuiGraphics.blit(LEFTPANEL_GUI_TEXTURE, x + 21 + yaw, y + 24,67,5,5,7);

    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {

    }
}
