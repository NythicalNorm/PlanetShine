package com.nythicalnorm.planetshine.planettexgen.handlers;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.network.textures.ClientboundLodTexturePacket;
import com.nythicalnorm.planetshine.network.textures.ClientboundPlanetTexturePacket;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.planettexgen.PlanetGradient;
import com.nythicalnorm.planetshine.planettexgen.lod_tex.LodTexGenTask;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.storage.PSDataPackManager;
import com.nythicalnorm.planetshine.storage.SpacecraftDataStorage;
import com.nythicalnorm.planetshine.util.calculations.LodTexCalc;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.*;

public class PlanetTexHandler {
    private static final String planetTexturesPath = "textures";

    private static File planetsDir;
    private static ExecutorService texExecuter;

    public void loadOrCreatePlanetTex(MinecraftServer server, SolarSystem planets, Path modSaveFolder) {

        Path planetsTexturesPath = modSaveFolder.resolve(planetTexturesPath);
        PlanetTexHandler.planetsDir = SpacecraftDataStorage.getOrCreateDir(planetsTexturesPath);

        if (PlanetTexHandler.planetsDir == null) {
            return;
        }

        RandomSource randomSource = RandomSource.create(server.getLevel(Level.OVERWORLD).getSeed());
        texExecuter = Executors.newSingleThreadExecutor();

        server.getPlayerList().broadcastSystemMessage(Component.translatable("planetshine.state.planetgen_start"), true);

        for (CelestialBody celestialBody : planets.getAllPlanetaryBodies().values()) {
            Path celestialBodyDir = planetsTexturesPath.resolve(celestialBody.getName());
            SpacecraftDataStorage.getOrCreateDir(celestialBodyDir);
            PlanetGradient planetGradient = PSDataPackManager.getPlanetGradient(celestialBody.getName());

            if (planetGradient == null) {
                PlanetShine.logError("Can't load a texture data pack for planet: " + celestialBody.getName() + ", Texture will not be generated");
                continue;
            }

            CompletableFuture<byte[]> planetImgData = CompletableFuture.supplyAsync(
                    new WholePlanetTexGenTask(celestialBodyDir, celestialBody.getName(), randomSource, planetGradient), texExecuter);

            celestialBody.getCelestialServerData().setPlanetTextureFolder(celestialBodyDir);
            celestialBody.getCelestialServerData().setPlanetMainTexBytes(planetImgData);

            planetImgData.thenRun(() -> {
                server.getPlayerList().broadcastSystemMessage(Component.translatable("planetshine.state.planetgen_end",
                        celestialBody.getName()), true);
            });
        }
    }

    public static ExecutorService getTexExecuter() {
        return texExecuter;
    }


    public static void sendBiomeTexToPlayer(ServerPlayer player, CelestialBody playerOnPlanet) {
        if (playerOnPlanet == null) {
            return;
        }

        Vec3 plrPos = player.position();
        int texturePixelSize = LodTexCalc.getTexturePixelSize(playerOnPlanet);
        Vector2i texPos = LodTexCalc.getPlanetTexCoordinates(plrPos, texturePixelSize);

        File biomeTexLocation = getFilePath(playerOnPlanet.getCelestialServerData().getPlanetTextureFolder(), texPos.x, texPos.y);

        CompletableFuture<byte[]> biomeTex = CompletableFuture.supplyAsync(
                new LodTexGenTask(playerOnPlanet.getCelestialServerData().getServerLevel(), 0,  texPos.x, texPos.y, texturePixelSize, biomeTexLocation), texExecuter);

        int index = texPos.x + (texPos.y * LodTexCalc.texQuadsPerCubeCell);

        biomeTex.thenAccept(texBytes -> {
            PacketHandler.sendToPlayer(new ClientboundLodTexturePacket(playerOnPlanet.getDimension(), index, 0, texBytes), player);
        });
    }

    private static File getFilePath(Path rootDir, int xIndex, int zIndex) {

        String fileName = xIndex + "_" + zIndex;
        Path biomeTexPath = rootDir.resolve(fileName + ".png");
        return new File(biomeTexPath.toUri());
    }

    public void sendAllTexToPlayer(ServerPlayer player, Map<OrbitId, CelestialBody> allPlanetaryBodies) {
        for (CelestialBody celestialBody : allPlanetaryBodies.values()) {
            celestialBody.getCelestialServerData().getPlanetMainTexBytes().thenAccept(texBytes -> sendToPlayer(player,
                    celestialBody.getOrbitId(), texBytes));
        }
    }

    private void sendToPlayer(ServerPlayer player, OrbitId planetID, byte[] texture) {
        PacketHandler.sendToPlayer(new ClientboundPlanetTexturePacket(planetID, texture), player);
    }
}
