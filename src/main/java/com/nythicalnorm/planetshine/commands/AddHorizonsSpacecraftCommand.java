package com.nythicalnorm.planetshine.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.horizons.JPLHorizons;
import com.nythicalnorm.planetshine.solarsystem.bodies.CelestialBody;
import com.nythicalnorm.planetshine.spacecraft.irlship.AbstractIrlSpacecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class AddHorizonsSpacecraftCommand {
    public AddHorizonsSpacecraftCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("horizons-add").requires((stack) -> {
                            return stack.hasPermission(0);
                        })
            .then(Commands.argument("spacecraft_name", StringArgumentType.word())
                .then(Commands.argument("parent_planet", PlanetArgument.planetArgument())
                    .executes((stack) -> {
                        return addHorizonsSpacecraft(stack.getSource(), StringArgumentType.getString(stack, "spacecraft_name"),
                                stack.getArgument("parent_planet", String.class));
                    })
                )
            )
        );
    }

    private int addHorizonsSpacecraft(CommandSourceStack pSource, String spacecraftName, String body) {
        if (PSServer.get() == null) {
            return 1;
        }
        CelestialBody planet = PSServer.get().getSolarSystem().getPlanet(body);
        if (planet == null) {
            return 1;
        }

        AbstractIrlSpacecraft irlSpacecraft = JPLHorizons.getSpacecraftData(spacecraftName, body, planet.getMass());

        if (irlSpacecraft != null) {
            PSServer.get().IrlSpacecraftJoinOrbital(irlSpacecraft, planet);
            PSServer.get().sendAllMessage("Added spacecraft " +  spacecraftName);

            return 0;
        }

        return 1;
    }
}
