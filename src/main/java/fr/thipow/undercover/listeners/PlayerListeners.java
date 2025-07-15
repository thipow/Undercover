package fr.thipow.undercover.listeners;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.EStates;
import fr.thipow.undercover.game.GamePlayer;
import fr.thipow.undercover.game.GameSettings;
import fr.thipow.undercover.game.managers.TurnManager;
import fr.thipow.undercover.game.managers.VotingManager;
import fr.thipow.undercover.gui.ConfigGUI;
import fr.thipow.undercover.managers.ScoreboardManager;
import fr.thipow.undercover.utils.GameUtils;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Handles player-related events such as join/quit, item interactions, inventory events, and general restrictions.
 *
 * @author Thipow
 */
public class PlayerListeners implements Listener {

    private final Undercover        main   = Undercover.getInstance();
    private final FileConfiguration config = main.getConfig();

    /**
     * Called when a player joins the server. Initializes player state based on the current game state.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (GameSettings.getCurrentMap() == null) {
            player.sendMessage("§c§lAUCUNE MAP N'A ÉTÉ CONFIGURER ! UTILISEZ /uc map <tab> POUR EN CONFIGURER UNE !");
            player.playSound(player.getLocation(), Sound.ENTITY_GHAST_HURT, 1f, 1f);
        }

        EStates gameState = main.getGameManager().getGameState();

        GamePlayer gamePlayer = main.getGameManager().getPlayerManager().getGamePlayer(player);

        if (gamePlayer == null) {
            gamePlayer = new GamePlayer(player);

            if (gameState == EStates.WAITING) {
                setupWaitingPlayer(gamePlayer);
                event.joinMessage(GameUtils.legacy("§b" + player.getName() + " §fa rejoint la partie !"));
            } else {
                setupSpectatorPlayer(gamePlayer);
                event.joinMessage(null);

                gamePlayer.eliminate();
            }
        } else {
            gamePlayer.reset();
            if (gameState == EStates.PLAYING) {
                if (!gamePlayer.isEliminated()) {
                    gamePlayer.eliminate();
                    Bukkit.broadcast(GameUtils.legacy(
                        "§c" + player.getName() + " a reconnecté mais a été éliminé car il a quitté la partie !"));

                    main.getGameManager().getVotingManager().checkVictoryConditions();

                    TurnManager turnManager = main.getGameManager().getTurnManager();
                    if (turnManager.getCurrentPlayer() != null && turnManager.getCurrentPlayer().equals(gamePlayer)) {
                        Bukkit.broadcast(GameUtils.legacy(
                            "§c" + player.getName() + " était le joueur en cours, on passe au suivant !"));
                        turnManager.updateCurrentIndex();
                        turnManager.nextTurn();
                    }

                    setupSpectatorPlayer(gamePlayer);
                } else {
                    setupSpectatorPlayer(gamePlayer);
                }
            } else {
                gamePlayer.reset();
                ScoreboardManager.createScoreboard(player);
                event.joinMessage(GameUtils.legacy("§b" + player.getName() + " §fa rejoint la partie !"));
            }
        }

        player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1f, 1f);

        for (String msg : config.getStringList("messages.join-messages")) {
            player.sendMessage(msg.replace("%player_name%", player.getName()));
        }
    }

    /**
     * Sets up a player when joining during the WAITING state.
     */
    private void setupWaitingPlayer(GamePlayer gamePlayer) {
        gamePlayer.reset();
        main.getGameManager().getPlayerManager().addPlayer(gamePlayer);
        ScoreboardManager.createScoreboard(gamePlayer.getPlayer());
    }

