package com.nythicalnorm.planetshine.spacecraft.spaceship;

import com.nythicalnorm.planetshine.PSClient;
import com.nythicalnorm.planetshine.rendering.map.IconRenderer;
import com.nythicalnorm.planetshine.rendering.map.MapIconRenderable;
import com.nythicalnorm.planetshine.spacecraft.hostspace.OrbitHostAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2i;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.world.ClientShipWorld;
import org.valkyrienskies.mod.api.ValkyrienSkies;

public class ClientSpaceshipBody extends AbstractSpaceshipBody implements MapIconRenderable {
    private Vector2i mapPos;

    public ClientSpaceshipBody(ShipOrbitBuilder shipOrbitBuilder) {
        super(shipOrbitBuilder, true);
    }

    @Override
    public void init() {
        super.init();
        if (this.getBody() != null) {
            return;
        }
        ClientShipWorld clientShipWorld = ValkyrienSkies.api().getClientShipWorld(Minecraft.getInstance());
        if (clientShipWorld != null) {
            this.setBody(clientShipWorld.getLoadedShips().getById(this.id.getShipID()));
        }
    }

    @Override
    public OrbitHostAccessor getHostSpaceAccess() {
        return PSClient.get().getCurrentHostSpace();
    }

    @Override
    public Vector2i getLatestMapPos() {
        return mapPos;
    }

    @Override
    public void setLatestMapPos(Vector2i pos) {
        this.mapPos = pos;
    }

    @Override
    public void drawIcon(GuiGraphics graphics, Vector2i screenPos, int i) {
        this.setLatestMapPos(screenPos);
        IconRenderer.drawIcon(graphics, IconRenderer.DEFAULT_SPACESHIP_ICON, screenPos);
    }

    @Override
    public boolean shouldDraw() {
        OrbitHostAccessor orbitHostAccessor = PSClient.get().getCurrentHostSpace();
        if (this.isBodyEntityLoaded() && orbitHostAccessor != null && orbitHostAccessor.getOrbitIdOfHost().equals(this.getOrbitId())) {
            Vector3dc localPlayerPos = PSClient.get().getPlayerOrbit().getMcPosition();
            return !this.body.getWorldAABB().containsPoint(localPlayerPos);
        }

        return true;
    }
}
