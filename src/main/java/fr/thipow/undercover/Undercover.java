package fr.thipow.undercover;

import fr.thipow.undercover.commands.UndercoverCommand;
import fr.thipow.undercover.game.GameManager;
import fr.thipow.undercover.game.GameTask;
import fr.thipow.undercover.listeners.ListenersManager;
import fr.thipow.undercover.utils.inventory.FastInvManager;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Undercover extends JavaPlugin {

    private static Undercover  instance;
    private        GameManager      gameManager;

    public static Undercover getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getServer().getConsoleSender().sendMessage("Undercover plugin is loading...");
        this.gameManager = new GameManager(this);
        gameManager.initGame();

        ListenersManager listenersManager = new ListenersManager(this);
        listenersManager.initListeners();

        GameTask gameTask = new GameTask(this);
        gameTask.runTaskTimer(this, 0, 20);
        FastInvManager.register(this);


        Objects.requireNonNull(getCommand("undercover")).setExecutor(new UndercoverCommand());

    }

    public GameManager getGameManager() {
        return gameManager;
    }

    @Override
    public void onDisable() {
        Bukkit.getServer().getConsoleSender().sendMessage("Undercover plugin is shutting down...");
    }
}
