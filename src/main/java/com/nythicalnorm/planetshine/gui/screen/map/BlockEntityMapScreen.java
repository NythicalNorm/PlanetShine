package com.nythicalnorm.planetshine.gui.screen.map;

import com.nythicalnorm.planetshine.gui.screen.MapSolarSystemScreen;

public class BlockEntityMapScreen extends MapSolarSystemScreen {
    public BlockEntityMapScreen() {
        super(false, null);
    }

    @Override
    public boolean overrideTimeWarpAllowance() {
        return true;
    }

    @Override
    public boolean resetKeysOnScreenOpen() {
        return true;
    }
}