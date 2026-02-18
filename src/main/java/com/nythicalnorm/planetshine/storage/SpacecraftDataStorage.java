package com.nythicalnorm.planetshine.storage;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.solarsystem.HostSpaceManager;
import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.solarsystem.SolarSystem;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.bodies.ServerCelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;

public class SpacecraftDataStorage {
    public static final String modSaveDirPath = "planetshine";
    public final File hostOrbitSaveFile;
    private final Path modSaveFolder;

    public SpacecraftDataStorage(MinecraftServer server, SolarSystem solarSystem) {
        this.modSaveFolder = server.getWorldPath(LevelResource.ROOT).resolve(SpacecraftDataStorage.modSaveDirPath);
        getOrCreateDir(modSaveFolder);
        this.hostOrbitSaveFile = new File(this.modSaveFolder.resolve("host_spaces.dat").toUri());

        for(CelestialBody celestialBody: solarSystem.getAllPlanetaryBodies().values()) {
            String fileName = celestialBody.getName().concat(".dat");
            File dataFile = new File(this.modSaveFolder.resolve(fileName).toUri());
            ((ServerCelestialBody) celestialBody).setPlanetDataFile(dataFile);
        }
    }

    public Path getModSaveFolder() {
        return modSaveFolder;
    }

    public void readSpacecraftData(SolarSystem solarSystem) {
        for(CelestialBody celestialBody: solarSystem.getAllPlanetaryBodies().values()) {
            File planetFileLoc = ((ServerCelestialBody) celestialBody).getPlanetDataFile();
            if (planetFileLoc.exists()) {
                ListTag spacecraftList = readList(planetFileLoc);
                if (spacecraftList == null) {
                    continue;
                }

                for (Tag tag : spacecraftList) {
                    if (tag instanceof CompoundTag compoundTag) {
                        OrbitalBody orbitalBody = NBTEncoders.getOrbitalBody(compoundTag);
                        solarSystem.playerJoinedOrbital(celestialBody, orbitalBody);
                    }
                }
            }
        }
    }

    public Map<OrbitId, Vector2ic> readHostSpaces() {
        Map<OrbitId, Vector2ic> allRegisteredHostSpaces = new Object2ObjectOpenHashMap<>();

        if (hostOrbitSaveFile.exists()) {
            ListTag hostList = readList(hostOrbitSaveFile);
            if (hostList == null) {
                return allRegisteredHostSpaces;
            }

            for (Tag tag : hostList) {
                if (tag instanceof CompoundTag compoundTag) {
                    OrbitId id = new OrbitId(compoundTag, "orbit_id");
                    Vector2i pos = NBTEncoders.getVector2i(compoundTag.getCompound("pos"));
                    allRegisteredHostSpaces.put(id, pos);
                }
            }
        }

        return allRegisteredHostSpaces;
    }

    public void saveSpacecraft(SolarSystem solarSystem) {
        for(CelestialBody celestialBody: solarSystem.getAllPlanetaryBodies().values()) {
            File planetFileLoc = ((ServerCelestialBody) celestialBody).getPlanetDataFile();
            ListTag spacecraftTags = new ListTag();

            for (OrbitalBody orbitalBody : celestialBody.getChildren()) {
                if (orbitalBody instanceof EntityOrbitBody) {
                    CompoundTag orbitalTag = NBTEncoders.putOrbitalBody(orbitalBody);
                    spacecraftTags.add(orbitalTag);
                }
            }

            writeList(planetFileLoc, spacecraftTags);
        }
    }

    public void saveHostSpaces(HostSpaceManager hostSpaceManager) {
        if (hostSpaceManager.isDirty()) {
            Map<OrbitId, Vector2ic> registeredHostSpaces = hostSpaceManager.getDataToSave();
            ListTag hostOrbit = new ListTag();

            for (Map.Entry<OrbitId, Vector2ic> entry : registeredHostSpaces.entrySet()) {
                CompoundTag entryTag = new CompoundTag();
                entry.getKey().encodeToNBT(entryTag, "orbit_id");
                entryTag.put("pos", NBTEncoders.putVector2i(entry.getValue()));

                hostOrbit.add(entryTag);
            }
            writeList(hostOrbitSaveFile, hostOrbit);
        }
    }

    private void writeList(File dataFileLoc, ListTag tag) {
        try (
                FileOutputStream fileoutputstream = new FileOutputStream(dataFileLoc);
                DataOutputStream dataoutputstream = new DataOutputStream(fileoutputstream)
        ) {
            tag.write(dataoutputstream);
        } catch (IOException exception) {
            PlanetShine.logError("Can't save planetData file to " + dataFileLoc.getPath());
            exception.printStackTrace();
        }
    }

    private ListTag readList(File dataFileLoc) {
        if (!dataFileLoc.exists()) {
            return null;
        } else {
            ListTag listTag = null;
            try (
                    FileInputStream fileinputstream = new FileInputStream(dataFileLoc);
                    DataInputStream datainputstream = new DataInputStream(fileinputstream)
            ) {
                listTag = ListTag.TYPE.load(datainputstream, 0, NbtAccounter.UNLIMITED);
            } catch (IOException exception) {
                PlanetShine.logError("Can't load planetData file from " + dataFileLoc.getPath() + ", is it corrupted? :( sowwy.. ");
                exception.printStackTrace();
            }

            return listTag;
        }
    }

    public static File getOrCreateDir (Path folderPath) {
        File folderDir = new File(folderPath.toUri());

        if (!folderDir.exists()) {
            boolean wasCreated = folderDir.mkdir();
            if (!wasCreated) {
                PlanetShine.logError(folderDir.getPath() + " Directory Creation Failed.");
                return null;
            }
        }
        return folderDir;
    }
}
