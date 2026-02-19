package com.nythicalnorm.planetshine.gui.screen;

import com.nythicalnorm.planetshine.gui.input.PlayerInputDirection;

public interface ISpacecraftDataDisplay {
    float getThrottleSetting();

    boolean isDockingMode();

    boolean isRCS();

    boolean isSAS();

    PlayerInputDirection getADAxis();

    PlayerInputDirection getSWAxis();

    PlayerInputDirection getCtrlShiftAxis();

    PlayerInputDirection getQEAxis();
}
