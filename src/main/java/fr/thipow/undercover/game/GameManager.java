package fr.thipow.undercover.game;

import static fr.thipow.undercover.game.GameTask.getFirstPlayerToPlay;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.utils.ItemBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class GameManager {

    private        EStates             gameState   = EStates.WAITING;
    private static ArrayList<Player>   gamePlayers = new ArrayList<>();
    private        Map<Player, ERoles> playerRoles = new HashMap<>();
    private        Undercover          main;
    private String[] words = getWords();

    public GameManager(Undercover main) {
        this.main = main;
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

    public void startGame(){
        distributeRoles();
        setGameState(EStates.PLAYING);
        for(Player player : gamePlayers) {
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
            GameTask gameTask = new GameTask(main);
            gameTask.runTaskTimer(main, 0L, 20L);
        }, 20L);
    }

    public static void startRound(){
        Player firstPlayer = getFirstPlayerToPlay(gamePlayers);
        Bukkit.broadcastMessage("§eLe joueur §b" + firstPlayer.getName() + "§e est le premier à jouer !");
        giveSkipItem(firstPlayer);

    }

    public static void nextRound(Player player){

    }

    public static void giveSkipItem(Player player){
        player.getInventory().setItem(0, new ItemBuilder(Material.FEATHER).setName("§cTerminé").toItemStack());
    }

    public static String[] getWords(){
        EWords[] allPairs = EWords.values();
        EWords pair = allPairs[(int) (Math.random() * allPairs.length)];
        return new String[] {pair.getWord1(), pair.getWord2()};
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
