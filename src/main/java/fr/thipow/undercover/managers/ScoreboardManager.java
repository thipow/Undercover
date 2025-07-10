package fr.thipow.undercover.managers;

import fr.mrmicky.fastboard.FastBoard;
import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.EStates;
import fr.thipow.undercover.game.GameManager;
import fr.thipow.undercover.game.GameSettings;
import java.util.*;
import org.bukkit.entity.Player;

/**
 * Manages custom player scoreboards for the Undercover plugin.
 * Displays game-related information based on the current game state (WAITING, PLAYING, ENDING).
 * Utilizes FastBoard for efficient and dynamic scoreboard updates.
 *
 * @author Thipow
 */

public class ScoreboardManager {

    private static final Undercover             main        = Undercover.getInstance();
    private static final GameManager            gameManager = main.getGameManager();
    private static final Map<Player, FastBoard> boards      = new HashMap<>();
    private static final String                 BOARD_TITLE = "§3Undercover";


    /**
     * Creates and assigns a new scoreboard to the specified player.
     * @param player the player to whom the scoreboard will be assigned.
     */
    public static void createScoreboard(Player player) {
        FastBoard board = new FastBoard(player);
        board.updateTitle(BOARD_TITLE);
        boards.put(player, board);
    }

    /**
     * Removes the scoreboard from the specified player.
     * @param player the player whose scoreboard will be removed.
     */
    public static void deleteScoreboard(Player player) {
        boards.remove(player);
    }

    /**
     * Updates the scoreboard content for the given player based on the current game state.
     * @param player the player whose scoreboard will be updated.
     */
    public static void updateScoreboard(Player player) {
        FastBoard board = boards.get(player);
        if (board == null) {
            return;
        }

        EStates state = gameManager.getGameState();
        board.updateTitle(BOARD_TITLE);

        switch (state) {
            case WAITING -> board.updateLines(getWaitingLines());
            case PLAYING -> board.updateLines(getPlayingLines());
            case ENDING -> board.updateLines(getEndingLines());
        }
    }

    /**
     * Builds the scoreboard lines for the WAITING game state.
     * @return a list of strings representing the scoreboard lines.
     */
    private static List<String> getWaitingLines() {
        return List.of(
            "§r",
            "§8- §fStatut : §a" + EStates.WAITING.getDisplayName(),
            "§8- §fJoueurs : §e" + gameManager.getGamePlayers().size() + "§8/§7" + GameSettings.getMaxPlayers(),
            "§8- §fCarte : §eCamping",
            "",
            "§8- §fVictoires Undercover: §c§l0",
            "§8- §fVictoires Civil: §b§l0",
            "§8- §fVictoires MrWhite: §f§l0",
            "",
            "§bthipow.fr"
        );
    }

    /**
     * Builds the scoreboard lines for the PLAYING game state.
     * @return a list of strings representing the scoreboard lines.
     */
    private static List<String> getPlayingLines() {
        return List.of(
            "§r",
            "§8- §fStatut : §a" + EStates.PLAYING.getDisplayName(),
            "§8- §fJoueurs restant : §e" + gameManager.getGamePlayers().size() + "§8/§7" + GameSettings.getMaxPlayers(),
            "§8- §fCarte : §eCamping",
            "",
            "§bthipow.fr"
        );
    }

    /**
     * Builds the scoreboard lines for the ENDING game state.
     * @return a list of strings representing the scoreboard lines.
     */
    private static List<String> getEndingLines() {
        return List.of(
            "§r",
            "§8- §fStatut : §a" + EStates.ENDING.getDisplayName(),
            "§8- §fCarte : §eCamping",
            "",
            "Gagnant(s) : §a§l" + GameManager.winners,
            "",
            "§bthipow.fr"
        );
    }
}
