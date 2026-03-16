package com.nythicalnorm.planetshine.network;

import com.nythicalnorm.planetshine.PlanetShine;
import com.nythicalnorm.planetshine.network.orbitaldata.*;
import com.nythicalnorm.planetshine.network.spacecraft.ServerboundPlayerHostVelUpdate;
import com.nythicalnorm.planetshine.network.textures.ClientboundLodTexturePacket;
import com.nythicalnorm.planetshine.network.textures.ClientboundPlanetTexturePacket;
import com.nythicalnorm.planetshine.network.time.ClientboundSolarSystemTimeUpdate;
import com.nythicalnorm.planetshine.network.time.ClientboundTimeWarpUpdate;
import com.nythicalnorm.planetshine.network.time.ServerboundTimeWarpChange;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(PlanetShine.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;

        INSTANCE.messageBuilder(ClientboundLoginPSClientStart.class, id++)
                .encoder(ClientboundLoginPSClientStart::encode)
                .decoder(ClientboundLoginPSClientStart::new)
                .consumerMainThread(ClientboundLoginPSClientStart::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundLoginEntityBodiesList.class, id++)
                .encoder(ClientboundLoginEntityBodiesList::encode)
                .decoder(ClientboundLoginEntityBodiesList::new)
                .consumerMainThread(ClientboundLoginEntityBodiesList::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundSolarSystemTimeUpdate.class, id++)
                .encoder(ClientboundSolarSystemTimeUpdate::encode)
                .decoder(ClientboundSolarSystemTimeUpdate::new)
                .consumerMainThread(ClientboundSolarSystemTimeUpdate::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundOrbitSOIChange.class, id++)
                .encoder(ClientboundOrbitSOIChange::encode)
                .decoder(ClientboundOrbitSOIChange::new)
                .consumerMainThread(ClientboundOrbitSOIChange::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundLocalPlayerJoinOrbital.class, id++)
                .encoder(ClientboundLocalPlayerJoinOrbital::encode)
                .decoder(ClientboundLocalPlayerJoinOrbital::new)
                .consumerMainThread(ClientboundLocalPlayerJoinOrbital::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundEntityBodyJoinOrbital.class, id++)
                .encoder(ClientboundEntityBodyJoinOrbital::encode)
                .decoder(ClientboundEntityBodyJoinOrbital::new)
                .consumerMainThread(ClientboundEntityBodyJoinOrbital::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundOrbitChange.class, id++)
                .encoder(ClientboundOrbitChange::encode)
                .decoder(ClientboundOrbitChange::new)
                .consumerMainThread(ClientboundOrbitChange::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundOrbitRemove.class, id++)
                .encoder(ClientboundOrbitRemove::encode)
                .decoder(ClientboundOrbitRemove::new)
                .consumerMainThread(ClientboundOrbitRemove::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundHostOrbitSet.class, id++)
                .encoder(ClientboundHostOrbitSet::encode)
                .decoder(ClientboundHostOrbitSet::new)
                .consumerMainThread(ClientboundHostOrbitSet::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundSetOrbitIntercept.class, id++)
                .encoder(ClientboundSetOrbitIntercept::encode)
                .decoder(ClientboundSetOrbitIntercept::new)
                .consumerMainThread(ClientboundSetOrbitIntercept::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundTimeWarpUpdate.class, id++)
                .encoder(ClientboundTimeWarpUpdate::encode)
                .decoder(ClientboundTimeWarpUpdate::new)
                .consumerMainThread(ClientboundTimeWarpUpdate::handle)
                .add();

        //Textures
        INSTANCE.messageBuilder(ClientboundPlanetTexturePacket.class, id++)
                .encoder(ClientboundPlanetTexturePacket::encode)
                .decoder(ClientboundPlanetTexturePacket::new)
                .consumerMainThread(ClientboundPlanetTexturePacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundLodTexturePacket.class, id++)
                .encoder(ClientboundLodTexturePacket::encode)
                .decoder(ClientboundLodTexturePacket::new)
                .consumerMainThread(ClientboundLodTexturePacket::handle)
                .add();

        // Server to Client
        INSTANCE.messageBuilder(ServerboundPlayerHostVelUpdate.class, id++)
                .encoder(ServerboundPlayerHostVelUpdate::encode)
                .decoder(ServerboundPlayerHostVelUpdate::new)
                .consumerMainThread(ServerboundPlayerHostVelUpdate::handle)
                .add();

        INSTANCE.messageBuilder(ServerboundTimeWarpChange.class, id++)
                .encoder(ServerboundTimeWarpChange::encode)
                .decoder(ServerboundTimeWarpChange::new)
                .consumerMainThread(ServerboundTimeWarpChange::handle)
                .add();
    }

    public static void sendToServer(Object msg) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), msg);
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToAllPlayersExcept(Object msg, ServerPlayer playerExcluded, List<ServerPlayer> playerList) {
        for (ServerPlayer player : playerList) {
            if (!player.equals(playerExcluded)) {
                INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
            }
        }
    }

    public static void sendToAllClients(Object msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }
}
