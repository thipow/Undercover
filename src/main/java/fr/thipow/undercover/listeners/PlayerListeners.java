package fr.thipow.undercover.listeners;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.EStates;
import fr.thipow.undercover.game.GameManager;
import fr.thipow.undercover.game.GameSettings;
import fr.thipow.undercover.gui.ConfigGUI;
import fr.thipow.undercover.managers.ScoreboardManager;
import fr.thipow.undercover.utils.GameUtils;
import fr.thipow.undercover.utils.ItemBuilder;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
 * Handles player-related events such as join/quit, item interactions,
 * inventory events, and general restrictions.
 * @author Thipow
 */
public class PlayerListeners implements Listener {

    private final Undercover        main   = Undercover.getInstance();
    private final FileConfiguration config = main.getConfig();

    /**
     * Called when a player joins the server.
     * Initializes player state based on the current game state.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!(main.getGameManager().isGameState(EStates.PLAYING))){
            event.joinMessage(GameUtils.legacy("§b" + player.getName() + " §fa rejoint la partie !"));
        }
        player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1, 1);

        for (String msg : config.getStringList("messages.join-messages")) {
            player.sendMessage(msg.replace("%player_name%", player.getName()));
        }

        if (main.getGameManager().isGameState(EStates.WAITING)) {
            setupWaitingPlayer(player);
        } else {
            setupSpectatorPlayer(player);
        }
    }

    /**
     * Sets up a player when joining during the WAITING state.
     */
    private void setupWaitingPlayer(Player player) {
        Location spawn = new Location(Bukkit.getWorld("world"), -16.5, 69, 0.5, -90, 0);
        player.teleport(spawn);
        player.setLevel(0);
        player.setExp(0f);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.playSound(new Location(Bukkit.getWorld("world"), 0, 69, 0), Sound.MUSIC_DISC_OTHERSIDE, 0.15f, 1);

        ScoreboardManager.createScoreboard(player);
        main.getGameManager().getGamePlayers().add(player);

        if (player.isOp()) {
            ItemStack configItem = new ItemBuilder(Material.NETHER_STAR)
                .setName(config.getString("messages.config-item-name"))
                .toItemStack();
            player.getInventory().setItem(4, configItem);
        }
    }

    /**
     * Sets up a player as a spectator when the game is running.
     */
    private void setupSpectatorPlayer(Player player) {
        Location specLocation = new Location(Bukkit.getWorld("world"), 0.5, 75, 0.5, -90, 0);
        player.teleport(specLocation);
        player.getInventory().clear();
        player.setAllowFlight(true);
        player.setFlying(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
    }

    @EventHandler public void onDamage(EntityDamageEvent event) { event.setCancelled(true); }
    @EventHandler public void onBlockPlace(BlockPlaceEvent event) { event.setCancelled(true); }
    @EventHandler public void onBlockBreak(BlockBreakEvent event) { event.setCancelled(true); }
    @EventHandler public void onItemPickup(PlayerAttemptPickupItemEvent event) { event.setCancelled(true); }
    @EventHandler public void onPickup(PlayerPickItemEvent event) { event.setCancelled(true); }
    @EventHandler public void onFoodLevelChange(FoodLevelChangeEvent event) { event.setCancelled(true); }
    @EventHandler public void onDrop(PlayerDropItemEvent event) { event.setCancelled(true); }
    @EventHandler public void onHandSwap(PlayerSwapHandItemsEvent event) { event.setCancelled(true); }
    @EventHandler public void onInventoryDrag(InventoryDragEvent event) { event.setCancelled(true); }
    @EventHandler public void onInventorySlotChange(InventoryMoveItemEvent event) { event.setCancelled(true); }

    /**
     * Handles player item interactions such as GUI opening or skipping turn.
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR) return;

        Material type = item.getType();

        switch (type) {
            case NETHER_STAR -> {
                new ConfigGUI(player).open(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }

            case FEATHER -> {
                if (!player.equals(GameManager.getCurrentPlayer())) return;
                event.setCancelled(true);
                player.getInventory().clear();

                String msg = config.getString("messages.speaking-time-exceeded");
                if (msg != null) {
                    Bukkit.broadcast(GameUtils.legacy(msg.replace("%player_name%", player.getName())));
                }

                GameManager.currentPlayerIndex++;
                GameManager.nextTurn();
            }

            case LEAD -> GameManager.resetGame();
        }
    }

    /**
     * Handles cleanup and player removal logic on quit.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(main.getGameManager().getGamePlayers().contains(player)) {
            event.quitMessage(GameUtils.legacy("§b" + player.getName() + " §fa quitté la partie !"));
        }
        ScoreboardManager.deleteScoreboard(player);

        EStates state = main.getGameManager().getGameState();
        main.getGameManager().getGamePlayers().remove(player);

        if (state == EStates.PLAYING) {
            String leaveMsg = config.getString("messages.join-message");
            if (leaveMsg != null) {
                Bukkit.broadcast(GameUtils.legacy(GameSettings.getPrefix() + leaveMsg.replace("%player_name%", player.getName())));
            }

            if (player.equals(GameManager.getCurrentPlayer())) {
                Bukkit.broadcast(GameUtils.legacy("§c" + player.getName() + " était le joueur en cours, on passe au suivant !"));
                GameManager.currentPlayerIndex++;
                GameManager.nextTurn();
            }
        }
    }
}
