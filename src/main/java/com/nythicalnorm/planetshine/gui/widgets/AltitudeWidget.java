package com.nythicalnorm.planetshine.gui.widgets;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.gui.screen.ISpacecraftOrbitDataDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;


@OnlyIn(Dist.CLIENT)
public class AltitudeWidget extends AbstractWidget {
    private static final ResourceLocation Altitude_GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(PlanetShine.MODID,
            "textures/gui/altitudewidget.png");

    ScrollingNumber[] scrollingNumbers;
    private static final int AmountOfNumberDisplays = 9;

    public AltitudeWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage);
        scrollingNumbers = new ScrollingNumber[AmountOfNumberDisplays];

        for (int i = 0; i < scrollingNumbers.length; i++) {
            scrollingNumbers[i] = new ScrollingNumber(i * 8);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int x = getX() - 46;
        int y = getY();

        pGuiGraphics.blit(Altitude_GUI_TEXTURE, x, y,0,0,92,28);
        Screen spacecraftScreen = PSClient.get().getScreenManager().getSpacecraftScreen();

        if (spacecraftScreen instanceof ISpacecraftOrbitDataDisplay orbitDataDisplay) {
            this.renderAltitudeNumbers(orbitDataDisplay, pGuiGraphics, x, y);
            pGuiGraphics.blit(Altitude_GUI_TEXTURE, x + 10, y + 15, 96, 0, 5, 13);
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {

    }

    private void renderAltitudeNumbers(ISpacecraftOrbitDataDisplay orbitDataDisplay, GuiGraphics pGuiGraphics, int xPos, int yPos) {
        double altitude = Math.abs(orbitDataDisplay.getAltitude());

        String altitudeMeters =  Long.toString((long) altitude);
        int altitudeUnitIndex;

        for (altitudeUnitIndex = 0; altitudeUnitIndex < 4; altitudeUnitIndex++){
            // 6 significant digits required for meter to km but 9 for other unit transitions
            if (altitudeMeters.length() <= 6 && altitudeUnitIndex == 0) {
                break;
            } else if (altitudeMeters.length() <= 9 && altitudeUnitIndex > 0) {
                break;
            }
            if (altitudeMeters.length() > 3) {
                altitudeMeters = altitudeMeters.substring(0, altitudeMeters.length() - 3);
            } else {
                break;
            }
        }

        //drawing the distance unit
        pGuiGraphics.blit(Altitude_GUI_TEXTURE, xPos + 74, yPos + 2,16, 32 + (altitudeUnitIndex * 12),16,12);

        float deltaTime = Minecraft.getInstance().getDeltaFrameTime();

        for (int i = 0; i < scrollingNumbers.length; i++) {
            int altitudeCharIndex = (altitudeMeters.length() - 1) - i;
            int scrollingNumbersIndex = (scrollingNumbers.length - 1) - i;

            if (altitudeCharIndex >= 0) {
                char num = altitudeMeters.charAt(altitudeCharIndex);
                scrollingNumbers[scrollingNumbersIndex].setNum(Character.getNumericValue(num));
            } else {
                scrollingNumbers[scrollingNumbersIndex].setNum(0);
            }

            scrollingNumbers[scrollingNumbersIndex].drawToScreen(pGuiGraphics, xPos, yPos, deltaTime);
        }
    }

    private static class ScrollingNumber {
        private int currentSetNum;
        private final int xOffset;
        private float currentY;
        private float YGoal;
        private static final float scrollSpeed = 1f;
        private static final int numbersTexHeight = 120;

        public ScrollingNumber(int xOffset) {
            this.xOffset = 2 + xOffset;
            currentSetNum = 0;
            currentY = 0;
        }

        public void setNum(int num) {
            currentSetNum = num % 10;
            YGoal = currentSetNum * 12;
        }

        public void drawToScreen(GuiGraphics pGuiGraphics, int xPos, int yPos, float partialTick) {
            float tickAmount = partialTick * scrollSpeed;
            float actualYGoal = YGoal;

            if (currentY - YGoal > ((float) numbersTexHeight / 2)) {
                actualYGoal = actualYGoal + numbersTexHeight;
            } else if (YGoal - currentY > ((float) numbersTexHeight / 2)) {
                actualYGoal = actualYGoal - numbersTexHeight;
            }

            float distance = Math.abs(actualYGoal - currentY);

            if (currentY < actualYGoal && (currentY + tickAmount) < actualYGoal) {
                currentY += tickAmount*distance;
            } else if (currentY > actualYGoal && (currentY - tickAmount) > actualYGoal) {
                currentY -= tickAmount*distance;
            }

            if (currentY > numbersTexHeight) {
                currentY = currentY - numbersTexHeight;
            } else if (currentY < 0) {
                currentY = currentY + numbersTexHeight;
            }

            int yTex = 32 + Math.round(currentY) ;

            pGuiGraphics.blit(Altitude_GUI_TEXTURE, xPos + xOffset, yPos + 2,0, yTex,8,12);
        }
    }
}
