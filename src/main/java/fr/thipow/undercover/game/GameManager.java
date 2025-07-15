package fr.thipow.undercover.game;


import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.managers.PlayerManager;
import fr.thipow.undercover.game.managers.RoleManager;
import fr.thipow.undercover.game.managers.TurnManager;
import fr.thipow.undercover.game.managers.VotingManager;
import fr.thipow.undercover.game.managers.WordManager;
import fr.thipow.undercover.utils.GameUtils;
import fr.thipow.undercover.utils.GlowUtils;
import fr.thipow.undercover.utils.ItemBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the full lifecycle of an Undercover game: setup, start, finish, and reset operations.
 *
 * @author Thipow
 */

public class GameManager {

    private static final @NotNull FileConfiguration config = Undercover.getInstance().getConfig();
    private static final          Logger            logger = Undercover.getInstance().getLogger();

    private final @NotNull Undercover main;

    private final @NotNull PlayerManager playerManager;
    private final @NotNull RoleManager   roleManager;
    private final @NotNull VotingManager votingManager;
    private final @NotNull TurnManager   turnManager;
    private final @NotNull WordManager   wordManager;
    private                boolean       gameFinished = false;

    private @NotNull EStates gameState = EStates.WAITING;
    private @NotNull ERoles  winners   = ERoles.CIVIL;

    /**
     * Constructs a GameManager with all required managers.
     *
     * @param main Plugin main instance, must not be null.
     */
    public GameManager(@NotNull Undercover main) {
        this.main = main;
        this.playerManager = new PlayerManager();
        this.roleManager = new RoleManager();
        this.votingManager = new VotingManager(this);
        this.turnManager = new TurnManager(this);
        this.wordManager = new WordManager();
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public @NotNull PlayerManager getPlayerManager() {
        return playerManager;
    }

    public @NotNull RoleManager getRoleManager() {
        return roleManager;
    }

    public @NotNull VotingManager getVotingManager() {
        return votingManager;
    }

    public @NotNull TurnManager getTurnManager() {
        return turnManager;
    }

    public @NotNull WordManager getWordManager() {
        return wordManager;
    }

    public @NotNull EStates getGameState() {
        return gameState;
    }

    /**
     * Updates the game state.
     *
     * @param newState New state, must not be null.
     */
    public void setGameState(@NotNull EStates newState) {
        this.gameState = newState;
        logger.info("Game state changed to: " + newState);
    }

    public @NotNull ERoles getWinners() {
        return winners;
    }

    /**
     * Sets the winning role/team for this round.
     *
     * @param winners Winning role, must not be null.
     */
    public void setWinners(@NotNull ERoles winners) {
        this.winners = winners;
        logger.info("Winning team set to: " + winners.getName());
    }

    /**
     * Initialize a new game: clears state and resets world time.
     */
    public void initGame() {
        setGameState(EStates.WAITING);
        GameUtils.smoothTimeTransition(Objects.requireNonNull(Bukkit.getWorld("world")), 4000);
        playerManager.clear();
        logger.info("Game initialized.");
    }

    /**
     * Start the game: assign roles, teleport players, give them words.
     */
    public void startGame() {
        logger.info("Starting game...");
        roleManager.assignRoles(playerManager.getAlivePlayers());
        turnManager.teleportPlayers();
        wordManager.assignWords(playerManager.getAlivePlayers());

        setGameState(EStates.PLAYING);
        GameUtils.smoothTimeTransition(Objects.requireNonNull(Bukkit.getWorld("world")), 12000);

        for (GamePlayer gp : playerManager.getAlivePlayers()) {
            Player player = gp.getPlayer();
            player.getInventory().clear();

            ERoles role = gp.getRole();
            String ucWord = GameUtils.capitalize(wordManager.getUndercoverWord());
            String civilWord = GameUtils.capitalize(wordManager.getCivilsWord());

            switch (role) {
                case UNDERCOVER -> sendUndercoverMessage(player, ucWord);
                case MR_WHITE -> sendWhiteMessage(player);
                case CIVIL -> sendCivilMessage(player, civilWord);
            }
        }

        if (GameSettings.isShowUndercoverNames()) {
            revealUndercoverTeammates();
        }

        Bukkit.getScheduler().runTaskLater(main, () -> {
            List<GamePlayer> shuffledPlayers = new ArrayList<>(playerManager.getAlivePlayers());
            Collections.shuffle(shuffledPlayers);
            turnManager.startTurns(shuffledPlayers);
            turnManager.nextTurn();
        }, 20 * 5);
    }

    /**
     * Handle game end: show titles, give flight, display roles.
     */
    public void finishGame() {
        GameUtils.clearArmorStands();

        for (Player player : Bukkit.getOnlinePlayers()) {
            GlowUtils.resetGlow(player.getPlayer());
        }
        this.gameFinished = true;
        setGameState(EStates.ENDING);
        GameUtils.smoothTimeTransition(Objects.requireNonNull(Bukkit.getWorld("world")), 4000);

        for (GamePlayer gp : playerManager.getAllGamePlayers()) {
            Player pl = gp.getPlayer();

            if (getWinners() == gp.getRole()) {
                Undercover.getInstance().getStatsManager().addWin(pl.getUniqueId(), gp.getRole().getIdentifier());
            } else {
                Undercover.getInstance().getStatsManager().addLoss(pl.getUniqueId());
            }

            if (pl.isOp()) {
                pl.getInventory().clear();
                pl.getInventory().setItem(0, new ItemBuilder(Material.LEAD).setName("§bNouvelle partie").toItemStack());
            }

            // Remove all potion effects
            pl.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(pl::removePotionEffect);

            pl.setAllowFlight(true);

            showEndTitle(pl, gp.getRole());

            pl.sendMessage("\n§fTu étais " + gp.getRole().getColor() + gp.getRole().getName() + "§f !");
            pl.sendMessage("\n§fLe mot des civils était : §3" + wordManager.getCivilsWord() + "§f.");
            pl.sendMessage("§fLe mot des Undercover était : §3" + wordManager.getUndercoverWord() + "§f.");
        }
    }

    /**
     * Reset everything back to default: settings, players, words, holograms.
     */
    public void resetGame() {
        logger.info("Resetting game...");

        for(Player player : Bukkit.getOnlinePlayers()) {
            if(playerManager.getGamePlayer(player) == null) {
                GamePlayer gamePlayer = new GamePlayer(player);
                playerManager.addPlayer(gamePlayer);
            }
        }

        playerManager.resetAll();
        turnManager.reset();
        wordManager.reset();
        this.gameFinished = false;

        for (ArmorStand as : votingManager.getVoteHolograms().values()) {
            as.remove();
        }

        setGameState(EStates.WAITING);
        GameUtils.smoothTimeTransition(Objects.requireNonNull(Bukkit.getWorld("world")), 4000);
    }

    /**
     * Sends a message to the player indicating they are the Undercover role and their assigned word.
     *
     * @param player The player to send the message to, must not be null.
     * @param word   The word assigned to the player, must not be null.
     */
    private void sendUndercoverMessage(@NotNull Player player, @NotNull String word) {
        if (GameSettings.isShowRole()) {
            player.sendMessage(
                "Tu es " + ERoles.UNDERCOVER.getColor() + ERoles.UNDERCOVER.getName() + "§f !\nTon mot est : §3" + word
                    + "§f !");
            player.sendTitle(ERoles.UNDERCOVER.getColor() + ERoles.UNDERCOVER.getName(), word, 20, 100, 20);
            player.playSound(player.getLocation(), Sound.ENTITY_WOLF_GROWL, 1, 1);
        } else {
            player.sendMessage("Ton mot est : §3" + word + "§f !");
            player.sendTitle("Ton mot", "§b" + word, 20, 100, 20);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        }
    }

    /**
     * Sends a message to the player indicating they are the White role and have no word.
     *
     * @param player The player to send the message to, must not be null.
     */
    private void sendWhiteMessage(@NotNull Player player) {
        player.sendTitle(ERoles.MR_WHITE.getColor() + ERoles.MR_WHITE.getName(), "§fTu n'as pas de mot !", 20, 100, 20);
        player.sendMessage("Tu es le Blanc ! À toi de deviner le mot des Undercover.");
        player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_3, 1, 1);
    }

