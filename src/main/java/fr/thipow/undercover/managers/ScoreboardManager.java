package fr.thipow.undercover.managers;

import fr.mrmicky.fastboard.FastBoard;
import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.EStates;
import fr.thipow.undercover.game.GameManager;
import fr.thipow.undercover.game.GameSettings;
import java.util.HashMap;
import org.bukkit.entity.Player;

public class ScoreboardManager {

    private static final Undercover                 main        = Undercover.getInstance();
    private static final GameManager                gameManager = main.getGameManager();
    private static final HashMap<Player, FastBoard> boards      = new HashMap<>();

    public static void createScoreboard(Player player) {
        boards.put(player, new FastBoard(player));
        boards.get(player).updateTitle("§3Undercover");
    }

    public static void deleteScoreboard(Player player) {
        boards.remove(player);
    }

    public static void updateScoreboard(Player player) {
        if (boards.containsKey(player)) {
            EStates gameState = gameManager.getGameState();
            if (gameState.equals(EStates.WAITING)) {
                boards.get(player).updateTitle("§3Undercover");
                boards.get(player).updateLines("§r", "§8- §fStatut : §a" + gameState.getStateName(),
                    "§8- §fJoueurs : §e" + gameManager.getGamePlayers().size() + "§8/§7" + GameSettings.getMaxPlayer(),
                    "", "§bthipow.fr");
            }
            if (gameState.equals(EStates.PLAYING)) {
                boards.get(player).updateTitle("§3Undercover");
                boards.get(player).updateLines("§r", "§8- §fStatut : §a" + gameState.getStateName(),
                    "§8- §fJoueurs : §e" + gameManager.getGamePlayers().size() + "§8/§7" + +GameSettings.getMaxPlayer(),
                    "", "§bthipow.fr");
            }
        }
    }

    public static void clearScoreboard(Player player) {
        boards.get(player).updateTitle("");
        boards.get(player).updateLines("");
    }

    public static FastBoard getScoreboard(Player player) {
        return boards.get(player);
    }
}





