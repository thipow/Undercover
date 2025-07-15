package fr.thipow.undercover.game.managers;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.ERoles;
import fr.thipow.undercover.game.GameManager;
import fr.thipow.undercover.game.GamePlayer;
import fr.thipow.undercover.game.GameSettings;
import fr.thipow.undercover.utils.GameUtils;
import fr.thipow.undercover.utils.GlowUtils;
import fr.thipow.undercover.utils.ItemBuilder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class VotingManager {

    private final FileConfiguration           config          = Undercover.getInstance().getConfig();
    private final GameManager                 gameManager;
    private final Map<GamePlayer, ArmorStand> voteHolograms   = new HashMap<>();
    private final Set<GamePlayer>             skipVotes       = new HashSet<>();
    private       boolean                     isInVotingPhase = false;
    private       boolean                     lastTieState    = false;
    private       BukkitTask                  discussionTimerTask;
    private       BukkitTask                  endDiscussionTask;

    public VotingManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * Réinitialise tous les votes des joueurs vivants.
     */
    public void resetVotes() {
        List<GamePlayer> alivePlayers = gameManager.getPlayerManager().getAlivePlayers();
        for (GamePlayer player : alivePlayers) {
            player.setVotedFor(null);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            GlowUtils.resetGlow(player.getPlayer());
        }
        skipVotes.clear();  // Réinitialise aussi les skips
        isInVotingPhase = false;
        lastTieState = false;

        // Suppression hologrammes
        for (ArmorStand hologram : voteHolograms.values()) {
            if (hologram != null && !hologram.isDead()) {
                hologram.remove();
            }
        }
        voteHolograms.clear();
    }

    /**
     * Enregistre le vote d’un joueur vers une cible.
     */
    public void vote(GamePlayer voter, GamePlayer target) {
        GamePlayer previousVote = voter.getVotedFor();
        if (previousVote == target) {
            voter.getPlayer().sendMessage("§cVous avez déjà voté pour ce joueur.");
            return;
        }

        voter.setVotedFor(target);

        voter.getPlayer().sendMessage(Objects.requireNonNull(config.getString("messages.voting-for"))
            .replace("%target_player%", target.getPlayer().getName()));

        if (!GameSettings.isPrivateVote()) {
            Bukkit.broadcast(
                GameUtils.legacy("§b" + voter.getPlayer().getName() + " §fa voté pour §b" + target.getPlayer().getName() + "§f!"));
            updateVoteHolograms();
        }
        GlowUtils.resetGlow(voter.getPlayer());
        GlowUtils.addGlow(voter.getPlayer(), target.getPlayer());

        if (allVoted()) {
            GamePlayer eliminated = getEliminatedPlayer();

            if (eliminated == null) {
                if (!lastTieState) {
                    Bukkit.broadcast(GameUtils.legacy("§cÉgalité ! Vote bloqué jusqu'à ce qu'un joueur change d'avis..."));
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1f, 1f);
                    }
                    lastTieState = true;
                }
                return;
            }

            // S'il y a un joueur éliminé, on remet l'état de l'égalité à false
            lastTieState = false;

            Bukkit.getScheduler().runTask(Undercover.getInstance(), this::endVotingPhase);
        }
    }


    /**
     * Vérifie si un joueur a déjà voté.
     */
    public boolean hasVoted(GamePlayer player) {
        return player.getVotedFor() != null;
    }

    /**
     * Supprime le vote d’un joueur.
     */
    public void removeVote(GamePlayer player) {
        player.setVotedFor(null);
        updateVoteHolograms();
        GlowUtils.resetGlow(player.getPlayer());
    }

    /**
     * Vérifie si tous les joueurs vivants ont voté.
     */
    public boolean allVoted() {
        List<GamePlayer> alivePlayers = gameManager.getPlayerManager().getAlivePlayers();
        for (GamePlayer player : alivePlayers) {
            if (!hasVoted(player)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compte les votes pour chaque joueur.
     */
    public Map<GamePlayer, Integer> countVotes() {
        List<GamePlayer> alivePlayers = gameManager.getPlayerManager().getAlivePlayers();
        Map<GamePlayer, Integer> results = new HashMap<>();

        for (GamePlayer voter : alivePlayers) {
            GamePlayer voted = voter.getVotedFor();
            if (voted != null) {
                results.put(voted, results.getOrDefault(voted, 0) + 1);
            }
        }

        return results;
    }

    /**
     * Retourne le joueur éliminé (ou null en cas d’égalité).
     */
    public GamePlayer getEliminatedPlayer() {
        Map<GamePlayer, Integer> voteCounts = countVotes();
        GamePlayer eliminated = null;
        int maxVotes = 0;
        boolean tie = false;

        for (Map.Entry<GamePlayer, Integer> entry : voteCounts.entrySet()) {
            int count = entry.getValue();
            if (count > maxVotes) {
                eliminated = entry.getKey();
                maxVotes = count;
                tie = false;
            } else if (count == maxVotes) {
                tie = true;
            }
        }

        return tie ? null : eliminated;
    }

    /**
     * Met à jour les hologrammes affichant le nombre de votes au-dessus des joueurs.
     */
    public void updateVoteHolograms() {
        List<GamePlayer> alivePlayers = gameManager.getPlayerManager().getAlivePlayers();

        Map<GamePlayer, Integer> voteCounts = new HashMap<>();
        for (GamePlayer voter : alivePlayers) {
            GamePlayer votedFor = voter.getVotedFor();
            if (votedFor != null) {
                voteCounts.put(votedFor, voteCounts.getOrDefault(votedFor, 0) + 1);
            }
        }

        for (GamePlayer target : alivePlayers) {
            Player targetPlayer = target.getPlayer();
            int votes = voteCounts.getOrDefault(target, 0);

            ArmorStand hologram = voteHolograms.get(target);

            if (votes == 0) {
                if (hologram != null && !hologram.isDead()) {
                    hologram.remove();
                }
                voteHolograms.remove(target);
                continue;
            }

            if (hologram == null || hologram.isDead()) {
                hologram = (ArmorStand) targetPlayer.getWorld()
                    .spawnEntity(targetPlayer.getLocation().add(0, 2.5, 0), EntityType.ARMOR_STAND);
                hologram.setInvisible(true);
                hologram.setCustomNameVisible(true);
                hologram.setMarker(true);
                hologram.setGravity(false);

                voteHolograms.put(target, hologram);
            }

            hologram.teleport(targetPlayer.getLocation().add(0, 2.25, 0));
            hologram.setCustomName("§bVotes: §f" + votes);
        }
    }

    /**
     * Vérifie les conditions de victoire après le vote et gère la suite de la partie.
     */
    public void checkVictoryConditions() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            GlowUtils.resetGlow(player.getPlayer());
        }
        long undercoverLeft = gameManager.getPlayerManager().getAlivePlayers().stream()
            .filter(p -> p.getRole() == ERoles.UNDERCOVER).count();
        long civilLeft = gameManager.getPlayerManager().getAlivePlayers().stream()
            .filter(p -> p.getRole() == ERoles.CIVIL).count();
        long whiteLeft = gameManager.getPlayerManager().getAlivePlayers().stream()
            .filter(p -> p.getRole() == ERoles.MR_WHITE).count();

        if (undercoverLeft == 0) {
            Bukkit.broadcast(GameUtils.legacy(Objects.requireNonNull(config.getString("messages.civils-win"))));
            gameManager.setWinners(ERoles.CIVIL);
            gameManager.finishGame();
        } else if (undercoverLeft >= civilLeft) {
            gameManager.finishGame();
            Bukkit.broadcast(GameUtils.legacy(Objects.requireNonNull(config.getString("messages.undercover-win"))));
            gameManager.setWinners(ERoles.UNDERCOVER);
        } else if (whiteLeft >= gameManager.getPlayerManager().getAlivePlayers().size() - 1) {
            gameManager.setWinners(ERoles.MR_WHITE);
            gameManager.finishGame();
        } else {
            Bukkit.broadcast(GameUtils.legacy("§bLa partie continue, reprise dans 5 secondes..."));
            Bukkit.getScheduler().runTaskLater(Undercover.getInstance(), e -> {
                gameManager.getTurnManager().nextTurn();
            }, 5 * 20L);
        }
    }

    public Map<GamePlayer, ArmorStand> getVoteHolograms() {
        return voteHolograms;
    }

    public boolean isInVotingPhase() {
        return isInVotingPhase;
    }

    public void endVotingPhase() {
        isInVotingPhase = false;
        GamePlayer eliminatedPlayer = getEliminatedPlayer();

        GameUtils.smoothTimeTransition(Objects.requireNonNull(Bukkit.getWorld("world")), 12000);

        if (eliminatedPlayer != null) {
            Bukkit.broadcast(GameUtils.legacy(Objects.requireNonNull(config.getString("messages.player-eliminated"))
                .replace("%player_name%", eliminatedPlayer.getPlayer().getName())
                .replace("%votes%", String.valueOf(countVotes().getOrDefault(eliminatedPlayer, 0)))
                .replace("%role%", eliminatedPlayer.getRole().getColor() + eliminatedPlayer.getRole().getName())));

            Objects.requireNonNull(Bukkit.getWorld(eliminatedPlayer.getPlayer().getWorld().getName()))
                .strikeLightning(eliminatedPlayer.getPlayer().getLocation());

            if (eliminatedPlayer.getRole() == ERoles.MR_WHITE) {
                Player whitePlayer = eliminatedPlayer.getPlayer();
                Bukkit.broadcast(GameUtils.legacy("§bMr. White a été éliminé ! Il doit deviner le mot des civils dans les 20 secondes pour gagner."));
                for(Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_GHAST_HURT, 1f, 1f);
                }
                whitePlayer.teleport(GameSettings.getCurrentMap() == null ? new Location(Bukkit.getWorld("world"), 0.5, 69.06250, 0.5) : GameSettings.getCurrentMap().getCenter());
                whitePlayer.sendMessage(
                    "§cVous êtes éliminé ! Devinez le mot des civils dans les 20 secondes pour gagner. Écrivez-le dans le chat...");


                Listener[] whiteListener = new Listener[1]; // Trick pour unregister plus tard

                whiteListener[0] = new Listener() {
                    @EventHandler
                    public void onWhiteGuess(AsyncPlayerChatEvent event) {
                        if (!event.getPlayer().equals(whitePlayer)) {
                            return;
                        }

                        event.setCancelled(true); // Annule le message pour les autres
                        String guess = event.getMessage().trim().toLowerCase();
                        String civilianWord = gameManager.getWordManager().getCivilsWord().trim().toLowerCase();

                        if (guess.equalsIgnoreCase(civilianWord)) {
                            Bukkit.getScheduler().runTask(Undercover.getInstance(), () -> {
                                Bukkit.broadcast(
                                    GameUtils.legacy("§aMr. White a deviné le mot des civils ! Il gagne la partie !"));
                                gameManager.setWinners(ERoles.MR_WHITE);
                                gameManager.finishGame();
                            });
                        } else {
                            whitePlayer.sendMessage("§cCe n’est pas le bon mot.");
                            Bukkit.getScheduler().runTask(Undercover.getInstance(), () -> {
                                whitePlayer.sendMessage("§cVous avez échoué à deviner. Vous êtes éliminé.");
                                eliminatedPlayer.eliminate();
                                gameManager.getTurnManager().getTurnOrder().remove(eliminatedPlayer);
                                checkVictoryConditions();
                            });
                        }

                        // Unregister listener
                        AsyncPlayerChatEvent.getHandlerList().unregister(whiteListener[0]);
                    }
                };

                // Enregistrement du listener
                Bukkit.getPluginManager().registerEvents(whiteListener[0], Undercover.getInstance());

                // Timer de 20 secondes
                Bukkit.getScheduler().runTaskLater(Undercover.getInstance(), () -> {
                    AsyncPlayerChatEvent.getHandlerList().unregister(whiteListener[0]);
                    if (!gameManager.isGameFinished()) {
                        whitePlayer.sendMessage("§cTemps écoulé ! Vous n'avez pas deviné le mot.");
                        eliminatedPlayer.eliminate();
                        gameManager.getTurnManager().getTurnOrder().remove(eliminatedPlayer);
                        checkVictoryConditions();
                    }
                }, 20 * 20L); // 20 secondes

                return;
            } else {
                eliminatedPlayer.eliminate();
                gameManager.getTurnManager().getTurnOrder().remove(eliminatedPlayer);
            }
        }

        checkVictoryConditions();
    }

    public void startVotingPhase() {
        if (GameSettings.getMeetingTime() > 0) {
            Bukkit.broadcast(GameUtils.legacy("§6Phase de discussion lancée ! Vous avez " + (GameSettings.getMeetingTime() == 1 ? GameSettings.getMeetingTime() + " seconde" : GameSettings.getMeetingTime() + " secondes") + " pour débattre."));
            GameUtils.smoothTimeTransition(Objects.requireNonNull(Bukkit.getWorld("world")), 14000);
            int cooldownSeconds = GameSettings.getMeetingTime();
            int cooldownTicks = cooldownSeconds * 20;

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getInventory().clear();
                player.getInventory().setItem(0, new ItemBuilder(Material.DIAMOND).setName("§bPasser la discussion").toItemStack());
                player.setLevel(cooldownSeconds);
                player.setExp(0f);
                player.playSound(GameSettings.getCurrentMap().getCenter(), Sound.UI_BUTTON_CLICK, 0.75f, 1f);
                if(Undercover.getInstance().getStatsManager().getMusicStatus(player.getUniqueId())) {
                    GameUtils.playGameSound(player);
                }
            }

            BukkitRunnable runnable = new BukkitRunnable() {
                int ticksPassed = 0;

                @Override
                public void run() {
                    ticksPassed++;
                    float progress = 1f - ((float) ticksPassed / cooldownTicks);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.setLevel(Math.max(cooldownSeconds - (ticksPassed / 20), 0));
                        player.setExp(Math.max(progress, 0f));
                    }
                    if (ticksPassed >= cooldownTicks) {
                        this.cancel();
                    }
                }
            };

            discussionTimerTask = runnable.runTaskTimer(Undercover.getInstance(), 0L, 1L);


            endDiscussionTask = Bukkit.getScheduler().runTaskLater(Undercover.getInstance(), this::startVotePhase, cooldownTicks);

        } else {
            startVotePhase();
        }
    }

    private void startVotePhase() {
        resetVotes();
        isInVotingPhase = true;
        GameUtils.smoothTimeTransition(Objects.requireNonNull(Bukkit.getWorld("world")), 16000);
        Bukkit.broadcast(GameUtils.legacy(Objects.requireNonNull(config.getString("messages.voting-phase-started"))));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setLevel(0);
            player.setExp(0f);
            player.getInventory().clear();
        }
    }

    /**
     * Un joueur clique sur le bouton skip. Si tous les joueurs ont skip, on met fin à la phase de discussion.
     */
    public void playerSkipDiscussion(GamePlayer player) {
        if (isInVotingPhase) {
            player.getPlayer().sendMessage("§cLa phase de discussion est déjà terminée.");
            return;
        }

        if (!gameManager.getPlayerManager().getAlivePlayers().contains(player)) {
            player.getPlayer().sendMessage("§cSeuls les joueurs vivants peuvent passer.");
            return;
        }

        if (skipVotes.contains(player)) {
            player.getPlayer().sendMessage("§cVous avez déjà demandé à passer la discussion.");
            return;
        }

        skipVotes.add(player);
        player.getPlayer().getInventory().clear();
        Bukkit.broadcast(GameUtils.legacy("§e" + player.getPlayer().getName() + " souhaite passer la discussion. (" + skipVotes.size() + "/"
            + gameManager.getPlayerManager().getAlivePlayers().size() + ")"));

        if (skipVotes.size() == gameManager.getPlayerManager().getAlivePlayers().size()) {
            Bukkit.broadcast(GameUtils.legacy("§aTous les joueurs ont demandé à passer la discussion, on passe au vote !"));

            if (discussionTimerTask != null) discussionTimerTask.cancel();
            if (endDiscussionTask != null) endDiscussionTask.cancel();
            startVotePhase();
        }
    }



}
