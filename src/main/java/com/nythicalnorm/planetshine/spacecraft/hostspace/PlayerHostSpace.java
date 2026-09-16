package com.nythicalnorm.planetshine.spacecraft.hostspace;

import com.nythicalnorm.planetshine.solarsystem.OrbitId;
import com.nythicalnorm.planetshine.spacecraft.EntityOrbitBody;
import com.nythicalnorm.planetshine.spacecraft.player.AbstractPlayerOrbitBody;
import net.minecraft.world.entity.Entity;
import org.joml.Vector2ic;

import java.util.function.Consumer;

public class PlayerHostSpace extends OrbitHostSpace {
    public PlayerHostSpace(OrbitId orbitIdOfHost, Vector2ic originPos, EntityOrbitBody<?> entityOrbitBody) {
        super(orbitIdOfHost, originPos, entityOrbitBody);
    }

    @Override
    public void affectMCEntities(Consumer<Entity> orbitBodyConsumer) {
        super.affectMCEntities(orbitBodyConsumer);
        if (this.hostBody.isBodyEntityLoaded()) {
            orbitBodyConsumer.accept(((AbstractPlayerOrbitBody)this.hostBody).getBody());
        }
    }
}
