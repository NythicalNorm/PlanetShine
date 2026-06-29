package com.nythicalnorm.planetshine.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.util.calculations.TimeCalc;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Collection;

public class PSTeleportCommand {
    public PSTeleportCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ps-tp").requires((stack) -> {
            return stack.hasPermission(2);
        })
            .then(Commands.argument("targets", EntityArgument.entities())
                .then(Commands.argument("planets", PlanetArgument.planetArgument())
                    .then(Commands.argument("semi-major_axis", DoubleArgumentType.doubleArg())
                        .then(Commands.argument("eccentricity", DoubleArgumentType.doubleArg())
                            .then(Commands.argument("inclination", DoubleArgumentType.doubleArg())
                                    .executes((stack) -> {
                                    return TeleportToOrbit(stack.getSource(), EntityArgument.getEntities(stack, "targets"),
                                    stack.getArgument("planets", String.class),
                                    DoubleArgumentType.getDouble(stack, "semi-major_axis"),
                                    DoubleArgumentType.getDouble(stack, "eccentricity"),
                                    DoubleArgumentType.getDouble(stack, "inclination"));
                                })
                            )
                        )
                    )
                )
            )
        );
    }

    private int TeleportToOrbit(CommandSourceStack pSource, Collection<? extends Entity> pTargets, String body,
                                double semiMajorAxisInput, double eccentricity, double inclination) {
        PSServer.getInstance().ifPresent(psServer -> {
            CelestialBody planet = psServer.getSolarSystem().getPlanet(body);
            for(Entity entity : pTargets) {
                if (entity instanceof ServerPlayer && planet != null) {
                    double semiMajorAxis = (semiMajorAxisInput*1000d) + planet.getRadius();
                    if (semiMajorAxisInput < 0) {
                        semiMajorAxis = (semiMajorAxisInput*1000d) - planet.getRadius();
                    }
                    long startingTime = psServer.getCurrentTime() + TimeCalc.timeDoubleToLong(20000f);
                    OrbitalElements orbitalElement = new OrbitalElements(semiMajorAxis, eccentricity, startingTime, inclination, 0d, 0d, planet.getMass());
                    Vector3d relativePosition = new Vector3d();
                    Vector3d relativeVelocity = new Vector3d();

                    orbitalElement.ToCartesian(startingTime, relativePosition, relativeVelocity);

                    LoadedShip shipMountedTo = VSGameUtilsKt.getShipMountedTo(entity);

                    if (shipMountedTo == null) {
                        psServer.playerTeleportToOrbit(planet, (ServerPlayer) entity, orbitalElement);
                    } else {
                        psServer.shipTeleportToOrbit(planet, (LoadedServerShip) shipMountedTo, orbitalElement,
                                relativePosition, relativeVelocity, shipMountedTo.getTransform().getRotation(), shipMountedTo.getAngularVelocity());
                    }
                }
            }
            pSource.sendSuccess(() -> {
                return Component.translatable("planetshine.commands.dimTeleport");
            }, true);
        });
        return 1;
    }
}
