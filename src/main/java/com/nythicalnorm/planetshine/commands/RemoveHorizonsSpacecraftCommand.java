package com.nythicalnorm.planetshine.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nythicalnorm.planetshine.PSServer;
import com.nythicalnorm.planetshine.spacecraft.irlship.AbstractIrlSpacecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class RemoveHorizonsSpacecraftCommand {
    public RemoveHorizonsSpacecraftCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("horizons-remove").requires((stack) -> {
                            return stack.hasPermission(0);
                        })
            .then(Commands.argument("spacecraft_name", StringArgumentType.word())
                .executes((stack) -> {
                    return addHorizonsSpacecraft(stack.getSource(), StringArgumentType.getString(stack, "spacecraft_name"));
                })
            )
        );
    }

    private int addHorizonsSpacecraft(CommandSourceStack pSource, String spacecraftName) {
        if (PSServer.get() == null) {
            return 1;
        }
        AbstractIrlSpacecraft irlSpacecraft = PSServer.get().getIRLSpacecraft(spacecraftName);

        if (irlSpacecraft != null) {
            PSServer.get().removeIRLSpacecraft(irlSpacecraft);
            PSServer.get().sendAllMessage("Remove spacecraft " +  spacecraftName);
            return 0;
        }

        return 1;
    }
}
