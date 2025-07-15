package fr.thipow.undercover.listeners;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.EStates;
import fr.thipow.undercover.game.GameManager;
import fr.thipow.undercover.game.GamePlayer;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.util.Vector;

/**
 * Handles all in-game player interactions and voting logic for the Undercover plugin.
 *
 * @author Thipow
 */
public class GameListeners implements Listener {

    private final FileConfiguration config        = Undercover.getInstance().getConfig();
    private final Map<UUID, Long>   voteCooldowns = new HashMap<>();

    /**
     * Handles voting via right/left click in the air. Players vote by aiming at another player and clicking.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        GamePlayer voter = Undercover.getInstance().getGameManager().getPlayerManager()
            .getGamePlayer(event.getPlayer());
        Action action = event.getAction();

        if (!Undercover.getInstance().getGameManager().getVotingManager().isInVotingPhase() || !(
            action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_AIR)) {
            return;
        }

        long now = System.currentTimeMillis();
        long lastVote = voteCooldowns.getOrDefault(voter.getPlayer().getUniqueId(), 0L);
        if (now - lastVote < 2000) {
            voter.getPlayer().sendMessage("§cVeuillez patienter avant de voter à nouveau.");
            voter.getPlayer().playSound(voter.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
            return;
        }

        GamePlayer target = getTargetPlayer(voter, 20);

        if (target == null) {
            if (voter.getPlayer().getEyeLocation().getPitch() > 50) {
                target = voter;
                voter.getPlayer().sendMessage("§eVous vous êtes voté vous-même.");
            } else {
                String msg = config.getString("messages.not-aiming-anyone");
                if (msg != null) {
                    voter.getPlayer().sendMessage(msg);
                }
                voter.getPlayer().playSound(voter.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                return;
            }
        }

        if (!Undercover.getInstance().getGameManager().getPlayerManager().getAlivePlayers().contains(target)) {
            return;
        }

        Undercover.getInstance().getGameManager().getVotingManager().vote(voter, target);
        voteCooldowns.put(voter.getPlayer().getUniqueId(), now);
    }

    /**
     * Handles sneaking to remove a vote. Players can sneak to retract their vote during the voting phase.
     */
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        GameManager gameManager = Undercover.getInstance().getGameManager();
        if (!gameManager.getVotingManager().isInVotingPhase()) {
            return;
        }

        Player player = event.getPlayer();
        GamePlayer gamePlayer = gameManager.getPlayerManager().getGamePlayer(player);
        if (gamePlayer == null) {
            return;
        }

        if (!event.isSneaking()) {
            return;
        }

        if (gamePlayer.getVotedFor() != null) {
            gameManager.getVotingManager().removeVote(gamePlayer);
            player.sendMessage("§eVotre vote a été retiré.");
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
        }
    }

    /**
     * Prevents weather changes during the game.
     */
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    /**
     * Prevents players from moving during the PLAYING phase.
     */
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        GameManager gameManager = Undercover.getInstance().getGameManager();
        if (!gameManager.getGameState().equals(EStates.PLAYING)) {
            return;
        }

        Player player = event.getPlayer();
        if (!gameManager.getPlayerManager().getAlivePlayers()
            .contains(gameManager.getPlayerManager().getGamePlayer(player))) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.distanceSquared(to) > 0) {
            event.setTo(
                new Location(from.getWorld(), from.getX(), from.getY(), from.getZ(), to.getYaw(), to.getPitch()));
        }
    }


    /**
     * Formats chat messages with custom format from config.
     */
    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String format = config.getString("messages.chat-format");

        if (format != null) {
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());

            String formattedMessage = format.replace("%player_name%", player.getName()).replace("%message%", message);

            event.setCancelled(true);
            if (!Undercover.getInstance().getGameManager().getGameState().equals(EStates.PLAYING)
                || Undercover.getInstance().getGameManager().getVotingManager().isInVotingPhase()) {
                Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(formattedMessage));
            } else {
                if (Undercover.getInstance().getGameManager().getTurnManager().getCurrentPlayer()
                    .equals(Undercover.getInstance().getGameManager().getPlayerManager().getGamePlayer(player))) {
                    Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(formattedMessage));
                } else {
                    player.sendMessage("§cVous ne pouvez pas parler pendant le tour d'un autre joueur.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                }
            }


        }
    }

    /**
     * Returns the player the viewer is aiming at within a given distance.
     *
     * @param viewer      the player who is looking
     * @param maxDistance the max distance to check
     * @return the targeted player, or null if none
     */
    public GamePlayer getTargetPlayer(GamePlayer viewer, double maxDistance) {
        List<Entity> nearby = viewer.getPlayer().getNearbyEntities(maxDistance, maxDistance, maxDistance);
        Vector viewDirection = viewer.getPlayer().getEyeLocation().getDirection();

        for (Entity entity : nearby) {
            if (!(entity instanceof Player target)) {
                continue;
            }
            if (target.getUniqueId().equals(viewer.getPlayer().getUniqueId())) {
                continue;
            }

            Vector toTarget = target.getLocation().toVector().subtract(viewer.getPlayer().getEyeLocation().toVector());
            double angle = viewDirection.angle(toTarget.normalize());

            if (angle < 0.2) {
                return Undercover.getInstance().getGameManager().getPlayerManager().getGamePlayer(target);
            }
        }

        return null;
    }


}
