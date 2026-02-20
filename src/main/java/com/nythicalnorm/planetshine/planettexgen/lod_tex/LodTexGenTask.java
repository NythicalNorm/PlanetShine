package com.nythicalnorm.planetshine.planettexgen.lod_tex;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.planettexgen.TexGenTask;
import com.nythicalnorm.planetshine.util.calculations.LodTexCalc;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class LodTexGenTask extends TexGenTask {
    private final ServerLevel level;
    private final int texSizeIndex;
    private final int xIndex;
    private final int zIndex;
    private final int texturePixelSize;
    private final File biomeTexLoc;

    public LodTexGenTask(ServerLevel level, int texSize, int xIndex, int zIndex, int texturePixelSize, File saveFilePath) {
        this.level = level;
        this.texSizeIndex = texSize;
        this.xIndex = xIndex;
        this.zIndex = zIndex;
        this.texturePixelSize = texturePixelSize;
        this.biomeTexLoc = saveFilePath;
    }

    public BufferedImage generateBiomeTex() {
        int textureRes = LodTexCalc.textureResolution;

        BufferedImage genTexture = new BufferedImage(textureRes, textureRes, BufferedImage.TYPE_INT_RGB);
        long beforeTimes = Util.getNanos();

        double minPosX = xIndex*texturePixelSize - ((double) texturePixelSize / 2);
        double minPosZ = zIndex*texturePixelSize - ((double) texturePixelSize / 2);
        BiomeManager biomeManager = level.getBiomeManager();

        for (int z = 0; z < textureRes; z++) {
            for (int x = 0; x < textureRes; x++) {
                int xDist = (int) Math.floor(minPosX + (((float)x / textureRes) * texturePixelSize));
                int zDist = (int) Math.floor(minPosZ + (((float)z / textureRes) * texturePixelSize));
                Holder<Biome> biomeAtPos = biomeManager.getNoiseBiomeAtQuart(QuartPos.fromBlock(xDist), 64, QuartPos.fromBlock(zDist));
                int biomeColor = BiomeColorHolder.getColorForBiome(biomeAtPos.get());
                genTexture.setRGB(x, z, biomeColor);
            }
        }

        long currentTime = Util.getNanos() - beforeTimes;
        PlanetShine.log("time Took for biomeTex: " + currentTime);
        return genTexture;
    }

    @Override
    public byte[] get() {
        if (!biomeTexLoc.exists()) {
            BufferedImage planetMap = generateBiomeTex();

            try (FileOutputStream fileWriter = new FileOutputStream(biomeTexLoc)) {
                byte[] imageBytes = convertBufferedImageToPngBytes(planetMap);
                fileWriter.write(imageBytes);
                return imageBytes;
            } catch (IOException e) {
                PlanetShine.logError("Can't write " + " Textures to file");
            }
        } else {
            try {
                return Files.readAllBytes(biomeTexLoc.toPath());
            } catch (IOException e) {
                PlanetShine.logError("Can't load textures");
            }
        }
        return null;
    }
}
