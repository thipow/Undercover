package fr.thipow.undercover.game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.thipow.undercover.Undercover;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/*
 * StatsManager is responsible for managing player statistics in the Undercover game.
 * It handles reading and writing player stats to a JSON file, allowing for tracking wins, losses, and roles.
 *
 * @author Thipow
 */

public class StatsManager {

    private final File                   statsFile;
    private final Gson                   gson;
    private       Map<UUID, PlayerStats> stats;

    /**
     * Constructs a new StatsManager instance. Initializes the Gson instance and the stats file.
     */
    public StatsManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.statsFile = new File(Undercover.getInstance().getDataFolder(), "stats.json");

        if (!statsFile.exists()) {
            try {
                boolean created = statsFile.createNewFile();
                if (created) {
                    stats = new HashMap<>();
                    save();
                } else {
                    Undercover.getInstance().getLogger()
                        .warning("Le fichier stats.json existe déjà ou n'a pas pu être créé.");
                    stats = new HashMap<>();
                }
            } catch (IOException e) {
                Undercover.getInstance().getLogger().log(Level.SEVERE, "Impossible de créer le fichier stats.json", e);
                stats = new HashMap<>();
            }
        } else {
            load();
        }
    }


    /**
     * Adds a win for a player with the specified UUID and role.
     *
     * @param uuid The UUID of the player.
     * @param role The role of the player (e.g., "civil", "undercover", "white").
     */
    public void addWin(UUID uuid, String role) {
        stats.computeIfAbsent(uuid, id -> new PlayerStats());
        PlayerStats playerStats = stats.get(uuid);

        switch (role.toLowerCase()) {
            case "civil" -> playerStats.civil++;
            case "undercover" -> playerStats.undercover++;
            case "white" -> playerStats.white++;
            default -> System.out.println("Unknown role: " + role);
        }

        save();
    }

    /**
     * Adds a loss for a player with the specified UUID.
     *
     * @param uuid The UUID of the player.
     */
    public void addLoss(UUID uuid) {
        stats.computeIfAbsent(uuid, id -> new PlayerStats());
        stats.get(uuid).losses++;
        save();
    }

    /**
     * Gets the number of losses for a player with the specified UUID.
     *
     * @param uuid The UUID of the player.
     * @return The number of losses, or 0 if the player has no recorded stats.
     */
    public int getLosses(UUID uuid) {
        PlayerStats ps = stats.get(uuid);
        return (ps != null) ? ps.losses : 0;
    }

    /**
     * Gets the number of wins for a player with the specified UUID and role.
     *
     * @param uuid The UUID of the player.
     * @param role The role of the player (e.g., "civil", "undercover", "white").
     * @return The number of wins for the specified role, or 0 if the player has no recorded stats for that role.
     */
    public int getWins(UUID uuid, String role) {
        PlayerStats ps = stats.get(uuid);
        if (ps == null) {
            return 0;
        }

        return switch (role.toLowerCase()) {
            case "civil" -> ps.civil;
            case "undercover" -> ps.undercover;
            case "white" -> ps.white;
            default -> 0;
        };
    }

    /**
     * Saves the current player statistics to the stats.json file. This method serializes the stats map to JSON format
     * and writes it to the file.
     */
    private void save() {
        try (Writer writer = new FileWriter(statsFile)) {
            gson.toJson(stats, writer);
        } catch (IOException e) {
            Undercover.getInstance().getLogger().log(Level.SEVERE, "Could not save stats.json", e);
        }
    }

    /**
     * Loads player statistics from the stats.json file. This method reads the JSON data from the file and deserializes
     * it into the stats map. If the file does not exist or is empty, it initializes an empty stats map.
     */
    private void load() {
        try (Reader reader = new FileReader(statsFile)) {
            Type type = new TypeToken<Map<UUID, PlayerStats>>() {
            }.getType();
            stats = gson.fromJson(reader, type);
            if (stats == null) {
                stats = new HashMap<>();
            }
        } catch (IOException e) {
            Undercover.getInstance().getLogger().log(Level.SEVERE, "Could not load stats.json", e);
            stats = new HashMap<>();
        }
    }

    public boolean getMusicStatus(UUID uuid) {
        PlayerStats ps = stats.get(uuid);
        if (ps == null) {
            ps = new PlayerStats();
            stats.put(uuid, ps);
        }
        return ps.music;
    }

    public void toogleMusic(UUID uuid) {
        PlayerStats ps = stats.get(uuid);
        if (ps == null) {
            ps = new PlayerStats();
            stats.put(uuid, ps);
        }
        ps.music = !ps.music;
        save();
    }

    /**
     * Reloads the player statistics by loading them from the stats.json file again. This method is useful for
     * refreshing the stats without restarting the server.
     */
    public void reload() {
        load();
    }

    /**
     * Represents the statistics of a player, including the number of wins and losses for each role.
     */
    private static class PlayerStats {

        int civil      = 0;
        int undercover = 0;
        int white      = 0;
        int losses     = 0;
        boolean music = true;
    }
}