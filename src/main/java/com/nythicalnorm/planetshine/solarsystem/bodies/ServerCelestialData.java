package com.nythicalnorm.planetshine.solarsystem.bodies;

import com.nythicalnorm.planetshine.planettexgen.PlanetGradient;
import net.minecraft.server.level.ServerLevel;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ServerCelestialData {
    private CompletableFuture<byte[]> planetTexBytes;
    private PlanetGradient planetGradient;
    private Path planetFolder;
    private File planetDataFile;
    private ServerLevel level;

    public CompletableFuture<byte[]> getPlanetMainTexBytes() {
        return planetTexBytes;
    }

    public void setPlanetMainTexBytes(CompletableFuture<byte[]> bytes) {
        planetTexBytes = bytes;
    }

    public Path getPlanetTextureFolder() {
        return planetFolder;
    }

    public void setPlanetTextureFolder(Path folder) {
        planetFolder = folder;
    }

    public File getPlanetDataFile() {
        return planetDataFile;
    }

    public void setPlanetDataFile(File dataFile) {
        this.planetDataFile = dataFile;
    }

    public ServerLevel getServerLevel() {
        return level;
    }

    public void setServerLevel(ServerLevel level) {
        this.level = level;
    }

    public PlanetGradient getPlanetGradient() {
        return planetGradient;
    }

    public void setPlanetGradient(PlanetGradient planetGradient) {
        this.planetGradient = planetGradient;
    }
}
