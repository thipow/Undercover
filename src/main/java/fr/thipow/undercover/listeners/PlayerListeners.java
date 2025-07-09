package fr.thipow.undercover.listeners;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.EStates;
import fr.thipow.undercover.game.GameManager;
import fr.thipow.undercover.gui.ConfigGUI;
import fr.thipow.undercover.managers.ScoreboardManager;
import fr.thipow.undercover.utils.ItemBuilder;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListeners implements Listener {

    private final Undercover main = Undercover.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        event.setJoinMessage(null);
        player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1, 1);
        player.sendMessage("§aBienvenue dans §2Undercover!");

        if(main.getGameManager().isGameState(EStates.WAITING)) {
            ScoreboardManager.createScoreboard(player);
            main.getGameManager().getGamePlayers().add(player);

            if(player.isOp()){
                player.getInventory().setItem(4, new ItemBuilder(Material.NETHER_STAR).setName("§bConfiguration").toItemStack());
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if(item == null) return;

        if (item.getType().equals(Material.NETHER_STAR)) {
            ConfigGUI configGUI = new ConfigGUI(player);
            configGUI.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }

        if(event.getItem().getType() == Material.FEATHER){
            if(!player.equals(GameManager.getCurrentPlayer())) return;

            event.setCancelled(true);
            player.getInventory().setItem(8, item);
            GameManager.nextTurn();
            Bukkit.broadcastMessage(GameManager.getCurrentPlayer() + "à passé son tour de parole !");
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerPickItemEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onHandSwap(PlayerSwapHandItemsEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventorySlotChange(InventoryMoveItemEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        event.setQuitMessage(null);
        ScoreboardManager.deleteScoreboard(player);
        if(main.getGameManager().isGameState(EStates.WAITING)) {
            main.getGameManager().getGamePlayers().remove(player);
        }
    }

}
