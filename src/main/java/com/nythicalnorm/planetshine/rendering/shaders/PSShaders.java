package com.nythicalnorm.planetshine.rendering.shaders;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.rendering.map.OrbitDrawer;
import com.nythicalnorm.planetshine.rendering.renderers.AtmosphereRenderer;
import com.nythicalnorm.planetshine.rendering.renderers.PlanetRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
public class PSShaders {
    private static ShaderInstance PLANET_SHADER;
    private static ShaderInstance SKYBOX_SHADER;
    private static ShaderInstance ORBIT_SHADER;

    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), PlanetShine.rl("planetshine_planet"),
                DefaultVertexFormat.POSITION_TEX), shaderInstance -> {
            PLANET_SHADER = shaderInstance;
            PlanetRenderer.setupShader();
        });
        event.registerShader(new ShaderInstance(event.getResourceProvider(), PlanetShine.rl("planetshine_skybox"),
                DefaultVertexFormat.POSITION_TEX), shaderInstance -> {
            SKYBOX_SHADER = shaderInstance;
            AtmosphereRenderer.setupShader();
        });
        event.registerShader(new ShaderInstance(event.getResourceProvider(), PlanetShine.rl("planetshine_orbit"),
                DefaultVertexFormat.POSITION_COLOR), shaderInstance -> {
            ORBIT_SHADER = shaderInstance;
            OrbitDrawer.setupShader();
        });
    }

    public static ShaderInstance getPlanetShader(){
        return PLANET_SHADER;
    }

    public static ShaderInstance getSkyboxShader(){
        return SKYBOX_SHADER;
    }

    public static ShaderInstance getOrbitShader() {
        return ORBIT_SHADER;
    }
}
