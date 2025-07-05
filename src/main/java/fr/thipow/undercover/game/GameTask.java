package fr.thipow.undercover.game;

import static fr.thipow.undercover.game.GameManager.giveSkipItem;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.managers.ScoreboardManager;
import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GameTask extends BukkitRunnable {

    private Undercover main;

    public GameTask(Undercover main) {
        this.main = main;
    }

    public static Player getFirstPlayerToPlay(ArrayList<Player> players) {
        if (players.isEmpty()) {
            return null;
        }
        return players.get(new Random().nextInt(players.size()));
    }

    @Override
    public void run(){
        for(Player player : Bukkit.getOnlinePlayers()){
            ScoreboardManager.updateScoreboard(player);
        }
    }

}
