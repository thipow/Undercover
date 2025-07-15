package fr.thipow.undercover.game.managers;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.GameManager;
import fr.thipow.undercover.game.GamePlayer;
import fr.thipow.undercover.game.GameSettings;
import fr.thipow.undercover.utils.GameUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class TurnManager {

    private final List<GamePlayer> turnOrder             = new ArrayList<>();
    private final GameManager      gameManager;
    private       int              currentIndex          = -1;
    private       int              speakingBarTaskId     = -1;
    private       int              speakingTimeoutTaskId = -1;

    public TurnManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Initialise l’ordre des tours avec les joueurs en vie.
     */
    public void startTurns(List<GamePlayer> players) {
        turnOrder.clear();
        players.stream().filter(p -> !p.isEliminated()).forEach(turnOrder::add);
        currentIndex = 0;
    }

    public void updateCurrentIndex() {
        currentIndex += 1;
    }

    public int getSpeakingBarTaskId() {
        return speakingBarTaskId;
    }

    public int getSpeakingTimeoutTaskId() {
        return speakingTimeoutTaskId;
    }

    public void clearTaskIds() {
        speakingBarTaskId = -1;
        speakingTimeoutTaskId = -1;
    }

    public void cancelTurn(){
        if (speakingBarTaskId != -1) {
            Bukkit.getScheduler().cancelTask(speakingBarTaskId);
            speakingBarTaskId = -1;
        }

        if (speakingTimeoutTaskId != -1) {
            Bukkit.getScheduler().cancelTask(speakingTimeoutTaskId);
            speakingTimeoutTaskId = -1;
        }

        currentIndex = -1;
    }


    /**
     * Passe au joueur suivant encore en vie.
     */
    public void nextTurn() {
        gameManager.getVotingManager().updateVoteHolograms();
        FileConfiguration config = Undercover.getInstance().getConfig();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.stopAllSounds();
        }

        if (speakingBarTaskId != -1) {
            Bukkit.getScheduler().cancelTask(speakingBarTaskId);
            speakingBarTaskId = -1;
        }

        if (currentIndex < 0 || currentIndex >= turnOrder.size()) {
            currentIndex = 0;

            turnOrder.clear();
            gameManager.getPlayerManager().getAlivePlayers().stream().filter(p -> !p.isEliminated())
                .forEach(turnOrder::add);
            nextTurn();
            return;
        }

        GamePlayer current = turnOrder.get(currentIndex);

        if (current.isEliminated()) {
            updateCurrentIndex();
            if (currentIndex >= turnOrder.size()) {
                gameManager.getVotingManager().startVotingPhase();
            } else {
                nextTurn();
            }
            return;
        }

        Bukkit.broadcast(GameUtils.legacy(Objects.requireNonNull(config.getString("messages.turn-announce"))
            .replace("%player_name%", current.getPlayer().getName())));
        GameUtils.giveSkipItem(current.getPlayer());

        int totalTime = 20;

        speakingBarTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Undercover.getInstance(), new Runnable() {
            int timeLeft = totalTime;

            @Override
            public void run() {
                if (timeLeft <= 0 || getCurrentPlayer() != current) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.setLevel(0);
                        player.setExp(0f);
                    }
                    Bukkit.getScheduler().cancelTask(speakingBarTaskId);
                    speakingBarTaskId = -1;
                    speakingTimeoutTaskId = -1;
                    return;
                }

                float xpProgress = timeLeft / (float) totalTime;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.setLevel(timeLeft);
                    player.setExp(xpProgress);
                }

                timeLeft--;
            }
        }, 0L, 20L);

        speakingTimeoutTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(Undercover.getInstance(), () -> {
            if (getCurrentPlayer() == current) {
                Bukkit.broadcast(GameUtils.legacy(Objects.requireNonNull(config.getString("messages.speaking-time-exceeded"))
                    .replace("%player_name%", current.getPlayer().getName())));
                current.getPlayer().getInventory().clear();
                currentIndex++;
                if (currentIndex >= turnOrder.size()) {
                    gameManager.getVotingManager().startVotingPhase();
                } else {
                    nextTurn();
                }

            }
        }, totalTime * 20L);
    }


    /**
     * Récupère le joueur dont c’est le tour.
     */
    public GamePlayer getCurrentPlayer() {
        if (turnOrder.isEmpty() || currentIndex == -1) {
            return null;
        }
        return turnOrder.get(currentIndex);
    }

    /**
     * Réinitialise le gestionnaire de tours.
     */
    public void reset() {
        turnOrder.clear();
        currentIndex = -1;
    }

    /**
     * Renvoie l’ordre actuel des joueurs (lecture seule).
     */
    public List<GamePlayer> getTurnOrder() {
        return turnOrder;
    }

    /**
     * Vérifie s’il reste un joueur actif dans l’ordre des tours.
     */
    public boolean hasNext() {
        if (turnOrder.isEmpty()) {
            return false;
        }

        for (GamePlayer player : turnOrder) {
            if (!player.isEliminated()) {
                return true;
            }
        }
        return false;
    }

    public void teleportPlayers() {
        List<Location> spawns = GameSettings.getCurrentMap().getSpawns();

        int numberOfPlayers = gameManager.getPlayerManager().getAlivePlayers().size();
        int totalSpawns = spawns.size();

        if (numberOfPlayers > totalSpawns) {
            Bukkit.broadcast(GameUtils.legacy("§cErreur : Pas assez de positions disponibles pour tous les joueurs !"));
            Undercover.getInstance().getLogger().severe("Not enough spawn positions for all players!");
            return;
        }

        for (int i = 0; i < numberOfPlayers; i++) {
            int index = (i * totalSpawns) / numberOfPlayers;
            Player player = gameManager.getPlayerManager().getAlivePlayers().get(i).getPlayer();
            player.teleport(spawns.get(index));
        }
    }
}