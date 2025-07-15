package fr.thipow.undercover;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fr.thipow.undercover.commands.UndercoverCommand;
import fr.thipow.undercover.game.GameManager;
import fr.thipow.undercover.game.GameTask;
import fr.thipow.undercover.game.MapsManager;
import fr.thipow.undercover.game.StatsManager;
import fr.thipow.undercover.listeners.ListenersManager;
import fr.thipow.undercover.utils.GameUtils;
import fr.thipow.undercover.utils.Metrics;
import fr.thipow.undercover.utils.inventory.FastInvManager;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Main class of the Undercover plugin. Handles the plugin lifecycle, including initialization and cleanup, as well as
 * global systems like game management, listener registration, and player handling during shutdown.
 *
 * @author Thipow
 */

public final class Undercover extends JavaPlugin {

    private static Undercover instance;

    private GameManager     gameManager;
    private ProtocolManager protocolManager;
    private StatsManager    statsManager;
    private MapsManager     mapsManager;

    /**
     * Returns the singleton instance of the plugin.
     *
     * @return the plugin instance.
     */
    public static Undercover getInstance() {
        return instance;
    }

    /**
     * Called when the plugin is enabled. Initializes configuration, managers, listeners, tasks, and prepares the game
     * environment.
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;

        getLogger().info("Undercover plugin is loading...");
        getLogger().info("Trying to hook into ProtocolLib...");
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            protocolManager = ProtocolLibrary.getProtocolManager();
            getLogger().info("ProtocolLib detected, glow system enabled.");
        } else {
            getLogger().warning("ProtocolLib not detected, some features will not be available.");
        }

        statsManager = new StatsManager();
        mapsManager = new MapsManager();
        gameManager = new GameManager(this);
        gameManager.initGame();

        Bukkit.getWorlds().forEach(world -> world.setClearWeatherDuration(99999));

        new ListenersManager(this).initListeners();
        new GameTask(this).runTaskTimer(this, 0, 20);
        FastInvManager.register(this);

        new Metrics(this, 26507);

        PluginCommand command = getCommand("undercover");
        if (command != null) {
            command.setExecutor(new UndercoverCommand());
        } else {
            getLogger().severe("Commande /undercover introuvable dans plugin.yml !");
        }

        GameUtils.clearArmorStands();

    }

    /**
     * Returns the logger for the plugin.
     *
     * @return the plugin logger.
     */
    @Override
    public @NotNull Logger getLogger() {
        return super.getLogger();
    }

    /**
     * Returns the instance of the GameManager.
     *
     * @return the GameManager instance.
     */
    public GameManager getGameManager() {
        return gameManager;
    }

    /**
     * Returns the instance of the MapsManager.
     *
     * @return the MapsManager instance.
     */
    public MapsManager getMapsManager() {
        return mapsManager;
    }

    /**
     * Returns the instance of the StatsManager.
     *
     * @return the StatsManager instance.
     */
    public StatsManager getStatsManager() {
        return statsManager;
    }

    /**
     * Returns the ProtocolManager instance used for ProtocolLib features.
     *
     * @return the ProtocolManager instance.
     */
    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }


    /**
     * Called when the plugin is disabled. Cleans up game entities and safely disconnects players.
     */
    @Override
    public void onDisable() {
        getLogger().info("Undercover plugin is shutting down...");
        GameUtils.clearArmorStands();
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
