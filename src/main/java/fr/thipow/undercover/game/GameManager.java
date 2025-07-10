package fr.thipow.undercover.game;


import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.managers.ScoreboardManager;
import fr.thipow.undercover.utils.ItemBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GameManager {

    private static final FileConfiguration       config             = Undercover.getInstance().getConfig();
    private static final Map<Player, ArmorStand> voteHolograms      = new HashMap<>();
    public static        int                     currentPlayerIndex = 0;
    public static        ERoles                  winners            = ERoles.CIVIL;
    private static       ArrayList<Player>       gamePlayers        = new ArrayList<>();
    private static       boolean                 inVotingPhase      = false;
    private static       Map<Player, Player>     votes              = new HashMap<>();
    private static       Map<Player, ERoles>     playerRoles        = new HashMap<>();
    private static       int                     speakingBarTaskId  = -1;
    private              EStates                 gameState          = EStates.WAITING;
    private        Undercover main;
    private static String[]   words = generateWords();


    public GameManager(Undercover main) {
        this.main = main;
    }

    public static void resetGame(){
        GameSettings.setShowUndercoverNames(config.getBoolean("default-setting.showUndercoverNames"));
        GameSettings.setWhiteEnabled(config.getBoolean("default-setting.whiteEnabled"));
        GameSettings.setMaxPlayers(config.getInt("default-settings.slots"));
        GameSettings.setUndercoverCount(config.getInt("default-settings.undercoverCount"));
        GameSettings.setWhiteCount(config.getInt("default-settings.whiteCount"));
        GameSettings.setPrivateVote(false);
        GameSettings.setShowRole(false);
        currentPlayerIndex = 0;
        inVotingPhase = false;
        gamePlayers.clear();
        votes.clear();
        playerRoles.clear();
        speakingBarTaskId = -1;
        for (ArmorStand hologram : voteHolograms.values()) {
            if (hologram != null && !hologram.isDead()) {
                hologram.remove();
            }
        }
        voteHolograms.clear();
        Undercover.getInstance().getGameManager().setGameState(EStates.WAITING);
        words = generateWords();
        for(Player player : Bukkit.getOnlinePlayers()){
            gamePlayers.add(player);
            player.stopAllSounds();

            player.playSound(new Location(Bukkit.getWorld("world"), 0.5, 69, 0.5), Sound.MUSIC_DISC_OTHERSIDE, 0.15f, 1);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            player.setLevel(0);
            player.setExp(0f);
            player.setAllowFlight(false);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();

            if (player.isOp()) {
                player.getInventory().setItem(4,
                    new ItemBuilder(Material.NETHER_STAR).setName(config.getString("messages.config-item-name"))
                        .toItemStack());
            }
        }
    }

    public static void startRound() {
        currentPlayerIndex = 0;
        inVotingPhase = false;
        nextTurn();
    }

    public static boolean isInVotingPhase() {
        return inVotingPhase;
    }

    public static void nextTurn() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.stopAllSounds();
        }
        if (speakingBarTaskId != -1) {
            Bukkit.getScheduler().cancelTask(speakingBarTaskId);
            speakingBarTaskId = -1;
        }

        if (currentPlayerIndex >= gamePlayers.size()) {
            startVotingPhase();
            return;
        }

        Player current = gamePlayers.get(currentPlayerIndex);
        Bukkit.broadcastMessage(Objects.requireNonNull(config.getString("messages.turn-announce"))
            .replace("%player_name%", current.getName()));
        giveSkipItem(current);

        int totalTime = 20;

        speakingBarTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Undercover.getInstance(), new Runnable() {
            int timeLeft = totalTime;

            @Override
            public void run() {
                if (timeLeft <= 0 || getCurrentPlayer() != current) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.setLevel(0);
                        player.setExp(0f);
                    }
                    Bukkit.getScheduler().cancelTask(speakingBarTaskId);
                    speakingBarTaskId = -1;
                    return;
                }

                float xpProgress = timeLeft / (float) totalTime;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.setLevel(timeLeft);
                    player.setExp(xpProgress);
                }

                timeLeft--;
            }
        }, 0L, 20L);

        Bukkit.getScheduler().runTaskLater(Undercover.getInstance(), () -> {
            if (getCurrentPlayer() == current) {
                Bukkit.broadcastMessage(Objects.requireNonNull(config.getString("messages.speaking-time-exceeded"))
                    .replace("%player_name%", current.getName()));
                current.getInventory().clear();
                currentPlayerIndex++;
                nextTurn();
            }
        }, totalTime * 20L);
    }


    private static void updateVoteHolograms() {
        for (Player target : gamePlayers) {
            int voteCount = (int) votes.values().stream().filter(p -> p.equals(target)).count();

            ArmorStand hologram = voteHolograms.get(target);

            if (voteCount == 0) {
                if (hologram != null && !hologram.isDead()) {
                    hologram.remove();
                    voteHolograms.remove(target);
                }
                continue;
            }

            if (hologram == null || hologram.isDead()) {
                hologram = (ArmorStand) target.getWorld()
                    .spawnEntity(target.getLocation().add(0, 2.5, 0), EntityType.ARMOR_STAND);
                hologram.setInvisible(true);
                hologram.setCustomNameVisible(true);
                hologram.setMarker(true);
                hologram.setGravity(false);
                voteHolograms.put(target, hologram);
            }

            hologram.teleport(target.getLocation().add(0, 2.25, 0));
            hologram.setCustomName("§bVotes: §f" + voteCount);
        }
    }


    public static void teleportPlayers() {
        List<Location> spawns = new ArrayList<>();
        spawns.add(new Location(Bukkit.getWorld("world"), 10.5, 69.5, 0.5f, 90, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), 10.5, 69.5, -2.5f, 75, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), 9.5, 69.5, -6f, 55, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), 6.5, 69.5, -8.5f, 35, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), 3.5, 69.5, -9.5f, 15, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), 0.5, 69.5, -9.5f, 0, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), -2.5f, 69.5, -9.5f, -15, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), -5.5f, 69.5, -8.5f, -35, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), -8.5f, 69.5, -5.5f, -55, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), -9.5f, 69.5, -2.5f, -70, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), -9.5f, 69.5, 0.5f, -90, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), -9.5f, 69.5, 3.5f, -105, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), -8.5f, 69.5, 6.5f, -125, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), -5.5f, 69.5, 9.5f, -145, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), -2.5f, 69.5, 10.5f, -165, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), 0.5f, 69.5, 10.5f, 180, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), 3.5f, 69.5, 10.5f, 165, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), 6.5f, 69.5, 9.5f, 145, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), 9.5f, 69.5, 6.5f, 125, 0));
        spawns.add(new Location(Bukkit.getWorld("world"), 10.5f, 69.5, 3.5f, 105, 0));

        int numberOfPlayers = gamePlayers.size();
        int totalSpawns = spawns.size();

        if (numberOfPlayers > totalSpawns) {
            Bukkit.broadcastMessage("§cErreur : Pas assez de positions disponibles pour tous les joueurs !");
            return;
        }

        for (int i = 0; i < numberOfPlayers; i++) {
            int index = (i * totalSpawns) / numberOfPlayers;
            Player player = gamePlayers.get(i);
            Location spawn = spawns.get(index);
            player.teleport(spawn);
        }
    }


    public static void startVotingPhase() {
        inVotingPhase = true;
        Bukkit.broadcastMessage(config.getString("messages.voting-phase-started"));
        votes.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setLevel(0);
            player.setExp(0f);
            player.playSound(new Location(Bukkit.getWorld("world"), 0.5, 69, 0.5), Sound.MUSIC_DISC_CREATOR, 0.15f, 1f);
        }
    }

    public static void vote(Player voter, Player target) {
        if (!Undercover.getInstance().getGameManager().isGameState(EStates.PLAYING)) {
            return;
        }
        if (!inVotingPhase) {
            return;
        }

        votes.put(voter, target);
        voter.sendMessage(Objects.requireNonNull(config.getString("messages.voting-for"))
            .replace("%target_player%", target.getName()));
        if (!GameSettings.isPrivateVote()) {
            Bukkit.broadcastMessage("§b" + voter.getName() + " §fa voté pour §b" + target.getName() + "§f!");
            updateVoteHolograms();
        }

        if (votes.size() >= gamePlayers.size()) {
            endVotingPhase();
        }
    }

    public static void endVotingPhase() {
        Map<Player, Integer> voteCount = new HashMap<>();
        for (Player voted : votes.values()) {
            voteCount.put(voted, voteCount.getOrDefault(voted, 0) + 1);
        }

        Player eliminatedPlayer = null;
        int maxVotes = 0;
        for (Map.Entry<Player, Integer> entry : voteCount.entrySet()) {
            if (entry.getValue() > maxVotes) {
                eliminatedPlayer = entry.getKey();
                maxVotes = entry.getValue();
            }
        }
        if (eliminatedPlayer != null) {
            Bukkit.broadcastMessage(Objects.requireNonNull(config.getString("messages.player-eliminated"))
                .replace("%player_name%", eliminatedPlayer.getName()).replace("%votes%", String.valueOf(maxVotes))
                .replace("%role%",
                    playerRoles.get(eliminatedPlayer).getColor() + playerRoles.get(eliminatedPlayer).getName()));
            Objects.requireNonNull(Bukkit.getWorld(eliminatedPlayer.getWorld().getName()))
                .strikeLightning(eliminatedPlayer.getLocation());
            gamePlayers.remove(eliminatedPlayer);
            eliminatedPlayer.getInventory().clear();
            eliminatedPlayer.setAllowFlight(true);
            eliminatedPlayer.setFlying(true);
            eliminatedPlayer.teleport(new Location(Bukkit.getWorld("world"), 0.5, 75, 0.5, -90, 0));
            eliminatedPlayer.addPotionEffect(
                new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 255, false, false));

            for (ArmorStand hologram : voteHolograms.values()) {
                if (hologram != null && !hologram.isDead()) {
                    hologram.remove();
                }
            }
            voteHolograms.clear();
        }
        checkVictoryConditions();
    }

    public static void checkVictoryConditions() {
        long undercoverLeft = gamePlayers.stream().filter(p -> playerRoles.get(p) == ERoles.UNDERCOVER).count();
        long civilLeft = gamePlayers.stream().filter(p -> playerRoles.get(p) == ERoles.CIVIL).count();
        long whiteLeft = gamePlayers.stream().filter(p -> playerRoles.get(p) == ERoles.MR_WHITE).count();

        if (undercoverLeft == 0) {
            Bukkit.broadcastMessage(Objects.requireNonNull(config.getString("messages.civils-win")));
            winners = ERoles.CIVIL;
            finishGame();
        } else if (undercoverLeft >= civilLeft) {
            finishGame();
            Bukkit.broadcastMessage(Objects.requireNonNull(config.getString("messages.undercover-win")));
            winners = ERoles.UNDERCOVER;
        } else {
            winners = ERoles.MR_WHITE;
            Bukkit.broadcastMessage("§bLa partie continue, reprise dans 5 secondes...");
            Bukkit.getScheduler().runTaskLater(Undercover.getInstance(), () -> {
                startRound();
            }, 5 * 20L);

        }
    }


    public static void finishGame() {
        Undercover.getInstance().getGameManager().setGameState(EStates.ENDING);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.isOp()){
                player.getInventory().clear();
                player.getInventory().setItem(0, new ItemBuilder(Material.LEAD).setName("§bNouvelle partie").toItemStack());
            }
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            player.setAllowFlight(true);

            if (playerRoles.get(player) == ERoles.UNDERCOVER && winners == ERoles.UNDERCOVER) {
                player.sendTitle("§a§lVICTOIRE", "§fVous avez gagné en tant qu'§cUndercover !", 20, 100, 20);
            } else if (playerRoles.get(player) == ERoles.CIVIL && winners == ERoles.CIVIL) {
                player.sendTitle("§a§lVICTOIRE", "§fVous avez gagné en tant que §bCivil !", 20, 100, 20);
            } else if (playerRoles.get(player) == ERoles.MR_WHITE && winners == ERoles.MR_WHITE) {
                player.sendTitle("§a§lVICTOIRE", "§fVous avez gagné en tant que §fWhite !", 20, 100, 20);
            } else {
                player.sendTitle("§c§lDÉFAITE", "§fVous avez perdu !", 20, 100, 20);
            }

            player.sendMessage("\n"
                + "§fVous etiez " + playerRoles.get(player).getColor() + playerRoles.get(player).getName() + "§f !\n"
                + " \n"
                + "§fLe mot des §cUndercover §fétait : §3" + Undercover.getInstance().getGameManager().getWords()[0] + "§f !\n"
                + "§fLe mot des §bCivils §fétait : §3" + Undercover.getInstance().getGameManager().getWords()[1] + "§f !\n");
        }
    }

    public static void giveSkipItem(Player player) {
        player.getInventory().setItem(0,
            new ItemBuilder(Material.FEATHER).setName(config.getString("messages.skip-item.name")).toItemStack());
    }

    public static Player getCurrentPlayer() {
        if (gamePlayers.isEmpty() || currentPlayerIndex >= gamePlayers.size()) {
            return null;
        }
        return gamePlayers.get(currentPlayerIndex);
    }

    public String[] getWords() {
        return words;
    }

    private static String[] generateWords() {
        EWords[] allPairs = EWords.values();
        EWords pair = allPairs[(int) (Math.random() * allPairs.length)];
        return new String[]{pair.getWord1(), pair.getWord2()};
    }

    public void initGame() {
        gameState = EStates.WAITING;
        gamePlayers.clear();
        playerRoles.clear();
    }

    public void distributeRoles() {
        Collections.shuffle(gamePlayers);
        Collections.shuffle(gamePlayers);
        Collections.shuffle(gamePlayers);
        Collections.shuffle(gamePlayers);
        Collections.shuffle(gamePlayers);
        Collections.shuffle(gamePlayers);
        Collections.shuffle(gamePlayers);
        for (Player player : gamePlayers) {
            if (playerRoles.get(player) == null) {
                if (GameSettings.getUndercoverCount() > 0) {
                    playerRoles.put(player, ERoles.UNDERCOVER);
                    GameSettings.setUndercoverCount(GameSettings.getUndercoverCount() - 1);
                } else if (GameSettings.isWhiteEnabled() && GameSettings.getWhiteCount() > 0) {
                    playerRoles.put(player, ERoles.MR_WHITE);
                    GameSettings.setWhiteCount(GameSettings.getWhiteCount() - 1);
                } else {
                    playerRoles.put(player, ERoles.CIVIL);
                }
            }
        }
    }

    public void startGame() {
        distributeRoles();
        teleportPlayers();
        for (Player player : gamePlayers) {
            player.getInventory().clear();
        }
        setGameState(EStates.PLAYING);
        for (Player player : gamePlayers) {
            ERoles role = playerRoles.get(player);

            String undercoverWord = capitalize(words[0]);
            String civilWord = capitalize(words[1]);

            switch (role) {
                case UNDERCOVER:

                    if (GameSettings.isShowRole()) {
                        player.sendMessage(
                            "Tu est " + role.getColor() + role.getName() + " !\n" + "Ton mot est : §3" + undercoverWord
                                + " !");
                        player.playSound(player.getLocation(), Sound.ENTITY_WOLF_GROWL, 1, 1);
                        player.sendTitle(role.getColor() + role.getName(), undercoverWord, 20, 100, 20);
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        player.sendMessage("Ton mot est : §3" + undercoverWord + " !");
                        player.sendTitle("Ton mot est", "§b" + undercoverWord, 20, 100, 20);
                    }

                    break;
                case MR_WHITE:
                    player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_3, 1, 1);
                    player.sendTitle(role.getColor() + role.getName(), "§fVous n'avez pas de mot !", 20, 100, 20);
                    player.sendMessage("Tu est " + role.getColor() + role.getName() + " !\n"
                        + "§3Vous n'avez pas de mot, vous devez deviner celui des Undercover !");
                    break;
                case CIVIL:
                    if (GameSettings.isShowRole()) {
                        player.sendMessage(
                            "Tu est " + role.getColor() + role.getName() + " !\n" + "Ton mot est : §3" + civilWord
                                + " !");
                        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
                        player.sendTitle(role.getColor() + role.getName(), civilWord, 20, 100, 20);
                    } else {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        player.sendMessage("Ton mot est : §3" + civilWord + " !");
                        player.sendTitle("Ton mot est", "§b" + civilWord, 20, 100, 20);
                    }
            }
        }

        if (GameSettings.isShowUndercoverNames()) {
            for (Player player : gamePlayers) {
                List<Player> undercoverPlayers = gamePlayers.stream()
                    .filter(p -> playerRoles.get(p) == ERoles.UNDERCOVER).toList();
                if (Undercover.getInstance().getGameManager().getPlayerRole(player) == ERoles.UNDERCOVER) {
                    player.sendMessage("§cVoici la liste de vos associés Undercover :");
                    for (Player undercover : undercoverPlayers) {
                        if (!undercover.equals(player)) {
                            player.sendMessage("§f- §3" + undercover.getName());
                        }
                    }
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(main, () -> {
            startRound();
        }, 20L);
    }

    private String capitalize(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    public ArrayList<Player> getGamePlayers() {
        return gamePlayers;
    }

    public Map<Player, ERoles> getPlayerRoles() {
        return playerRoles;
    }

    public ERoles getPlayerRole(Player player) {
        return playerRoles.getOrDefault(player, ERoles.CIVIL);
    }

    public EStates getGameState() {
        return gameState;
    }

    public void setGameState(final EStates gameState) {
        this.gameState = gameState;
    }

    public boolean isGameState(final EStates state) {
        return gameState == state;
    }
}
