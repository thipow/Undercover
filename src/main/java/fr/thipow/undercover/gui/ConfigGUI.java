package fr.thipow.undercover.gui;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.GameSettings;
import fr.thipow.undercover.game.StartingTask;
import fr.thipow.undercover.utils.ItemBuilder;
import fr.thipow.undercover.utils.inventory.FastInv;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * GUI for configuring game settings in Undercover. This GUI allows players to modify various game settings such as
 * maximum players, undercover count, meeting time, and more.
 *
 * @author Thipow
 */

public class ConfigGUI extends FastInv {

    private final FileConfiguration config = Undercover.getInstance().getConfig();

    public ConfigGUI(final Player player) {
        super(6 * 9, Undercover.getInstance().getConfig().getString("messages.guis.config-gui.name"));

        // Set borders for the GUI
        setBorders();

        // Reset Item
        // When clicked, reset GameSettings to default
        ItemStack resetItem = new ItemBuilder(Material.TNT).setName("§cRétablir la configuration")
            .setLore("§8", "§fCliquez pour réinitialiser la configuration", "§f",
                "§cAttention, cette action est irréversible !").toItemStack();
        setItem(53, resetItem, e -> {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
            GameSettings.loadDefaults();
            refresh(player);
        });

        // Start and stop item
        // Toggle game start or stop
        ItemStack startItem = new ItemBuilder(StartingTask.isStarted() ? Material.RED_DYE : Material.LIME_DYE,
            1).setName(StartingTask.isStarted() ? "§cOn annule tout !" : "§aC'est parti !").toItemStack();
        setItem(49, startItem, e -> {
            if (GameSettings.getCurrentMap() == null) {
                player.sendMessage("§cVeuillez sélectionner une map avant de démarrer le jeu.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            if(Undercover.getInstance().getGameManager().getPlayerManager().getAllGamePlayers().size() < 3){
                player.sendMessage("§cIl vous faut au moins 3 joueurs pour démarrer une partie.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            StartingTask.toggle(Undercover.getInstance());
            if (StartingTask.isStarted()) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            } else {
                player.sendMessage(Objects.requireNonNull(config.getString("messages.starting-stopped")));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
            }
            refresh(player);
        });

        // Toggle private vote item
        ItemStack privateVoteItem = new ItemBuilder(Material.PAPER, 1).setName("§bVotes anonymes")
            .setLore("§8", "§fValeur actuelle: §3" + (GameSettings.isPrivateVote() ? "§aActivé" : "§cDésactivé"), "§f",
                "§bCliquez pour modifier").toItemStack();
        setItem(21, privateVoteItem,
            e -> toggleAndRefresh(player, () -> GameSettings.setPrivateVote(!GameSettings.isPrivateVote())));

        // Map selector item
        ItemStack mapSelectorItem = new ItemBuilder(Material.GRASS_BLOCK, 1).setName("§bSelecteur de carte")
            .setLore("§8", "§fCarte actuelle: §3" + (GameSettings.getCurrentMap() == null ? "§cAucune"
                : GameSettings.getCurrentMap().getDisplayName()), "§f", "§bCliquez pour modifier").toItemStack();
        setItem(22, mapSelectorItem, e -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            new MapsGUI(player).open(player);
        });

        // Toggle show role item
        ItemStack showRoleItem = new ItemBuilder(Material.LECTERN, 1).setName("§bConnaître son rôle")
            .setLore("§8", "§fValeur actuelle: §3" + (GameSettings.isShowRole() ? "§aActivé" : "§cDésactivé"), "§f",
                "§bCliquez pour modifier").toItemStack();
        setItem(23, showRoleItem,
            e -> toggleAndRefresh(player, () -> GameSettings.setShowRole(!GameSettings.isShowRole())));

        // Change max players item
        // LEFT click decreases, RIGHT click increases
        ItemStack slotsItem = new ItemBuilder(Material.BEACON, 1).setName("§bNombre de places")
            .setLore("§8", "§fValeur actuelle: §3" + GameSettings.getMaxPlayers() + " places", "§f",
                "§bClique droit pour diminuer", "§bClique gauche pour augmenter").toItemStack();
        setItem(29, slotsItem, e -> handleMaxPlayersChange(e.getClick(), player));

        // Toggle show undercover names item
        ItemStack showUndercoverNames = new ItemBuilder(Material.NAME_TAG, 1).setName("§bConnaître ses alliés")
            .setLore("§8",
                "§fValeur actuelle: §3" + (GameSettings.isShowUndercoverNames() ? "§aActivé" : "§cDésactivé"), "§f",
                "§bCliquez pour modifier").toItemStack();
        setItem(30, showUndercoverNames, e -> toggleAndRefresh(player,
            () -> GameSettings.setShowUndercoverNames(!GameSettings.isShowUndercoverNames())));

        // Toggle white item
        ItemStack whiteEnabledItem = new ItemBuilder(
            (GameSettings.isWhiteEnabled() ? Material.LIGHT_GRAY_DYE : Material.GRAY_DYE), 1).setName("§bMr.White")
            .setLore("§8", "§fValeur actuelle: §3" + (GameSettings.isWhiteEnabled() ? "§aActivé" : "§cDésactivé"), "§f",
                "§bClique gauche").toItemStack();
        setItem(33, whiteEnabledItem,
            e -> toggleAndRefresh(player, () -> GameSettings.setWhiteEnabled(!GameSettings.isWhiteEnabled())));

        // Undercover count item
        ItemStack undercoverCountItem = new ItemBuilder(Material.RED_BANNER, 1).setName("§bNombre d'Undercover")
            .setLore("§8", "§fValeur actuelle: §3" + GameSettings.getUndercoverCount(), "§f",
                "§bClique droit pour diminuer", "§bClique gauche pour augmenter").toItemStack();
        setItem(32, undercoverCountItem, e -> handleUndercoverCountChange(e.getClick(), player));

        // Meeting time item
        ItemStack meetingTimeItem = new ItemBuilder(Material.CLOCK, 1).setName("§bTemps de discussion").setLore("§8",
            "§fValeur actuelle: §3" + (GameSettings.getMeetingTime() == 0 ? "§cDésactivé"
                : GameSettings.getMeetingTime() == 1 ? GameSettings.getMeetingTime() + " seconde"
                    : GameSettings.getMeetingTime() + " secondes"), "§f", "§bClique gauche pour diminuer",
            "§bClique droit pour augmenter").toItemStack();
        setItem(31, meetingTimeItem, e -> handleMeetingTimeChange(e.getClick(), player));

        // Summary item
        ItemStack summaryItem = new ItemBuilder(Material.NETHER_STAR, 1).setName("§bRésumé de la configuration :")
            .setLore("§8", "§fPlaces : §3" + GameSettings.getMaxPlayers(),
                "§fListe des Undercovers : " + (GameSettings.isShowUndercoverNames() ? "§aActivé" : "§cDésactivé"),
                "§fVotes anonymes : " + (GameSettings.isPrivateVote() ? "§aActivé" : "§cDésactivé"),
                "§fRôles cachés : " + (GameSettings.isShowRole() ? "§aActivé" : "§cDésactivé"),
                "§fMr.White : " + (GameSettings.isWhiteEnabled() ? "§aActivé" : "§cDésactivé"),
                "§fNombre d'Undercover : §3" + GameSettings.getUndercoverCount(),
                "§fTemps de discussion : §3" + (GameSettings.getMeetingTime() == 0 ? "§cDésactivé"
                    : GameSettings.getMeetingTime() == 1 ? GameSettings.getMeetingTime() + " seconde"
                        : GameSettings.getMeetingTime() + " secondes")).toItemStack();
        setItem(4, summaryItem);
    }

    /**
     * Refreshes the GUI for the given player.
     *
     * @param player The player whose GUI should be refreshed.
     */
    private static void refresh(Player player) {
        new ConfigGUI(player).open(player);
    }

    /**
     * Places decorative borders around the GUI.
     */
    private void setBorders() {
        ItemStack blueBorder = new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE, 1).toItemStack();
        ItemStack aquaBorder = new ItemBuilder(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1).toItemStack();
        int[] blueSlots = {0, 8, 45, 53};
        int[] aquaSlots = {1, 9, 18, 27, 46, 35, 36, 17, 26, 44, 52, 7};
        for (int slot : blueSlots) {
            setItem(slot, blueBorder);
        }
        for (int slot : aquaSlots) {
            setItem(slot, aquaBorder);
        }
    }

