package dev.sasukector.hundreddaysbase.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.sasukector.hundreddaysbase.HundredDaysBedrockWalk;
import dev.sasukector.hundreddaysbase.helpers.ServerUtilities;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class PointsController {

    private static PointsController instance = null;
    private final @Getter Map<UUID, Integer> playerPoints;

    public static PointsController getInstance() {
        if (instance == null) {
            instance = new PointsController();
        }
        return instance;
    }

    public PointsController() {
        this.playerPoints = new HashMap<>();
        this.createSaveTask();
    }

    public int getTotalPoints() {
        return this.playerPoints.values().stream().mapToInt(Integer::valueOf).sum();
    }

    private LinkedHashMap<UUID, Integer> getSortedPlayerPoints() {
        return this.playerPoints.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new
            ));
    }

    public int getPlayerPoint(Player player) {
        if (!this.playerPoints.containsKey(player.getUniqueId())) {
            this.playerPoints.put(player.getUniqueId(), 0);
        }
        return this.playerPoints.get(player.getUniqueId());
    }

    public int getPlayerPosition(Player player) {
        int position = 1;
        LinkedHashMap<UUID, Integer> sortedPoints = getSortedPlayerPoints();
        for (Map.Entry<UUID, Integer> entry : sortedPoints.entrySet()) {
            if (entry.getKey().equals(player.getUniqueId())) {
                break;
            }
            position++;
        }
        return position;
    }

    public void addPointsToPlayer(Player player, int points) {
        int playerPoints = this.getPlayerPoint(player);
        int newPoints = playerPoints + points;
        this.playerPoints.put(player.getUniqueId(), newPoints);
        player.sendActionBar(Component.text("+1", TextColor.color(0x0091AD)));

        if (!TeamsController.getInstance().isMaster(player)) {
            String rankUp = "";
            if (newPoints >= 1000 && newPoints < 5000) {
                TeamsController.getInstance().getWalkerTeam().addEntry(player.getName());
                if (newPoints == 1000) {
                    rankUp = "<color:#AA0000>WALKER</color>";
                }
            } else if (newPoints >= 5000 && newPoints < 10000) {
                TeamsController.getInstance().getRunnerTeam().addEntry(player.getName());
                if (newPoints == 5000) {
                    rankUp = "<color:#00AA00>RUNNER</color>";
                }
            } else if (newPoints >= 10000 && newPoints < 100000) {
                TeamsController.getInstance().getAddictTeam().addEntry(player.getName());
                if (newPoints == 10000) {
                    rankUp = "<color:#00AAAA>ADDICT</color>";
                }
            } else if (newPoints >= 100000) {
                TeamsController.getInstance().getGodlikeTeam().addEntry(player.getName());
                if (newPoints == 100000) {
                    rankUp = "<color:#AA00AA>GODLIKE</color>";
                }
            }
            if (!rankUp.equals("")) {
                ServerUtilities.playBroadcastSound("minecraft:entity.wither.death",  1f, 1.4f);
                ServerUtilities.sendBroadcastMessage(ServerUtilities.getMiniMessage().parse(
                        "<bold><color:#0091AD>" + player.getName() +
                                "</color></bold> pasó a rango " + rankUp
                ));
            }
        }
    }

    public LinkedHashMap<UUID, Integer> getTop5() {
        return this.playerPoints.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(5)
            .collect(Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new
            ));
    }

    public LinkedHashMap<UUID, Integer> getLess5() {
        return this.playerPoints.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new
                ));
    }

    private Map<UUID, Integer> convertJSONArrayToMap(JsonArray pointsArray) {
        Map<UUID, Integer> pointsMap = new HashMap<>();
        pointsArray.forEach(jsonElement -> {
            JsonObject playerPoints = jsonElement.getAsJsonObject();
            pointsMap.put(UUID.fromString(playerPoints.get("uuid").getAsString()), playerPoints.get("points").getAsInt());
        });
        return pointsMap;
    }

    private JsonArray convertMapToJsonArray(Map<UUID, Integer> pointsMap) {
        JsonArray jsonArray = new JsonArray();
        for (Map.Entry<UUID, Integer> entry : pointsMap.entrySet()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("uuid", entry.getKey().toString());
            jsonObject.addProperty("points", entry.getValue());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    private JsonArray getPointsJSONArray() {
        JsonArray pointsArray = null;
        File configFile = new File(HundredDaysBedrockWalk.getInstance().getDataFolder(), "points.json");
        if (!configFile.exists()) {
            HundredDaysBedrockWalk.getInstance().saveResource(configFile.getName(), false);
        }
        try {
            String baseJson = Files.readString(configFile.toPath());
            if (baseJson != null && !baseJson.isEmpty()) {
                pointsArray = new Gson().fromJson(baseJson, JsonArray.class);
            }
        } catch (Exception e) {
            Bukkit.getLogger().info(ChatColor.RED + "Error while getting JSON file for points: " + e);
            e.printStackTrace();
        }
        return pointsArray;
    }

    public void loadPointsFromFile() {
        Map<UUID, Integer> loadedPlayerPoints = this.convertJSONArrayToMap(this.getPointsJSONArray());
        this.playerPoints.clear();
        playerPoints.putAll(loadedPlayerPoints);
    }

    public void savePointsToFile() {
        File configFile = new File(HundredDaysBedrockWalk.getInstance().getDataFolder(), "points.json");
        if (!configFile.exists()) {
            HundredDaysBedrockWalk.getInstance().saveResource(configFile.getName(), false);
        }
        try {
            FileWriter fileWriter = new FileWriter(configFile, false);
            fileWriter.write(convertMapToJsonArray(this.playerPoints).toString());
            fileWriter.close();
        } catch (Exception e) {
            Bukkit.getLogger().info(ChatColor.RED + "Error while writing JSON file for points: " + e);
            e.printStackTrace();
        }
    }

    public void createSaveTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                savePointsToFile();
            }
        }.runTaskTimer(HundredDaysBedrockWalk.getInstance(), 0L, 20L * 15);
    }

}
