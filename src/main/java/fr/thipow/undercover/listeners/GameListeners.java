package fr.thipow.undercover.listeners;

import fr.thipow.undercover.game.GameManager;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class GameListeners implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if(action != Action.RIGHT_CLICK_AIR && action != Action.LEFT_CLICK_AIR) return;
        
        Player voter = event.getPlayer();
        
        if(!GameManager.isInVotingPhase()) return;

        Player target = getTargetPlayer(voter, 20);
        if(target == null) {
            voter.sendMessage("§cVous ne viser aucun joueur.");
            return;
        }

        GameManager.vote(voter, target);
    }

    public Player getTargetPlayer(Player viewer, double maxDistance){
        List<Entity> nearby = viewer.getNearbyEntities(maxDistance, maxDistance, maxDistance);
        Vector direction = viewer.getEyeLocation().getDirection();

        for(Entity entity : nearby){
            if(!(entity instanceof Player target)) continue;

            Vector toTarget = target.getLocation().toVector().subtract(viewer.getLocation().toVector());
            double angle = direction.angle(toTarget.normalize());

            if(angle < 0.2){
                return target;
            }
        }
        return null;
    }
    
}
