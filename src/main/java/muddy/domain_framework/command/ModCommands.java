package muddy.domain_framework.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ModCommands {
    private static int executeSubCommandOne(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("Called /command sub_command_one."), false);
        return 1;
    }
}
