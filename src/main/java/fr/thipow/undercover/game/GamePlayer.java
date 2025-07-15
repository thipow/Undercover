package fr.thipow.undercover.game;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.utils.GameUtils;
import fr.thipow.undercover.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Represents a player in the Undercover game with their associated role, elimination status, and voting information.
 *
 * @author Thipow
 */

public class GamePlayer {

    private final Player     player;
    private       ERoles     role;
    private       boolean    isEliminated = false;
    private       GamePlayer votedFor     = null;
    private       String     word;


    /**
     * Constructs a GamePlayer instance for the given Bukkit Player.
     *
     * @param player the Bukkit Player to associate with this GamePlayer
     */
    public GamePlayer(Player player) {
        this.player = player;
        Bukkit.getServer().getConsoleSender().sendMessage("Creating GamePlayer for " + player.getName());
    }

    /**
     * Gets the Bukkit Player associated with this GamePlayer.
     *
     * @return the Bukkit Player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the role of this GamePlayer.
     *
     * @return the role of the player
     */
    public ERoles getRole() {
        return role;
    }

    /**
     * Sets the role of this GamePlayer.
     *
     * @param role the role to assign to the player
     */
    public void setRole(ERoles role) {
        this.role = role;
    }

    /**
     * Checks if this GamePlayer is eliminated.
     *
     * @return true if the player is eliminated, false otherwise
     */
    public boolean isEliminated() {
        return isEliminated;
    }

    public void setEliminated(boolean isEliminated) {
        this.isEliminated = isEliminated;
    }


    /**
     * Marks this GamePlayer as eliminated, clearing their inventory, allowing flight, and teleporting them to the
     * center of the map. Also applies invisibility effect and removes them from the turn order.
     */
    public void eliminate() {
        isEliminated = true;
        player.getInventory().clear();
        player.setAllowFlight(true);
        player.setFlying(true);
        player.teleport(GameSettings.getCurrentMap().getCenter());
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 255, false, false));
        Undercover.getInstance().getGameManager().getTurnManager().getTurnOrder().remove(this);
    }

    /**
     * Gets the player this GamePlayer voted for.
     *
     * @return the GamePlayer that this player voted for, or null if no vote was cast
     */
    public GamePlayer getVotedFor() {
        return votedFor;
    }



    /**
     * Sets the player this GamePlayer voted for.
     *
     * @param votedFor the GamePlayer that this player voted for
     */
    public void setVotedFor(GamePlayer votedFor) {
        this.votedFor = votedFor;
    }

    /**
     * Gets the word associated with this GamePlayer, typically used for the Undercover role.
     *
     * @return the word assigned to this player
     */
    public String getWord() {
        return word;
    }

    /**
     * Sets the word for this GamePlayer.
     *
     * @param word the word to assign to this player
     */
    public void setWord(String word) {
        this.word = word;
    }

    /**
     * Resets the GamePlayer to its initial state, clearing role, word, elimination status, and vote. Also resets player
     * inventory, teleportation, potion effects, and sound.
     */
    public void reset() {
        this.role = null;
        this.word = null;
        this.isEliminated = false;
        this.votedFor = null;

        player.stopAllSounds();

        if(Undercover.getInstance().getStatsManager().getMusicStatus(player.getUniqueId())) {
            GameUtils.playWaitingSound(player);
        }

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        player.teleport(GameSettings.getCurrentMap().getSpawn());
        player.setLevel(0);
        player.setExp(0f);
        player.setAllowFlight(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();

        if (player.isOp()) {
            player.getInventory().setItem(4, new ItemBuilder(Material.NETHER_STAR).setName(
                Undercover.getInstance().getConfig().getString("messages.config-item-name")).toItemStack());
        }
        player.getInventory().setItem(0, new ItemBuilder(Material.BOOK).setName("§b§lRègles").toItemStack());

        Undercover.getInstance().getGameManager().getPlayerManager().addPlayer(this);
    }


    /**
     * Checks if this GamePlayer is the same as another object based on their unique ID.
     *
     * @param o the object to compare with
     * @return true if the objects are the same, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GamePlayer that = (GamePlayer) o;
        return player.getUniqueId().equals(that.player.getUniqueId());
    }

    /**
     * Returns the hash code for this GamePlayer based on their unique ID.
     *
     * @return the hash code of the player's unique ID
     */
    @Override
    public int hashCode() {
        return player.getUniqueId().hashCode();
    }
}
