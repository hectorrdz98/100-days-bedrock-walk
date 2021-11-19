package dev.sasukector.hundreddaysbase.events;

import dev.sasukector.hundreddaysbase.controllers.BoardController;
import dev.sasukector.hundreddaysbase.controllers.PointsController;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.List;

public class SpawnEvents implements Listener {

    private static final List<Material> blackListMaterials;
    static {
        blackListMaterials = new ArrayList<>();
        blackListMaterials.add(Material.BEDROCK);
        blackListMaterials.add(Material.WATER);
        blackListMaterials.add(Material.LAVA);
        blackListMaterials.add(Material.AIR);
        blackListMaterials.add(Material.VOID_AIR);
        blackListMaterials.add(Material.CAVE_AIR);
        blackListMaterials.add(Material.BARRIER);
        blackListMaterials.add(Material.VOID_AIR);
        blackListMaterials.add(Material.NETHER_PORTAL);
        blackListMaterials.add(Material.END_PORTAL);
        blackListMaterials.add(Material.END_PORTAL_FRAME);
        blackListMaterials.add(Material.OBSIDIAN);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(
                Component.text("+ ", TextColor.color(0x84E3A4))
                        .append(Component.text(player.getName(), TextColor.color(0x84E3A4)))
        );
        BoardController.getInstance().newPlayerBoard(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BoardController.getInstance().removePlayerBoard(player);
        event.quitMessage(
                Component.text("- ", TextColor.color(0xE38486))
                        .append(Component.text(player.getName(), TextColor.color(0xE38486)))
        );
    }

    @EventHandler
    public void onPlayerWalk(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() != to.getX() || from.getZ() != to.getZ() || from.getY() != to.getY()) {
            Block block = player.getLocation().add(0, -1, 0).getBlock();
            Material material = block.getType();
            if (!blackListMaterials.contains(material)) {
                block.setType(Material.BEDROCK);
                PointsController.getInstance().addPointsToPlayer(player, 1);
            }
        }
    }

}