    /**
     * Sends a message to the player indicating their role and word if applicable.
     * @param player The player to send the message to, must not be null.
     * @param word  The word assigned to the player, must not be null.
     */
    private void sendCivilMessage(@NotNull Player player, @NotNull String word) {
        if (GameSettings.isShowRole()) {
            player.sendMessage(
                "Tu es " + ERoles.CIVIL.getColor() + ERoles.CIVIL.getName() + "§f !\nTon mot est : §3" + word + "§f !");
            player.sendTitle(ERoles.CIVIL.getColor() + ERoles.CIVIL.getName(), word, 20, 100, 20);
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
        } else {
            player.sendMessage("Ton mot est : §3" + word + "§f !");
            player.sendTitle("Ton mot", "§b" + word, 20, 100, 20);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        }
    }

    /**
     * Displays the end title based on the player's role and whether they won or lost.
     *
     * @param player The player to show the title to, must not be null.
     * @param role   The role of the player, must not be null.
     */
    private void showEndTitle(@NotNull Player player, @NotNull ERoles role) {
        if (role == winners) {
            player.sendTitle("§a§lVICTOIRE", "§fTu as gagné en tant que " + role.getColor() + role.getName() + "§f !",
                20, 100, 20);
        } else {
            player.sendTitle("§c§lDÉFAITE", "§fTu as perdu...", 20, 100, 20);
        }
    }

    /**
     * Reveals all undercover teammates to each undercover player. Sends a message with the names of all undercover
     * teammates.
     */
    private void revealUndercoverTeammates() {
        for (GamePlayer gp : playerManager.getAlivePlayers()) {
            if (gp.getRole() == ERoles.UNDERCOVER) {
                Player p = gp.getPlayer();
                p.sendMessage("§cTes coéquipiers Undercover :");
                playerManager.getAlivePlayers().stream().filter(teammate -> teammate.getRole() == ERoles.UNDERCOVER)
                    .forEach(teammate -> p.sendMessage("§f- §3" + teammate.getPlayer().getName()));
            }
        }
    }
}
