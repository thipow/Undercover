package fr.thipow.undercover.commands;

import fr.thipow.undercover.Undercover;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class VoteCommand implements CommandExecutor {
    private final Undercover main = Undercover.getInstance();

    @Override
    public boolean onCommand(@NotNull final CommandSender commandSender, @NotNull final Command command,
                             @NotNull final String s,
                             final @NotNull String @NotNull [] strings) {


        return false;
    }
}
