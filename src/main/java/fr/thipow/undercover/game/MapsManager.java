package fr.thipow.undercover.game;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.maps.GameMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Manages the game maps for the Undercover plugin. Handles loading, saving, creating, deleting, and retrieving maps.
 *
 * @author Thipow
 */

public class MapsManager {

    private final Map<String, GameMap> maps = new HashMap<>();
    private final File                 file;
    private final FileConfiguration    config;

    /**
     * Constructs the MapsManager, initializes the maps file and loads existing maps.
     */
    public MapsManager() {
        file = new File(Undercover.getInstance().getDataFolder(), "maps.yml");
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    Undercover.getInstance().getLogger().warning("maps.yml already exists or could not be created.");
                }
            } catch (IOException e) {
                Undercover.getInstance().getLogger().severe("maps.yml could not be created.");
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadMaps();
    }


    /**
     * Loads all maps from the configuration file into the maps collection. Each map is identified by its name and
     * contains properties like display name, item, spawn, center, and additional spawns.
     */
    private void loadMaps() {
        maps.clear();
        for (String key : config.getKeys(false)) {
            String displayName = config.getString(key + ".displayName", key);
            Material item = Material.getMaterial(config.getString(key + ".item", "PAPER"));
            Location spawn = getLocation(config, key + ".spawn");
            Location center = getLocation(config, key + ".center");

            List<Location> spawns = new ArrayList<>();
            List<?> rawSpawns = config.getList(key + ".spawns");
            if (rawSpawns != null) {
                for (Object obj : rawSpawns) {
                    if (obj instanceof String locStr) {
                        spawns.add(parseLocation(locStr));
                    }
                }
            }

            maps.put(key.toLowerCase(), new GameMap(key, displayName, item, spawn, center, spawns));
        }
    }

    /**
     * Returns a list of all map names currently loaded.
     *
     * @return A list of map names.
     */
    public List<String> getMapNames() {
        return new ArrayList<>(maps.keySet());
    }

    /**
     * Saves all maps to the configuration file. Each map's properties are saved under its name as a key.
     */
    public void saveMaps() {
        for (String key : maps.keySet()) {
            GameMap map = maps.get(key);
            config.set(key + ".displayName", map.getDisplayName());
            config.set(key + ".item", map.getItem().name());
            config.set(key + ".spawn", serializeLocation(map.getSpawn()));
            config.set(key + ".center", serializeLocation(map.getCenter()));

            List<String> spawns = new ArrayList<>();
            for (Location loc : map.getSpawns()) {
                spawns.add(serializeLocation(loc));
            }
            config.set(key + ".spawns", spawns);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            Undercover.getInstance().getLogger().severe("Could not save maps.yml");
        }
    }

    /**
     * Creates a new map with the specified name if it does not already exist.
     *
     * @param name The name of the map to create.
     */
    public void createMap(String name) {
        if (maps.containsKey(name.toLowerCase())) {
            return;
        }
        GameMap map = new GameMap(name, name, Material.PAPER, null, null, new ArrayList<>());
        maps.put(name.toLowerCase(), map);
        saveMaps();
    }

    /**
     * Resets the spawn locations for a specific map.
     *
     * @param mapName The name of the map to reset spawns for.
     */
    public void resetSpawns(String mapName) {
        GameMap map = getMap(mapName);
        if (map != null) {
            map.getSpawns().clear();
            config.set(mapName + ".spawns", null);
            saveMaps();
        }
    }

    /**
     * Deletes a map by its name.
     *
     * @param name The name of the map to delete.
     * @return true if the map was deleted, false if it did not exist.
     */
    public boolean deleteMap(String name) {
        if (!maps.containsKey(name.toLowerCase())) {
            return false;
        }
        maps.remove(name.toLowerCase());
        config.set(name.toLowerCase(), null);
        saveMaps();
        return true;
    }

    /**
     * Retrieves a map by its name.
     *
     * @param name The name of the map.
     * @return The GameMap object if found, or null if not found.
     */
    public GameMap getMap(String name) {
        return maps.get(name.toLowerCase()) == null ? null : maps.get(name.toLowerCase());
    }

    /**
     * Returns a collection of all available maps.
     *
     * @return A collection of GameMap objects.
     */
    public Collection<GameMap> getAllMaps() {
        return maps.values();
    }

    /**
     * Returns the first map in the collection, or null if no maps are available.
     *
     * @return The first GameMap, or null if no maps exist.
     */
    public GameMap getFirstMap() {
        if (maps.isEmpty()) {
            return null;
        }
        return maps.values().iterator().next();
    }

    /**
     * Returns a random map from the available maps.
     *
     * @return A random GameMap, or null if no maps are available.
     */
    public GameMap getRandomMap() {
        if (maps.isEmpty()) {
            return null;
        }
        List<GameMap> mapList = new ArrayList<>(maps.values());
        return mapList.get(new Random().nextInt(mapList.size()));
    }

    /**
     * Sets the spawn location for the specified map.
     *
     * @param name The name of the map.
     * @param loc  The Location to set as the spawn.
     */
    public void setSpawn(String name, Location loc) {
        GameMap map = getMap(name);
        if (map != null) {
            map.setSpawn(loc);
            saveMaps();
        }
    }

    /**
     * Sets the center location for the specified map.
     *
     * @param name The name of the map.
     * @param loc  The Location to set as the center.
     */
    public void setCenter(String name, Location loc) {
        GameMap map = getMap(name);
        if (map != null) {
            map.setCenter(loc);
            saveMaps();
        }
    }

    /**
     * Adds a spawn location to the specified map.
     *
     * @param name The name of the map.
     * @param loc  The Location to add as a spawn.
     */
    public void addSpawn(String name, Location loc) {
        GameMap map = getMap(name);
        if (map != null) {
            map.addSpawn(loc);
            saveMaps();
        }
    }

    /**
     * Sets the display name for the map.
     *
     * @param name        The name of the map.
     * @param displayName The display name to set.
     */
    public void setDisplayName(String name, String displayName) {
        GameMap map = getMap(name);
        if (map != null) {
            map.setDisplayName(displayName);
            saveMaps();
        }
    }

    /**
     * Sets the item for the map.
     *
     * @param name     The name of the map.
     * @param material The Material to set as the item.
     */
    public void setItem(String name, Material material) {
        GameMap map = getMap(name);
        if (map != null) {
            map.setItem(material);
            saveMaps();
        }
    }

    /**
     * Serializes a Location to a string representation.
     *
     * @param loc The Location to serialize.
     * @return A string formatted as "world,x,y,z,yaw,pitch", or null if the location is null.
     */
    private String serializeLocation(Location loc) {
        if (loc == null) {
            return null;
        }
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw()
            + "," + loc.getPitch();
    }

    /**
     * Parses a string representation of a Location.
     *
     * @param str The string to parse, formatted as "world,x,y,z,yaw,pitch".
     * @return The parsed Location, or null if the string is invalid.
     */
    private Location parseLocation(String str) {
        if (str == null) {
            return null;
        }
        String[] parts = str.split(",");
        if (parts.length < 6) {
            return null;
        }
        return new Location(Bukkit.getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]),
            Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));
    }

    /**
     * Retrieves a Location from the configuration file.
     *
     * @param config The configuration to read from.
     * @param path   The path in the configuration where the location is stored.
     * @return The parsed Location, or null if not found.
     */
    private Location getLocation(FileConfiguration config, String path) {
        String locStr = config.getString(path);
        return locStr != null ? parseLocation(locStr) : null;
    }
}
