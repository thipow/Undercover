package fr.thipow.undercover.game;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.managers.ScoreboardManager;
import fr.thipow.undercover.utils.GameUtils;
import fr.thipow.undercover.utils.ItemBuilder;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Game loop task updating game time, scoreboards and sending role words to players.
 *
 * @author Thipow
 */
public class GameTask extends BukkitRunnable {

    private final Undercover main;

    private final List<String> waitingMessages = Arrays.asList("§eEn attente de joueurs...",
        "§aUtilise /uc music pour désactiver la musique !", "§bInvite tes amis à rejoindre !",
        "§dLa partie va bientôt commencer...");

    private int messageIndex = 0;
    private int tickCounter  = 0;

    public GameTask(Undercover main) {
        this.main = main;
    }

    /**
     * Starts the game task.
     */
    @Override
    public void run() {
        // Update scoreboard
        Bukkit.getOnlinePlayers().forEach(ScoreboardManager::updateScoreboard);

        // Update action bars
        updateActionBars();

        // Update tablist header/footer and give items
        for (Player p : Bukkit.getOnlinePlayers()) {
            giveItems(p);

            Component header = Component.text("Undercover", NamedTextColor.AQUA)
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Mini-jeu social", NamedTextColor.WHITE)).appendNewline();

            Component footer = Component.newline().append(Component.text("Version ", NamedTextColor.GRAY))
                .append(Component.text(Undercover.getInstance().getPluginMeta().getVersion(), NamedTextColor.GREEN))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Développé par ", NamedTextColor.GRAY))
                .append(Component.text("Thipow", NamedTextColor.AQUA));

            p.sendPlayerListHeaderAndFooter(header, footer);
        }
    }

    /**
     * Gives items to players based on the game state.
     *
     * @param player the player to give items to
     */
    private void giveItems(Player player) {
        if (player.isOp() && main.getGameManager().getGameState().equals(EStates.WAITING)) {
            player.getInventory().setItem(4, new ItemBuilder(Material.NETHER_STAR).setName(
                Undercover.getInstance().getConfig().getString("messages.config-item-name")).toItemStack());
        }
        if (player.isOp() && main.getGameManager().getGameState().equals(EStates.ENDING)) {
            player.getInventory().setItem(0, new ItemBuilder(Material.LEAD).setName("§bNouvelle partie").toItemStack());
        }
    }

    /**
     * Updates the action bars for all players with their current word or rotates waiting messages if the game is not in
     * PLAYING state.
     */
    private void updateActionBars() {
        if (main.getGameManager().getGameState().equals(EStates.PLAYING)) {
            for (GamePlayer gamePlayer : main.getGameManager().getPlayerManager().getAlivePlayers()) {
                String word = gamePlayer.getWord();
                if (word.isEmpty()) {
                    word = "Devine le mot !";
                }
                gamePlayer.getPlayer().sendActionBar(GameUtils.legacy("§bMot : §f" + GameUtils.capitalize(word)));
            }
        } else {
            tickCounter++;
            if (tickCounter >= 5) {
                tickCounter = 0;
                messageIndex = (messageIndex + 1) % waitingMessages.size();
            }

            String msg = waitingMessages.get(messageIndex);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendActionBar(GameUtils.legacy(msg));
            }
        }
    }
}
