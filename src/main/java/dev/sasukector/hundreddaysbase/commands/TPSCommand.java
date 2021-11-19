package dev.sasukector.hundreddaysbase.commands;

import dev.sasukector.hundreddaysbase.helpers.ServerUtilities;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TPSCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            player.playSound(player.getLocation(), "minecraft:block.note_block.bell", 1, 1);
            ServerUtilities.sendServerMessage(player, "TPS: §6" + String.format("%.2f", Bukkit.getTPS()[0]));
        }
        return true;
    }

}
