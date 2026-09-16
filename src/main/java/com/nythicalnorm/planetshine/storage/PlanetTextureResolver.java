package com.nythicalnorm.planetshine.storage;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.planettexgen.PlanetGradient;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.Deserializers;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Map;

public class PlanetTextureResolver extends SimpleJsonResourceReloadListener {
    private static final Logger logger = PlanetShine.getLogger();
    public static final Gson GSON_INSTANCE = Deserializers.createFunctionSerializer().create();

    public PlanetTextureResolver() {
        super(GSON_INSTANCE, "ps_texture_gen");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, @NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
        Map<String, PlanetGradient> planetGradientMap = new Object2ObjectOpenHashMap<>();

        pObject.forEach((key, element) -> {
            if (key == null || element == null) {
                return;
            }
            try {
                JsonObject jsonObject = element.getAsJsonObject();
                String name = jsonObject.get("name").getAsString();
                PlanetGradient planetGradient = PlanetGradient.readFromJson(jsonObject);
                planetGradientMap.put(name, planetGradient);
            } catch (Exception e) {
                logger.error("Unable to parse datapack for planetary body {}", key.getPath());
                e.printStackTrace();
            }
        });

        PSDataPackManager.textureGenDatapackLoaded(planetGradientMap);
    }
}
