package fr.thipow.undercover.game.maps;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Represents a game map in the Undercover plugin. Contains properties such as name, display name, item, spawn
 * locations, and center location. Provides methods to access and modify these properties.
 *
 * @author Thipow
 */

public class GameMap {

    private final String         name;
    private final List<Location> spawns;
    private       String         displayName;
    private       Material       item;
    private       Location       spawn;
    private       Location       center;

    /**
     * Constructs a new GameMap with the specified properties.
     *
     * @param name        The unique identifier for the map.
     * @param displayName The display name of the map.
     * @param item        The material representing the map in the inventory.
     * @param spawn       The main spawn location for players.
     * @param center      The center location of the map.
     * @param spawns      A list of additional spawn locations for players.
     */
    public GameMap(String name, String displayName, Material item, Location spawn, Location center,
                   List<Location> spawns) {
        this.name = name;
        this.displayName = displayName;
        this.item = item;
        this.spawn = spawn;
        this.center = center;
        this.spawns = spawns != null ? spawns : new ArrayList<>();
    }

    /**
     * Gets the unique identifier of the map.
     *
     * @return The name of the map.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the display name of the map, formatted for display in the game. The display name uses Minecraft's color
     * codes.
     *
     * @return The formatted display name.
     */
    public String getDisplayName() {
        return displayName.replaceAll("&", "§");
    }

    /**
     * Sets the display name of the map.
     *
     * @param displayName The new display name to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the material representing the map in the inventory.
     *
     * @return The Material of the map item.
     */
    public Material getItem() {
        return item;
    }

    /**
     * Sets the material representing the map in the inventory.
     *
     * @param item The Material to set as the map item.
     */
    public void setItem(Material item) {
        this.item = item;
    }

    /**
     * Gets the main spawn location for players on this map.
     *
     * @return The Location of the main spawn.
     */
    public Location getSpawn() {
        if (spawn != null && spawn.getWorld() != null) {
            return spawn;
        }

        org.bukkit.World defaultWorld = Bukkit.getWorld("world");
        if (defaultWorld == null && !Bukkit.getWorlds().isEmpty()) {
            defaultWorld = Bukkit.getWorlds().getFirst();
        }

        return new Location(defaultWorld, -16.5, 69, 0.5, -90, 0);
    }

    /**
     * Sets the main spawn location for players on this map.
     *
     * @param spawn The Location to set as the main spawn.
     */
    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    /**
     * Gets the center location of the map. If the center is not set or the world is null, it returns a default
     * location.
     *
     * @return The Location of the center of the map.
     */
    public Location getCenter() {
        if (center != null && center.getWorld() != null) {
            return center;
        }

        var defaultWorld = Bukkit.getWorld("world");
        if (defaultWorld == null) {
            defaultWorld = Bukkit.getWorlds().getFirst();
        }
        return new Location(defaultWorld, 0.5, 75, 0.5, -90, 0);
    }

    /**
     * Sets the center location of the map.
     *
     * @param center The Location to set as the center of the map.
     */
    public void setCenter(Location center) {
        this.center = center;
    }

    /**
     * Gets the list of spawn locations for players on this map.
     *
     * @return A List of Locations representing the spawn points.
     */
    public List<Location> getSpawns() {
        return spawns;
    }

    /**
     * Adds a new spawn location to the map.
     *
     * @param spawn The Location to add as a spawn point.
     */
    public void addSpawn(Location spawn) {
        this.spawns.add(spawn);
    }
}

