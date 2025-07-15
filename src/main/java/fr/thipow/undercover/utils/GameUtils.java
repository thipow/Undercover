package fr.thipow.undercover.utils;

import fr.thipow.undercover.Undercover;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Utility class for game-related operations in the Undercover plugin.
 *
 * @author Thipow
 */

public class GameUtils {

    /**
     * Clears all armor stands from all worlds in the server.
     */
    public static void clearArmorStands() {
        Bukkit.getWorlds().forEach(world -> world.getEntitiesByClass(ArmorStand.class).forEach(Entity::remove));
    }

    public static void playWaitingSound(Player player) {
        List<Location> locations = new ArrayList<>();
        Undercover.getInstance().getMapsManager().getAllMaps().forEach(map -> {
            locations.add(map.getCenter());
        });
        for(Location location : locations) {
            player.playSound(location, Sound.MUSIC_DISC_OTHERSIDE, 0.15f, 1);
        }
    }

    public static void playGameSound(Player player) {
        List<Location> locations = new ArrayList<>();
        Undercover.getInstance().getMapsManager().getAllMaps().forEach(map -> {
            locations.add(map.getCenter());
        });
        for(Location location : locations) {
            player.playSound(location, Sound.MUSIC_DISC_CREATOR, 0.15f, 1);
        }
    }

    /**
     * Gives a skip item to the specified player.
     *
     * @param player the player to receive the skip item
     */
    public static void giveSkipItem(Player player) {
        player.getInventory().setItem(0, new ItemBuilder(Material.FEATHER).setName(
            Undercover.getInstance().getConfig().getString("messages.skip-item-name")).toItemStack());
    }

    /**
     * Converts a legacy text string into a Component using the legacy section serializer.
     *
     * @param legacyText the legacy text to convert
     * @return the converted Component
     */
    public static Component legacy(String legacyText) {
        return LegacyComponentSerializer.legacySection().deserialize(legacyText);
    }

    /**
     * Capitalizes the first letter of a word.
     *
     * @param word the word to capitalize
     * @return the capitalized word, or the original word if it is null or empty
     */
    public static String capitalize(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    /**
     * Smoothly transitions the time in the specified world to a target time over 20 ticks (1 second).
     *
     * @param world      the world to change the time in
     * @param targetTime the target time to set, in ticks (0-23999)
     */
    public static void smoothTimeTransition(World world, long targetTime) {
        long startTime = world.getTime();
        long delta = (targetTime - startTime + 24000) % 24000;

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;
                float progress = ticks / (float) 20;
                long newTime = (startTime + (long) (delta * progress)) % 24000;
                world.setTime(newTime);

                if (ticks >= 20) {
                    cancel();
                    world.setTime(targetTime);
                }
            }
        }.runTaskTimer(Undercover.getInstance(), 0L, 1L);
    }

}
