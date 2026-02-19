package com.nythicalnorm.planetshine.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class PSKeyBinds {
    public static final String KEY_CATEGORY_VOXEL_SPACE_PROGRAM = "key.category.planetshine.main";
    public static final String KEY_INCREASE_TIME_WARP = "key.planetshine.increase_time_warp";
    public static final String KEY_DECREASE_TIME_WARP = "key.planetshine.decrease_time_warp";
    public static final String KEY_OPEN_SOLAR_SYSTEM_MAP = "key.planetshine.open_solar_system_map";
    public static final String KEY_USE_PLAYER_JETPACK = "key.planetshine.use_player_jetpack_key";

    public static final KeyMapping INC_TIME_WARP_KEY = new KeyMapping(KEY_INCREASE_TIME_WARP, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_PERIOD, KEY_CATEGORY_VOXEL_SPACE_PROGRAM);

    public static final KeyMapping DEC_TIME_WARP_KEY = new KeyMapping(KEY_DECREASE_TIME_WARP, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_COMMA, KEY_CATEGORY_VOXEL_SPACE_PROGRAM);

    public static final KeyMapping OPEN_SOLAR_SYSTEM_MAP_KEY = new KeyMapping(KEY_OPEN_SOLAR_SYSTEM_MAP, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, KEY_CATEGORY_VOXEL_SPACE_PROGRAM);

    public static final KeyMapping USE_PLAYER_JETPACK_KEY = new KeyMapping(KEY_USE_PLAYER_JETPACK, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, KEY_CATEGORY_VOXEL_SPACE_PROGRAM);
}
