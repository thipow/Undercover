package fr.thipow.undercover.game;

import static fr.thipow.undercover.game.GameTask.getFirstPlayerToPlay;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.utils.ItemBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GameManager {

    private static ArrayList<Player>   gamePlayers        = new ArrayList<>();
    private static int                 currentPlayerIndex = 0;
    private static boolean             inVotingPhase      = false;
    private static Map<Player, Player> votes              = new HashMap<>();
    private static Map<Player, ERoles> playerRoles        = new HashMap<>();
    private        EStates             gameState          = EStates.WAITING;
    private        Undercover          main;
    private        String[]            words              = getWords();

    public GameManager(Undercover main) {
        this.main = main;
    }

    public static void startRound() {
        currentPlayerIndex = 0;
        inVotingPhase = false;
        nextTurn();
        Player firstPlayer = getFirstPlayerToPlay(gamePlayers);
        giveSkipItem(firstPlayer);

    }

    public static boolean isInVotingPhase() {
        return inVotingPhase;
    }

    public static void nextTurn() {
        if (currentPlayerIndex >= gamePlayers.size()) {
            startVotingPhase();
            return;
        }

        Player current = gamePlayers.get(currentPlayerIndex);
        Bukkit.broadcastMessage("§aC'est au tour de " + current.getName() + " de jouer !");
        giveSkipItem(current);

        Bukkit.getScheduler().runTaskLater(Undercover.getInstance(), () -> {
            if (getCurrentPlayer() == current) {
                Bukkit.broadcastMessage("§c" + current.getName() + " a dépassé son temps de parole !");
                currentPlayerIndex++;
                nextTurn();
            }
        }, 20 * 20L);

    }

    public static void startVotingPhase() {
        inVotingPhase = true;
        Bukkit.broadcastMessage("§cLa phase de vote commence !");
        votes.clear();
    }

    public static void vote(Player voter, Player target) {
        if (!inVotingPhase) {
            voter.sendMessage("§cLa phase de vote n'est pas active !");
            return;
        }

        if (votes.containsKey(voter)) {
            voter.sendMessage("§cVous avez déjà voté !");
            return;
        }

        votes.put(voter, target);
        voter.sendMessage("§aVous avez voté pour " + target.getName() + " !");

        if (votes.size() >= gamePlayers.size()) {
            endVotingPhase();
        }
    }

    public static void endVotingPhase() {
        Map<Player, Integer> voteCount = new HashMap<>();
        for (Player voted : votes.values()) {
            voteCount.put(voted, voteCount.getOrDefault(voted, 0) + 1);
        }

        Player eliminatedPlayer = null;
        int maxVotes = 0;
        for (Map.Entry<Player, Integer> entry : voteCount.entrySet()) {
            if (entry.getValue() > maxVotes) {
                eliminatedPlayer = entry.getKey();
                maxVotes = entry.getValue();
            }
        }
        if (eliminatedPlayer != null) {
            Bukkit.broadcastMessage("§c" + eliminatedPlayer.getName() + " a été éliminé avec " + maxVotes + " votes !");
            gamePlayers.remove(eliminatedPlayer);
            eliminatedPlayer.getInventory().clear();
            eliminatedPlayer.setGameMode(GameMode.SPECTATOR);
            eliminatedPlayer.teleport(new Location(Bukkit.getWorld("world"), 0, 80, 0, 90, 0));
        }

    }

    public static void checkVictoryConditions() {
        long undercoverLeft = gamePlayers.stream().filter(p -> playerRoles.get(p) == ERoles.UNDERCOVER).count();
        long civilLeft = gamePlayers.stream().filter(p -> playerRoles.get(p) == ERoles.CIVIL).count();
        long whiteLeft = gamePlayers.stream().filter(p -> playerRoles.get(p) == ERoles.WHITE).count();

        if (undercoverLeft == 0) {
            Bukkit.broadcastMessage("§cLes Civils ont gagné !");
        } else if (undercoverLeft >= civilLeft) {
            Bukkit.broadcastMessage("§aLes Undercover ont gagné !");
        } else {
            startRound();
        }
    }

    public static void giveSkipItem(Player player) {
        player.getInventory().setItem(0, new ItemBuilder(Material.FEATHER).setName("§cTerminé").toItemStack());
    }

    public static String[] getWords() {
        EWords[] allPairs = EWords.values();
        EWords pair = allPairs[(int) (Math.random() * allPairs.length)];
        return new String[]{pair.getWord1(), pair.getWord2()};
    }

    public static Player getCurrentPlayer() {
        if (gamePlayers.isEmpty() || currentPlayerIndex >= gamePlayers.size()) {
            return null;
        }
        return gamePlayers.get(currentPlayerIndex);
    }

    public void initGame() {
        gameState = EStates.WAITING;
        gamePlayers.clear();
        playerRoles.clear();
    }

    public void distributeRoles() {
        Collections.shuffle(gamePlayers);
        for (Player player : gamePlayers) {
            if (playerRoles.get(player) == null) {
                if (GameSettings.getUndercoverCount() > 0) {
                    playerRoles.put(player, ERoles.UNDERCOVER);
                    GameSettings.setUndercoverCount(GameSettings.getUndercoverCount() - 1);
                } else if (GameSettings.isWhiteEnabled() && GameSettings.getWhiteCount() > 0) {
                    playerRoles.put(player, ERoles.WHITE);
                    GameSettings.setWhiteCount(GameSettings.getWhiteCount() - 1);
                } else {
                    playerRoles.put(player, ERoles.CIVIL);
                }
            }
        }
    }

    public void startGame() {
        distributeRoles();
        setGameState(EStates.PLAYING);
        for (Player player : gamePlayers) {
            ERoles role = playerRoles.get(player);

            switch (role) {
                case UNDERCOVER:
                    player.playSound(player.getLocation(), Sound.ENTITY_WOLF_GROWL, 1, 1);
                    player.sendTitle(role.getColor() + role.getName(), words[0], 20, 100, 20);
                    break;
                case WHITE:
                    player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_3, 1, 1);
                    player.sendTitle(role.getColor() + role.getName(), "§fVous n'avez pas de mot !", 20, 100, 20);
                    break;
                case CIVIL:
                    player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
                    player.sendTitle(role.getColor() + role.getName(), words[1], 20, 100, 20);
                    break;
            }
        }

        Bukkit.getScheduler().runTaskLater(main, () -> {
            startRound();
        }, 20L);
    }

    public ArrayList<Player> getGamePlayers() {
        return gamePlayers;
    }

    public Map<Player, ERoles> getPlayerRoles() {
        return playerRoles;
    }

    public ERoles getPlayerRole(Player player) {
        return playerRoles.getOrDefault(player, ERoles.CIVIL);
    }

    public EStates getGameState() {
        return gameState;
    }

    public void setGameState(final EStates gameState) {
        this.gameState = gameState;
    }

    public boolean isGameState(final EStates state) {
        return gameState == state;
    }
}
