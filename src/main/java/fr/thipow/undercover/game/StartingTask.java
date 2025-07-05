package fr.thipow.undercover.game;

import fr.thipow.undercover.Undercover;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class StartingTask extends BukkitRunnable {

    private static StartingTask instance;
    private static boolean started = false;
    private int countdown = 10;

    private StartingTask() {}

    @Override
    public void run() {
        if (countdown > 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendTitle("§bDébut dans §f" + countdown + " §bsecondes", "", 0, 20, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
            }
            countdown--;
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendTitle("§aC'est parti !", "", 0, 40, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
            }

            Undercover.getInstance().getGameManager().startGame();
            cancelTask();
        }
    }

    public static void toggle(Undercover plugin) {
        if (isStarted()) {
            cancelTask();
            Bukkit.broadcastMessage("§cLancement annulé !");
        } else {
            start(plugin);
        }
    }

    public static void start(Undercover plugin) {
        if (started) return;

        instance = new StartingTask();
        instance.runTaskTimer(plugin, 0L, 20L); // toutes les secondes
        started = true;

        Bukkit.broadcastMessage("§aLancement de la partie dans 10 secondes !");
    }

    public static void cancelTask() {
        if (instance != null) {
            instance.cancel();
            instance = null;
        }
        started = false;
    }

    public static boolean isStarted() {
        return started;
    }
}
