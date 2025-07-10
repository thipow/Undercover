package fr.thipow.undercover;

import fr.thipow.undercover.commands.UndercoverCommand;
import fr.thipow.undercover.game.GameManager;
import fr.thipow.undercover.game.GameTask;
import fr.thipow.undercover.listeners.ListenersManager;
import fr.thipow.undercover.utils.GameUtils;
import fr.thipow.undercover.utils.inventory.FastInvManager;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class of the Undercover plugin.
 * Handles the plugin lifecycle, including initialization and cleanup,
 * as well as global systems like game management, listener registration,
 * and player handling during shutdown.
 * @author Thipow
 */

public final class Undercover extends JavaPlugin {

    private static Undercover  instance;
    private        GameManager gameManager;

    /**
     * Returns the singleton instance of the plugin.
     * @return the plugin instance.
     */
    public static Undercover getInstance() {
        return instance;
    }

    /**
     * Called when the plugin is enabled.
     * Initializes configuration, managers, listeners, tasks,
     * and prepares the game environment.
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        logToConsole("Undercover plugin is loading...");

        this.gameManager = new GameManager(this);
        gameManager.initGame();

        Bukkit.getWorlds().forEach(world -> world.setClearWeatherDuration(99999));

        new ListenersManager(this).initListeners();
        new GameTask(this).runTaskTimer(this, 0, 20);
        FastInvManager.register(this);

        PluginCommand command = getCommand("undercover");
        if (command != null) {
            command.setExecutor(new UndercoverCommand());
        } else {
            logToConsole("§cErreur : commande /undercover introuvable dans plugin.yml !");
        }

        for (Entity entity : Objects.requireNonNull(Bukkit.getWorld("world")).getEntities()) {
            if (entity instanceof ArmorStand) {
                entity.remove();
            }
        }

    }

    /**
     * Logs a formatted message to the server console.
     * @param message the message to display.
     */
    private void logToConsole(String message) {
        Bukkit.getConsoleSender().sendMessage("[Undercover] " + message);
    }

    /**
     * Returns the instance of the GameManager.
     * @return the GameManager instance.
     */
    public GameManager getGameManager() {
        return gameManager;
    }


    /**
     * Called when the plugin is disabled.
     * Cleans up game entities and safely disconnects players.
     */
    @Override
    public void onDisable() {
        Bukkit.getServer().getConsoleSender().sendMessage("Undercover plugin is shutting down...");
        GameUtils.clearArmorStands("world");
        kickAllPlayers();

    }

    /**
     * Kicks all online players with a restart message.
     */
    private void kickAllPlayers() {
        Component message = Component.text("§cLe serveur est en cours de redémarrage, merci de réessayer plus tard.");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kick(message);
        }
    }

}
