package fr.thipow.undercover.game.managers;

import fr.thipow.undercover.game.GamePlayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Manages the players in the Undercover game. Provides methods to add, remove, and retrieve players, as well as to
 * check their game status. Also provides methods to get lists of alive and eliminated players.
 *
 * @author Thipow
 */

public class PlayerManager {

    private final Map<UUID, GamePlayer> players = new HashMap<>();

    /**
     * Adds a player to the game. If the player already exists, it will not be added again.
     *
     * @param gamePlayer The GamePlayer object representing the player to be added.
     */
    public void addPlayer(GamePlayer gamePlayer) {
        players.putIfAbsent(gamePlayer.getPlayer().getUniqueId(), gamePlayer);
    }

    /**
     * Removes a player from the game.
     *
     * @param player The Player object representing the player to be removed.
     */
    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

    /**
     * Retrieves the GamePlayer object for a given Player.
     *
     * @param player The Player object for which to retrieve the GamePlayer.
     * @return The GamePlayer object associated with the player, or null if the player is not in the game.
     */
    public GamePlayer getGamePlayer(Player player) {
        return players.get(player.getUniqueId());
    }

    /**
     * Get all GamePlayer objects in the game.
     * @return A list of all GamePlayer objects currently in the game.
     */
    public List<GamePlayer> getAllGamePlayers() {
        return new ArrayList<>(players.values());
    }

    /**
     * Retrieves a list of all players currently alive in the game.
     *
     * @return A list of GamePlayer objects representing all alive players.
     */
    public List<GamePlayer> getAlivePlayers() {
        return players.values().stream()
            .filter(p -> !p.isEliminated())
            .toList();
    }

    /**
     * Retrieves a list of all players currently eliminated in the game.
     *
     * @return A list of GamePlayer objects representing all eliminated players.
     */
    public List<GamePlayer> getEliminatedPlayers() {
        return players.values().stream()
            .filter(GamePlayer::isEliminated)
            .toList();
    }

    /**
     * Checks if a player is currently in the game.
     *
     * @param player The Player object to check.
     * @return true if the player is in the game, false otherwise.
     */
    public boolean isInGame(Player player) {
        return players.containsKey(player.getUniqueId());
    }

    /**
     * Clears all players from the game. This method is typically used to reset the game state.
     */
    public void clear() {
        players.clear();
    }

    /**
     * Gets the number of players currently in the game.
     *
     * @return The number of players.
     */
    public int size() {
        return players.size();
    }

    /**
     * Resets all players in the game. This method is typically used to reset player states at the end of a game.
     */
    public void resetAll() {
        for (GamePlayer gamePlayer : players.values()) {
            gamePlayer.reset();
        }
    }
}