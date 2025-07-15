package fr.thipow.undercover.game;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.utils.GameUtils;
import java.time.Duration;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Task responsible for the countdown before the game starts.
 *
 * @author Thipow
 */
public class StartingTask extends BukkitRunnable {

    private static StartingTask instance;
    private static boolean      started   = false;
    private        int          countdown = 10;

    /**
     * Starts the countdown task if not already started.
     *
     * @param plugin the Undercover plugin instance.
     */
    public static void start(Undercover plugin) {
        if (started) {
            return;
        }
        instance = new StartingTask();
        instance.runTaskTimer(plugin, 0L, 20L);
        started = true;
        Bukkit.broadcast(
            GameUtils.legacy(Objects.requireNonNull(plugin.getConfig().getString("messages.start-message"))));
    }

    /**
     * Toggles the starting task on or off.
     *
     * @param plugin the Undercover plugin instance.
     */
    public static void toggle(Undercover plugin) {
        if (isStarted()) {
            cancelTask();
            Bukkit.broadcast(
                GameUtils.legacy(Objects.requireNonNull(plugin.getConfig().getString("messages.starting-stopped"))));
        } else {
            start(plugin);
        }
    }

    /**
     * Cancels the countdown task if running.
     */
    public static void cancelTask() {
        if (instance != null) {
            instance.cancel();
            instance = null;
        }
        started = false;
    }

    /**
     * Checks if the starting task is currently running.
     *
     * @return true if the task is started, false otherwise.
     */
    public static boolean isStarted() {
        return started;
    }

    @Override
    public void run() {
        if (countdown > 0) {
            String title = "§bDébut dans §f" + countdown + "§b secondes";
            broadcastTitleAndSound(title, "", Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f, 0, 10);
            countdown--;
        } else {
            broadcastTitleAndSound("§aC'est parti !", "", Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f, 0, 10);
            Undercover.getInstance().getGameManager().startGame();
            cancelTask();
        }
    }

    /**
     * Sends a title and a sound to all online players.
     *
     * @param title        The main title text displayed.
     * @param subtitle     The subtitle text displayed under the main title.
     * @param sound        The sound to play for each player.
     * @param volume       The volume of the sound (1.0F is default).
     * @param pitch        The pitch of the sound (1.0F is normal pitch).
     * @param fadeInTicks  How many ticks the title takes to fade in.
     * @param fadeOutTicks How many ticks the title takes to fade out.
     */
    private void broadcastTitleAndSound(String title, String subtitle, Sound sound, float volume, float pitch,
                                        int fadeInTicks, int fadeOutTicks) {
        Title.Times times = Title.Times.times(Duration.ofMillis(fadeInTicks * 50L), Duration.ofMillis(20 * 50L),
            Duration.ofMillis(fadeOutTicks * 50L));

        Component titleComponent = Component.text(title);
        Component subtitleComponent = Component.text(subtitle);

        Title titlePacket = Title.title(titleComponent, subtitleComponent, times);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(titlePacket);
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

}