    /**
     * Sets up a player when joining during the PLAYING state as a spectator. Spectators are invisible, can fly, and are
     * teleported to the map center.
     */
    private void setupSpectatorPlayer(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        player.teleport(GameSettings.getCurrentMap().getCenter());
        player.getInventory().clear();
        player.setAllowFlight(true);
        player.setFlying(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 255, false, false));
    }

    /**
     * Event Handlers for various player actions to restrict gameplay during the game. These handlers cancel actions
     * that would disrupt the game flow.
     */
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemPickup(PlayerAttemptPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerPickItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHandSwap(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventorySlotChange(InventoryMoveItemEvent event) {
        event.setCancelled(true);
    }

    /**
     * Handles player item interactions such as GUI opening or skipping turn.
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        Material type = item.getType();

        switch (type) {
            case NETHER_STAR -> {
                new ConfigGUI(player).open(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }

            case FEATHER -> {
                GamePlayer current = Undercover.getInstance().getGameManager().getTurnManager().getCurrentPlayer();
                if (current == null || !player.equals(current.getPlayer())) {
                    return;
                }

                event.setCancelled(true);
                player.getInventory().clear();

                String msg = "§b%player_name% a terminé son tour !";
                Bukkit.broadcast(GameUtils.legacy(msg.replace("%player_name%", player.getName())));

                TurnManager turnManager = Undercover.getInstance().getGameManager().getTurnManager();
                Bukkit.getScheduler().cancelTask(turnManager.getSpeakingBarTaskId());
                Bukkit.getScheduler().cancelTask(turnManager.getSpeakingTimeoutTaskId());
                turnManager.clearTaskIds();

                turnManager.updateCurrentIndex();

                if (turnManager.getCurrentIndex() >= turnManager.getTurnOrder().size()) {
                    main.getGameManager().getVotingManager().startVotingPhase();
                } else {
                    turnManager.nextTurn();
                }
            }
            case DIAMOND -> {
                GamePlayer gamePlayer = Undercover.getInstance().getGameManager().getPlayerManager()
                    .getGamePlayer(player);
                if (gamePlayer == null) {
                    return;
                }

                VotingManager votingManager = Undercover.getInstance().getGameManager().getVotingManager();

                votingManager.playerSkipDiscussion(gamePlayer);

                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }

            case BOOK -> {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                player.sendMessage("§c§lRÈGLES DU JEU\n" +
                    "\n§r\n" +
                    "§fChaque joueur reçoit un mot secret.\n" +
                    "§fMais attention... certains joueurs ont un mot différent :\n" +
                    "§fIls sont les §c§lUndercover§e.\n" +
                    "\n§r\n" +
                    "§fUn joueur n’a §caucun§f mot du tout :\n" +
                    "§fIl s'agit de §f§lMr. White§f.\n" +
                    "\n§r\n" +
                    "§3Votre objectif :\n" +
                    "§fDéduisez qui ment ou improvise, et éliminez les imposteurs !\n" +
                    "§7§o(Mais attention à ne pas vous faire démasquer si vous êtes Undercover ou Mr. White...)\n" +
                    "\n§r\n" +
                    "§3Déroulement du jeu :\n" +
                    "§bPhase de discussion :§f Chaque joueur donne un mot en rapport avec son mot secret.\n" +
                    "§bPhase de vote :§f Les joueurs votent pour éliminer un suspect.\n" +
                    "\n§r\n" +
                    "§cEn cas d'égalité, il faudra savoir vous départager...\n");
            }

            case LEAD -> Undercover.getInstance().getGameManager().resetGame();
        }
    }

    /**
     * Handles cleanup and player removal logic on quit.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = main.getGameManager().getPlayerManager().getGamePlayer(player);
        EStates gameState = main.getGameManager().getGameState();

        ScoreboardManager.deleteScoreboard(player);

        if (gamePlayer == null) {
            return;
        }

        if (gameState == EStates.PLAYING) {
            if (!gamePlayer.isEliminated()) {
                gamePlayer.eliminate();
                Bukkit.broadcast(GameUtils.legacy("§c" + player.getName() + " a quitté la partie et a été éliminé !"));

                main.getGameManager().getVotingManager().checkVictoryConditions();

                TurnManager turnManager = main.getGameManager().getTurnManager();
                if (turnManager.getCurrentPlayer() != null && turnManager.getCurrentPlayer().equals(gamePlayer)) {
                    Bukkit.broadcast(
                        GameUtils.legacy("§c" + player.getName() + " était le joueur en cours, on passe au suivant !"));
                    turnManager.updateCurrentIndex();
                    turnManager.nextTurn();
                }
            }

        } else {
            main.getGameManager().getPlayerManager().removePlayer(player);
            event.quitMessage(GameUtils.legacy("§b" + player.getName() + " §fa quitté la partie !"));
        }
    }
}
