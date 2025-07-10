package fr.thipow.undercover.game;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.managers.ScoreboardManager;
import fr.thipow.undercover.utils.GameUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Game loop task updating game time, scoreboards and sending role words to players.
 * @author Thipow
 */
public class GameTask extends BukkitRunnable {

    private final Undercover main;
    private Long lastTargetTime = null;

    public GameTask(Undercover main) {
        this.main = main;
    }

    @Override
    public void run() {
        World world = Bukkit.getWorld("world");
        if (world == null) return;

        GameManager gameManager = main.getGameManager();
        EStates currentState = gameManager.getGameState();

        long targetTime = switch (currentState) {
            case WAITING, ENDING -> 4000;
            case PLAYING -> {
                updateActionBars(gameManager);
                yield GameManager.isInVotingPhase() ? 16000 : 12000;
            }
        };

        if (lastTargetTime == null || !lastTargetTime.equals(targetTime)) {
            smoothTimeTransition(world, targetTime);
            lastTargetTime = targetTime;
        }

        Bukkit.getOnlinePlayers().forEach(ScoreboardManager::updateScoreboard);
    }

    private void updateActionBars(GameManager gameManager) {
        for (Player player : gameManager.getGamePlayers()) {
            ERoles role = gameManager.getPlayerRole(player);
            String[] words = gameManager.getWords();
            String word = switch (role) {
                case UNDERCOVER -> words[0];
                case CIVIL -> words[1];
                case MR_WHITE -> "Devine le mot !";
            };

            player.sendActionBar(GameUtils.legacy("§bMot : §f" + capitalize(word)));
        }
    }

    private String capitalize(String word) {
        return (word == null || word.isEmpty()) ? word :
            word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private void smoothTimeTransition(World world, long targetTime) {
        long startTime = world.getTime();
        long delta = (targetTime - startTime + 24000) % 24000;

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;
                float progress = ticks / (float) 20;
                long newTime = (startTime + (long) (delta * progress)) % 24000;
                world.setTime(newTime);

                if (ticks >= 20) {
                    cancel();
                    world.setTime(targetTime);
                }
            }
        }.runTaskTimer(main, 0L, 1L);
    }
}
