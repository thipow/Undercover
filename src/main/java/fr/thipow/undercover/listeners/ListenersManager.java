package fr.thipow.undercover.listeners;

import fr.thipow.undercover.Undercover;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class ListenersManager {

    public PluginManager pluginManager;
    public Undercover    main = Undercover.getInstance();

    public ListenersManager(Undercover main) {
        this.main = main;
        pluginManager = Bukkit.getPluginManager();
    }

    public void initListeners(){
        pluginManager.registerEvents(new GameListeners(), main);
        pluginManager.registerEvents(new PlayerListeners(), main);
    }

}
