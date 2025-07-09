package fr.thipow.undercover.gui;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.GameSettings;
import fr.thipow.undercover.game.StartingTask;
import fr.thipow.undercover.utils.ItemBuilder;
import fr.thipow.undercover.utils.inventory.FastInv;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ConfigGUI extends FastInv {

    public ConfigGUI(final Player player) {
        super(5 * 9, "§bConfiguration");

        ItemStack blueBorder = new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE, 1).toItemStack();
        ItemStack aquaBorder = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1).toItemStack();

        setItem(0, blueBorder);
        setItem(8, blueBorder);
        setItem(36, blueBorder);
        setItem(44, blueBorder);
        setItem(1, aquaBorder);
        setItem(9, aquaBorder);
        setItem(18, aquaBorder);
        setItem(27, aquaBorder);
        setItem(37, aquaBorder);
        setItem(17, aquaBorder);
        setItem(26, aquaBorder);
        setItem(35, aquaBorder);
        setItem(43, aquaBorder);
        setItem(7, aquaBorder);

        ItemStack startItem = new ItemBuilder(Material.GREEN_DYE, 1).setName("§aC'est parti !").toItemStack();

        setItem(40, startItem, e -> {
            if (StartingTask.isStarted()) {
                StartingTask.toggle(Undercover.getInstance()); // annule le lancement
                player.sendMessage("§cLancement annulé.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
            } else {
                StartingTask.toggle(Undercover.getInstance()); // lance le countdown
                player.sendMessage("§aLancement dans 10 secondes...");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
        });


        ItemStack slotsItem = new ItemBuilder(Material.BEACON, 1).setName("§bNombre de places")
            .setLore("§8", "§fValeur actuelle: §3" + GameSettings.getMaxPlayer() + " places", "§f",
                "§bClique droit pour diminuer",
                "§bClique gauche pour augmenter").toItemStack();

        setItem(20, slotsItem, e -> {

            int slots = GameSettings.getMaxPlayer();
            if (e.getClick() == ClickType.LEFT) {
                if(slots <= GameSettings.getWhiteCount() + GameSettings.getUndercoverCount()){
                    player.sendMessage("§cIl vous faut un minimum de civil pour jouer.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                    GameSettings.setMaxPlayer(GameSettings.getWhiteCount() + GameSettings.getUndercoverCount());
                    refresh(player);
                    return;
                }
                if (slots <= 4) {
                    GameSettings.setMaxPlayer(4);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                    player.sendMessage("§cLe nombre de places ne peut pas être inférieur à 4.");
                    refresh(player);
                } else {
                    GameSettings.setMaxPlayer(slots - 1);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    refresh(player);
                }
            }
            if (e.getClick() == ClickType.RIGHT) {
                if (slots >= 24) {
                    GameSettings.setMaxPlayer(24);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                    player.sendMessage("§cLe nombre de places ne peut pas être supérieur à 24.");
                    refresh(player);
                } else {
                    GameSettings.setMaxPlayer(slots + 1);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    refresh(player);
                }
            }
        });

        ItemStack showUndercoverNames = new ItemBuilder(Material.NAME_TAG, 1).setName("§bConnaître ses alliés")
            .setLore("§8", "§fValeur actuelle: §3" + (GameSettings.isUndercoverNameShowing() ? "§aActivé" : "§cDésactivé"),
                "§f", "§bCliquez pour modifier").toItemStack();

        setItem(21, showUndercoverNames, e -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            GameSettings.setShowUndercoverNames(!GameSettings.isUndercoverNameShowing());
            refresh(player);
        });

        ItemStack whiteEnabledItem = new ItemBuilder(
            (GameSettings.isWhiteEnabled() ? Material.LIGHT_GRAY_DYE : Material.GRAY_DYE), 1).setName("§bMr.White")
            .setLore("§8", "§fValeur actuelle: §3" + (GameSettings.isWhiteEnabled() ? "§aActivé" : "§cDésactivé"), "§f",
                "§bClique gauche").toItemStack();

        setItem(22, whiteEnabledItem, e -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            GameSettings.setWhiteEnabled(!GameSettings.isWhiteEnabled());
            GameSettings.setWhiteCount(GameSettings.isWhiteEnabled() ? 1 : 0);
            refresh(player);
        });

        ItemStack undercoverCountItem = new ItemBuilder(Material.RED_BANNER, 1).setName("§bNombre d'Undercover")
            .setLore("§8", "§fValeur actuelle: §3" + GameSettings.getUndercoverCount(), "§f", "§bClique droit pour diminuer",
                "§bClique gauche pour augmenter")
            .toItemStack();

        setItem(23, undercoverCountItem, e -> {

            int undercoverCount = GameSettings.getUndercoverCount();
            if(e.getClick() == ClickType.LEFT) {
                if(undercoverCount >= (GameSettings.getMaxPlayer() - GameSettings.getWhiteCount()) - (undercoverCount + GameSettings.getUndercoverCount())) {
                    player.sendMessage("§cIl vous faut un minimum de civil pour jouer.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                }else{
                    GameSettings.setUndercoverCount(undercoverCount + 1);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    refresh(player);
                }
            }
            if(e.getClick() == ClickType.RIGHT) {
                if(undercoverCount <= 0) {
                    player.sendMessage("§cIl vous faut au moins 1 undercover pour jouer.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                    GameSettings.setUndercoverCount(1);
                    refresh(player);
                }else{
                    GameSettings.setUndercoverCount(undercoverCount - 1);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                    refresh(player);
                }
            }


        });

        ItemStack whiteCountItem = new ItemBuilder(Material.WHITE_BANNER, 1).setName("§bNombre de Mr.White")
            .setLore("§8", "§fValeur actuelle: §3" + GameSettings.getWhiteCount(), "§f", "§bClique gauche pour diminuer",
                "§bClique droit pour augmenter")
            .toItemStack();

        setItem(24, whiteCountItem, e -> {
            if (GameSettings.isWhiteEnabled()) {
                if (e.getClick() == ClickType.LEFT) {
                    int whiteCount = GameSettings.getWhiteCount();
                    if (whiteCount <= 1) {
                        GameSettings.setWhiteCount(1);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                        player.sendMessage("§cSi vous ne voulez pas de Mr.White, désactivez-le dans la configuration.");
                    } else {
                        GameSettings.setWhiteCount(whiteCount - 1);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        refresh(player);
                    }
                } else if (e.getClick() == ClickType.RIGHT) {
                    int whiteCount = GameSettings.getWhiteCount();
                    if (whiteCount >= GameSettings.getMaxPlayer() - GameSettings.getUndercoverCount()) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                        player.sendMessage(
                            "§cVous ne pouvez pas ajouter plus de Mr.White que le nombre de places disponibles.");
                    } else {
                        GameSettings.setWhiteCount(whiteCount + 1);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        refresh(player);
                    }
                }
            }
        });

        ItemStack summaryItem = new ItemBuilder(Material.NETHER_STAR, 1).setName("§bRésumé de la configuration :")
            .setLore("§8", "§fPlaces : §3" + GameSettings.getMaxPlayer(),
                "§fChat Undercover : " + (GameSettings.isUndercoverNameShowing() ? "§aActivé" : "§cDésactivé"),
                "§fMr.White : " + (GameSettings.isWhiteEnabled() ? "§aActivé" : "§cDésactivé"),
                "§fNombre d'Undercover : §3" + GameSettings.getUndercoverCount(),
                "§fNombre de Mr.White : §3" + GameSettings.getWhiteCount()).toItemStack();

        setItem(4, summaryItem);
    }

    private static void refresh(Player player) {
        ConfigGUI configGUI = new ConfigGUI(player);
        configGUI.open(player);
    }

}
