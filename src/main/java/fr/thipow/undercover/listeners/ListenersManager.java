package fr.thipow.undercover.listeners;

import fr.thipow.undercover.Undercover;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;

/**
 * Manages the registration of all event listeners used in the Undercover plugin.
 * @author Thipow
 */
public class ListenersManager {

    private final PluginManager pluginManager;
    private final Undercover main;

    /**
     * Constructs the listener manager with a reference to the main plugin instance.
     *
     * @param main the plugin instance
     */
    public ListenersManager(Undercover main) {
        this.main = Objects.requireNonNull(main, "Plugin instance cannot be null.");
        this.pluginManager = Bukkit.getPluginManager();
    }

    /**
     * Registers all plugin event listeners to the server's PluginManager.
     */
    public void initListeners() {
        pluginManager.registerEvents(new GameListeners(), main);
        pluginManager.registerEvents(new PlayerListeners(), main);
    }
}
