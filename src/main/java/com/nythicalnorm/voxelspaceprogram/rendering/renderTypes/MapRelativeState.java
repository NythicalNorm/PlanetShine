package com.nythicalnorm.voxelspaceprogram.rendering.renderTypes;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum MapRelativeState {
    FocusedBody,
    RelativePos,
    AbsolutePos,
    FocusedBodyParent,
    AlwaysParentRelative
}
