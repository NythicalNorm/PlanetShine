package com.nythicalnorm.voxelspaceprogram.rendering.renderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nythicalnorm.voxelspaceprogram.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.voxelspaceprogram.rendering.map.MapRenderer;
import com.nythicalnorm.voxelspaceprogram.rendering.map.OrbitDrawer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class MapRenderableOrbit extends MapRenderable {
    OrbitalBody orbitOf;

    public MapRenderableOrbit(MapRelativeState mapRelativeState, OrbitalBody orbitOf, OrbitalBody parentBody) {
        super(mapRelativeState, parentBody);
        this.orbitOf = orbitOf;
    }

    @Override
    public Vector3f render(PoseStack poseStack, Matrix4f projectionMatrix) {
        OrbitDrawer.drawOrbit(orbitOf, MapRenderer.SCALE_FACTOR, poseStack, projectionMatrix);
        return null;
    }
}
