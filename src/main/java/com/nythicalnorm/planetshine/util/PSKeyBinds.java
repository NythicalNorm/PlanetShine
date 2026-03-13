package com.nythicalnorm.planetshine.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class PSKeyBinds {
    public static final String KEY_CATEGORY_PLANETSHINE = "key.category.planetshine.main";
    public static final String KEY_INCREASE_TIME_WARP = "key.planetshine.increase_time_warp";
    public static final String KEY_DECREASE_TIME_WARP = "key.planetshine.decrease_time_warp";
    public static final String KEY_OPEN_SOLAR_SYSTEM_MAP = "key.planetshine.open_solar_system_map";
    public static final String KEY_OPEN_SPACECRAFT_HUD = "key.planetshine.open_spacecraft_hud_key";
    public static final String KEY_PLAYER_SPACE_ROTATE = "key.planetshine.player_space_rotate";
    public static final String KEY_CHANGE_SPACECRAFT_VIEW = "key.planetshine.change_spacecraft_view";

    public static final KeyMapping INC_TIME_WARP_KEY = new KeyMapping(KEY_INCREASE_TIME_WARP, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_PERIOD, KEY_CATEGORY_PLANETSHINE);

    public static final KeyMapping DEC_TIME_WARP_KEY = new KeyMapping(KEY_DECREASE_TIME_WARP, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_COMMA, KEY_CATEGORY_PLANETSHINE);

    public static final KeyMapping OPEN_SOLAR_SYSTEM_MAP_KEY = new KeyMapping(KEY_OPEN_SOLAR_SYSTEM_MAP, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, KEY_CATEGORY_PLANETSHINE);

    public static final KeyMapping OPEN_SPACECRAFT_HUD_KEY = new KeyMapping(KEY_OPEN_SPACECRAFT_HUD, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, KEY_CATEGORY_PLANETSHINE);

    public static final KeyMapping CHANGE_SPACECRAFT_VIEW_KEY = new KeyMapping(KEY_CHANGE_SPACECRAFT_VIEW, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, KEY_CATEGORY_PLANETSHINE);

    public static final KeyMapping PLAYER_SPACE_ROTATE_KEY = new KeyMapping(KEY_PLAYER_SPACE_ROTATE, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY_PLANETSHINE);
}
