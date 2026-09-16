package com.nythicalnorm.planetshine.storage;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.solarsystem.OrbitalBodyTypeRegistry;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalBodyType;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

public class NBTEncoders {
    public static CompoundTag putVector3d(Vector3dc vector) {
        CompoundTag vectorTag = new CompoundTag();
        vectorTag.putDouble("x", vector.x());
        vectorTag.putDouble("y", vector.y());
        vectorTag.putDouble("z", vector.z());
        return vectorTag;
    }

    public static Vector3d getVector3d(CompoundTag tag) {
        return new Vector3d(
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getDouble("z")
        );
    }

    public static CompoundTag putVector3f(Vector3f vector) {
        CompoundTag vectorTag = new CompoundTag();
        vectorTag.putFloat("x", vector.x);
        vectorTag.putFloat("y", vector.y);
        vectorTag.putFloat("z", vector.z);
        return vectorTag;
    }

    public static Vector3f getVector3f(CompoundTag tag) {
        return new Vector3f(
                tag.getFloat("x"),
                tag.getFloat("y"),
                tag.getFloat("z")
        );
    }

    public static CompoundTag putVector2i(Vector2ic vector) {
        CompoundTag vector2iTag = new CompoundTag();
        vector2iTag.putInt("x", vector.x());
        vector2iTag.putInt("y", vector.y());
        return vector2iTag;
    }
    public static Vector2i getVector2i(CompoundTag tag) {
        return new Vector2i(tag.getInt("x"), tag.getInt("y"));
    }

    public static CompoundTag putQuaternionfc(Quaternionfc quaternionf) {
        CompoundTag quaternionTag = new CompoundTag();
        quaternionTag.putDouble("x", quaternionf.x());
        quaternionTag.putDouble("y", quaternionf.y());
        quaternionTag.putDouble("z", quaternionf.z());
        quaternionTag.putDouble("w", quaternionf.w());
        return quaternionTag;
    }

    public static Quaternionf getQuaternionf(CompoundTag tag) {
        return new Quaternionf(
                tag.getFloat("x"),
                tag.getFloat("y"),
                tag.getFloat("z"),
                tag.getFloat("w")
        );
    }

    public static CompoundTag putOrbitalElements(@Nullable OrbitalElements orbitalElements) {
        CompoundTag elementsTag = new CompoundTag();
        if (orbitalElements == null) {
            return elementsTag;
        }

        elementsTag.putDouble("major_axis", orbitalElements.getSemiMajorAxis());
        elementsTag.putDouble("eccentricity", orbitalElements.getEccentricity());
        elementsTag.putLong("periapsis_time", orbitalElements.getPeriapsisTime());

        elementsTag.putDouble("inclination", orbitalElements.getInclination());
        elementsTag.putDouble("argument_periapsis", orbitalElements.getArgumentOfPeriapsis());
        elementsTag.putDouble("longitude", orbitalElements.getLongitudeOfAscendingNode());
        elementsTag.putDouble("parent_mass", orbitalElements.getParentMass());

        return elementsTag;
    }

    public static OrbitalElements getOrbitalElements(CompoundTag tag) {
        return new OrbitalElements(
                tag.getDouble("major_axis"),
                tag.getDouble("eccentricity"),
                tag.getLong("periapsis_time"),

                tag.getDouble("inclination"),
                tag.getDouble("argument_periapsis"),
                tag.getDouble("longitude"),
                tag.getDouble("parent_mass")
        );
    }

    public static CompoundTag putOrbitalBody(OrbitalBody orbitalBody) {
       return orbitalBody.getType().get().encodeToNBT(orbitalBody);
    }

    public static @Nullable OrbitalBody getOrbitalBody(CompoundTag tag) {
       String resourceLoc = tag.getString("type_name");
        OrbitalBodyType<? extends OrbitalBody, ? extends OrbitalBody.Builder<?>> orbitalBodyType = null;

       if (resourceLoc.contains(":")) {
           orbitalBodyType = OrbitalBodyTypeRegistry.getType(ResourceLocation.parse(resourceLoc));
       } else {
           orbitalBodyType = OrbitalBodyTypeRegistry.getType(PlanetShine.rl(resourceLoc));
       }

       if (orbitalBodyType != null) {
           return orbitalBodyType.decodeFromNBT(tag).build();
       } else {
           return null;
       }
    }
}
