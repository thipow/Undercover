package fr.thipow.undercover.gui;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.GameSettings;
import fr.thipow.undercover.game.maps.GameMap;
import fr.thipow.undercover.utils.GameUtils;
import fr.thipow.undercover.utils.ItemBuilder;
import fr.thipow.undercover.utils.inventory.FastInv;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A GUI for selecting maps in the Undercover plugin. It displays all available maps, allows the player to select one,
 * and teleports all players to the selected map's spawn.
 */
public class MapsGUI extends FastInv {

    // Slots used for displaying maps
    private static final int[] MAP_SLOTS = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    // Slot constants for navigation buttons
    private static final int BACK_SLOT       = 49;
    private static final int RANDOM_MAP_SLOT = 4;

    /**
     * Constructs and initializes the MapsGUI for a given player.
     *
     * @param player The player who is viewing the GUI.
     */
    public MapsGUI(final Player player) {
        super(6 * 9, Undercover.getInstance().getConfig().getString("messages.guis.config-gui.name"));

        // Set up decorative borders
        setBorders();

        // Load all available maps
        List<GameMap> maps = new ArrayList<>(Undercover.getInstance().getMapsManager().getAllMaps());

        // Display either available maps or a barrier item if none exist
        if (maps.isEmpty()) {
            setItem(22, new ItemBuilder(Material.BARRIER).setName("§cNo maps available").toItemStack());
        } else {
            populateMapSlots(player, maps);
        }

        // Back button to return to the configuration GUI
        setItem(BACK_SLOT, new ItemBuilder(Material.ARROW).setName("§cBack").toItemStack(),
            e -> new ConfigGUI(player).open(player));

        // Random map selection button
        setItem(RANDOM_MAP_SLOT, new ItemBuilder(Material.COMPASS).setName("§bRandom Map").toItemStack(), e -> {
            GameMap random = Undercover.getInstance().getMapsManager().getRandomMap();
            if (random == null) {
                player.sendMessage("§cNo maps are defined.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }
            GameSettings.setCurrentMap(random);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            player.sendMessage("§aRandom map selected!");
            teleportAllPlayersToMap(random);
            refresh(player);
        });
    }

    /**
     * Refreshes the GUI for the given player.
     *
     * @param player The player whose GUI should be refreshed.
     */
    private static void refresh(Player player) {
        new MapsGUI(player).open(player);
    }

    /**
     * Fills the GUI with decorative border items.
     */
    private void setBorders() {
        ItemStack blue = new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).toItemStack();
        ItemStack aqua = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE).toItemStack();

        // Define the border slots visually
        int[] blueSlots = {0, 8, 45, 53};
        int[] aquaSlots = {1, 7, 9, 17, 18, 26, 27, 35, 36, 44, 46, 52};

        for (int slot : blueSlots) {
            setItem(slot, blue);
        }
        for (int slot : aquaSlots) {
            setItem(slot, aqua);
        }
    }

    /**
     * Populates the GUI with available maps and their click actions.
     *
     * @param player The player who opened the GUI.
     * @param maps   The list of maps to display.
     */
    private void populateMapSlots(Player player, List<GameMap> maps) {
        int max = Math.min(maps.size(), MAP_SLOTS.length);
        for (int i = 0; i < max; i++) {
            GameMap map = maps.get(i);
            int slot = MAP_SLOTS[i];

            // Build the item representing the map
            ItemStack mapItem = new ItemBuilder(map.getItem()).setName(map.getDisplayName()).setLore(
                GameSettings.getCurrentMap() == map ? "§cDéjà sélectionnée !"
                    : "§aCliquez pour sélectionner cette carte !").toItemStack();

            // Set the item with click action
            setItem(slot, mapItem, e -> {
                if (Undercover.getInstance().getMapsManager().getFirstMap() == null) {
                    player.sendMessage("§cAucune carte n'est définie !");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }

                GameSettings.setCurrentMap(map);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                player.sendMessage(map.getDisplayName() + " §aest désormais la carte sélectionnée !");
                teleportAllPlayersToMap(map);
                refresh(player);
            });
        }
    }

    /**
     * Teleports all online players to the spawn of the given map and plays a sound for them.
     *
     * @param map The map to teleport players to.
     */
    private void teleportAllPlayersToMap(GameMap map) {
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.teleport(map.getSpawn());
        });
    }
}
