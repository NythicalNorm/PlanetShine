package com.nythicalnorm.planetshine.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.horizons.JPLHorizons;
import com.nythicalnorm.planetshine.network.PacketHandler;
import com.nythicalnorm.planetshine.network.orbitaldata.ClientboundOrbitChange;
import com.nythicalnorm.planetshine.solarsystem.orbits.OrbitalElements;
import com.nythicalnorm.planetshine.spacecraft.irlship.AbstractIrlSpacecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class UpdateHorizonsSpacecraftCommand {
    public UpdateHorizonsSpacecraftCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("horizons-update").requires((stack) -> {
                return stack.hasPermission(0);
            })
            .then(Commands.argument("spacecraft_name", StringArgumentType.word())
                    .executes((stack) -> {
                        return updateHorizonsSpacecraft(stack.getSource(), StringArgumentType.getString(stack, "spacecraft_name"));
                    })
            )
        );
    }

    private int updateHorizonsSpacecraft(CommandSourceStack pSource, String spacecraftName) {
        if (PSServer.get() == null) {
            return 1;
        }
        AbstractIrlSpacecraft irlSpacecraft = PSServer.get().getIRLSpacecraft(spacecraftName);

        if (irlSpacecraft != null) {
            OrbitalElements orbitalElements = JPLHorizons.getOrbitalElementData(spacecraftName, irlSpacecraft.getParent().getName(), irlSpacecraft.getParent().getMass());

            if (orbitalElements != null) {
                irlSpacecraft.setOrbitalElements(orbitalElements);
                PacketHandler.sendToAllClients(new ClientboundOrbitChange(irlSpacecraft.getOrbitId(), orbitalElements));
                PSServer.get().sendAllMessage("Updated spacecraft " +  spacecraftName);
            }
            return 0;
        }
        return 1;
    }
}
