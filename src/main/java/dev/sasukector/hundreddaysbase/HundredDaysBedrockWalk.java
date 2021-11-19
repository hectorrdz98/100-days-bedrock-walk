package dev.sasukector.hundreddaysbase;

import dev.sasukector.hundreddaysbase.commands.PointsCommand;
import dev.sasukector.hundreddaysbase.commands.ToggleDaysCommand;
import dev.sasukector.hundreddaysbase.commands.PlayedCommand;
import dev.sasukector.hundreddaysbase.controllers.BoardController;
import dev.sasukector.hundreddaysbase.controllers.PointsController;
import dev.sasukector.hundreddaysbase.events.SpawnEvents;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class HundredDaysBedrockWalk extends JavaPlugin {

    private static @Getter HundredDaysBedrockWalk instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info(ChatColor.DARK_PURPLE + "HundredDaysBedrockWalk startup!");
        instance = this;

        // Register events
        this.getServer().getPluginManager().registerEvents(new SpawnEvents(), this);
        Bukkit.getOnlinePlayers().forEach(player -> BoardController.getInstance().newPlayerBoard(player));

        // Register commands
        Objects.requireNonNull(HundredDaysBedrockWalk.getInstance().getCommand("points")).setExecutor(new PointsCommand());
        Objects.requireNonNull(HundredDaysBedrockWalk.getInstance().getCommand("played")).setExecutor(new PlayedCommand());
        Objects.requireNonNull(HundredDaysBedrockWalk.getInstance().getCommand("toggleDays")).setExecutor(new ToggleDaysCommand());

        // Load points
        PointsController.getInstance().loadPointsFromFile();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info(ChatColor.DARK_PURPLE + "HundredDaysBedrockWalk shutdown!");
        PointsController.getInstance().savePointsToFile();
    }
}
