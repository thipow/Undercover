package fr.thipow.undercover.listeners;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.EStates;
import fr.thipow.undercover.game.GameManager;
import io.papermc.paper.event.player.AsyncChatEvent;
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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Handles all in-game player interactions and voting logic for the Undercover plugin.
 * @author Thipow
 */
public class GameListeners implements Listener {

    private final FileConfiguration config = Undercover.getInstance().getConfig();
    private final Map<UUID, Long> voteCooldowns = new HashMap<>();

    /**
     * Handles voting via right/left click in the air.
     * Players vote by aiming at another player and clicking.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player voter = event.getPlayer();
        Action action = event.getAction();

        if (!GameManager.isInVotingPhase() ||
            !(action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_AIR)) {
            return;
        }

        long now = System.currentTimeMillis();
        long lastVote = voteCooldowns.getOrDefault(voter.getUniqueId(), 0L);
        if (now - lastVote < 2000) {
            voter.sendMessage("§cVeuillez patienter avant de voter à nouveau.");
            voter.playSound(voter.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
            return;
        }

        Player target = getTargetPlayer(voter, 20);

        if (target == null) {
            if (voter.getLocation().getPitch() > 50) {
                target = voter;
                voter.sendMessage("§eVous vous êtes voté vous-même.");
            } else {
                String msg = config.getString("messages.not-aiming-anyone");
                if (msg != null) {
                    voter.sendMessage(msg);
                }
                voter.playSound(voter.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                return;
            }
        }

        if (!Undercover.getInstance().getGameManager().getGamePlayers().contains(target)) {
            return;
        }

        GameManager.vote(voter, target);
        voteCooldowns.put(voter.getUniqueId(), now);
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
        if (!gameManager.isGameState(EStates.PLAYING)) return;

        Player player = event.getPlayer();
        if (!gameManager.getGamePlayers().contains(player)) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        if (from.distanceSquared(to) > 0) {
            event.setTo(new Location(from.getWorld(), from.getX(), from.getY(), from.getZ(), to.getYaw(), to.getPitch()));
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

            String formattedMessage = format
                .replace("%player_name%", player.getName())
                .replace("%message%", message);

            // Cancel default event and broadcast custom message
            event.setCancelled(true);
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(formattedMessage));
        }
    }

    /**
     * Returns the player the viewer is aiming at within a given distance.
     *
     * @param viewer      the player who is looking
     * @param maxDistance the max distance to check
     * @return the targeted player, or null if none
     */
    public Player getTargetPlayer(Player viewer, double maxDistance) {
        List<Entity> nearby = viewer.getNearbyEntities(maxDistance, maxDistance, maxDistance);
        Vector viewDirection = viewer.getEyeLocation().getDirection();

        for (Entity entity : nearby) {
            if (!(entity instanceof Player target)) continue;

            Vector toTarget = target.getLocation().toVector().subtract(viewer.getLocation().toVector());
            double angle = viewDirection.angle(toTarget.normalize());

            if (angle < 0.2) {
                return target;
            }
        }

        return null;
    }

}