    /*
     * Utility method to toggle a setting and refresh the GUI.
     * @param player the player who clicked
     */
    private void toggleAndRefresh(Player player, Runnable action) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        action.run();
        refresh(player);
    }

    /**
     * Handle click events for changing the maximum number of players.
     *
     * @param click  the ClickType of the event
     * @param player the player who clicked
     */
    private void handleMaxPlayersChange(ClickType click, Player player) {
        int slots = GameSettings.getMaxPlayers();

        if (click == ClickType.LEFT) {
            if (slots <= (GameSettings.isWhiteEnabled() ? 1 : 0) + GameSettings.getUndercoverCount()) {
                player.sendMessage("§cIl vous faut un minimum de civil pour jouer.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
            } else if (slots <= 3) {
                GameSettings.setMaxPlayers(3);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                player.sendMessage("§cLe nombre de places ne peut pas être inférieur à 3.");
            } else {
                GameSettings.setMaxPlayers(slots - 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        } else if (click == ClickType.RIGHT) {
            if (slots >= Undercover.getInstance().getMapsManager()
                .getMap(GameSettings.getCurrentMap().getName()).getSpawns().size()) {
                GameSettings.setMaxPlayers(
                    Undercover.getInstance().getMapsManager()
                        .getMap(GameSettings.getCurrentMap().getName())
                        .getSpawns().size());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                player.sendMessage(
                    "§cLe nombre de places ne peut pas être supérieur à "
                        + Undercover.getInstance().getMapsManager()
                        .getMap(GameSettings.getCurrentMap().getName()).getSpawns().size() + ".");
            } else {
                GameSettings.setMaxPlayers(slots + 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        }

        int maxPlayers = GameSettings.getMaxPlayers();
        int maxUndercover = (maxPlayers % 2 == 0) ? (maxPlayers / 2 - 1) : (maxPlayers / 2);
        if (GameSettings.getUndercoverCount() > maxUndercover) {
            GameSettings.setUndercoverCount(maxUndercover);
            player.sendMessage("§eLe nombre d'undercover a été ajusté à §6" + maxUndercover + "§e car vous avez changer le nombre de places.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
        }

        refresh(player);
    }


    /**
     * Handle click events for toggling private vote.
     *
     * @param player the player who clicked
     */
    private void handleUndercoverCountChange(ClickType click, Player player) {
        int undercoverCount = GameSettings.getUndercoverCount();
        int maxPlayers = GameSettings.getMaxPlayers();
        int maxUndercover = (maxPlayers % 2 == 0) ? (maxPlayers / 2 - 1) : (maxPlayers / 2);
        if (click == ClickType.LEFT) {
            if (undercoverCount >= maxUndercover) {
                player.sendMessage("§cIl vous faut un minimum de civil pour jouer.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
            } else {
                GameSettings.setUndercoverCount(undercoverCount + 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        } else if (click == ClickType.RIGHT) {
            if (undercoverCount <= 1) {
                player.sendMessage("§cIl vous faut au moins 1 undercover pour jouer.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                GameSettings.setUndercoverCount(1);
            } else {
                GameSettings.setUndercoverCount(undercoverCount - 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        }
        refresh(player);
    }

    /**
     * Handle click events for changing meeting time.
     *
     * @param click  the ClickType of the event
     * @param player the player who clicked
     */
    private void handleMeetingTimeChange(ClickType click, Player player) {
        int meetingTime = GameSettings.getMeetingTime();
        if (click == ClickType.LEFT) {
            if (meetingTime <= 0) {
                GameSettings.setMeetingTime(0);
                player.sendMessage("§cLe temps de discussion est déjà désactivé.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            } else {
                GameSettings.setMeetingTime(meetingTime - 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        } else if (click == ClickType.RIGHT) {
            if (meetingTime >= 60) {
                GameSettings.setMeetingTime(60);
                player.sendMessage("§cLe temps de discussion est déjà au maximum.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            } else {
                GameSettings.setMeetingTime(meetingTime + 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }
        }
        refresh(player);
    }

}
