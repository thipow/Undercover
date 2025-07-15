package fr.thipow.undercover.commands;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.EStates;
import fr.thipow.undercover.game.MapsManager;
import fr.thipow.undercover.game.maps.GameMap;
import fr.thipow.undercover.utils.GameUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class UndercoverCommand implements CommandExecutor, TabCompleter {

    private final Undercover main = Undercover.getInstance();
    private final MapsManager mapsManager = main.getMapsManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSeul un joueur peut exécuter cette commande.");
            return true;
        }

        if (args[0].equalsIgnoreCase("music")) {
            if(main.getStatsManager().getMusicStatus(player.getUniqueId())){
                player.sendMessage("§aMusique désactivée.");
                player.stopAllSounds();
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            } else {
                player.sendMessage("§aMusique activée.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                if(!Undercover.getInstance().getGameManager().getGameState().equals(EStates.PLAYING)){
                    GameUtils.playWaitingSound(player);
                }else{
                    if(Undercover.getInstance().getGameManager().getVotingManager().isInVotingPhase()){
                        GameUtils.playGameSound(player);
                    }
                }
            }
            Undercover.getInstance().getStatsManager().toogleMusic(player.getUniqueId());
            return true;
        }


        if (!sender.hasPermission("uc.admin") && (args[0].equalsIgnoreCase("map") || args[0].equalsIgnoreCase("start"))) {
            sender.sendMessage("§cVous n'avez pas la permission d'exécuter cette commande.");
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            main.getGameManager().startGame();
            return true;
        }

        if (!args[0].equalsIgnoreCase("map")) return false;

        if (args.length == 1 && args[0].equalsIgnoreCase("map")) {
            sender.sendMessage("§cUtilisation : /uc map <create|delete|setspawn|setcenter|addspawn|setdisplayname|setitem>");
            return true;
        }

        String action = args[1];

        switch (action.toLowerCase()) {
            case "create" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /uc map create <nom>");
                    return true;
                }
                if(mapsManager.getAllMaps().size() >= 14){
                    sender.sendMessage("§cVous avez atteint le nombre maximum de maps (14).");
                    return true;
                }
                String mapName = args[2];
                if (mapsManager.getMap(mapName) == null) {
                    mapsManager.createMap(mapName);
                    sender.sendMessage("§aMap créée: " + mapName);
                }else {
                    sender.sendMessage("§cUne map avec ce nom existe déjà.");
                }
                return true;

            }

            case "delete" -> {
                if (args.length < 3) return usage(sender, "delete <map>");
                if (mapsManager.deleteMap(args[2])) {
                    sender.sendMessage("§aMap supprimée.");
                } else {
                    sender.sendMessage("§cMap introuvable.");
                }
            }

            case "setspawn" -> {
                if (args.length < 3) return usage(sender, "setspawn <map>");
                GameMap map = mapsManager.getMap(args[2]);
                if (map == null) return mapNotFound(sender);
                mapsManager.setSpawn(map.getName(), player.getLocation());
                sender.sendMessage("§aSpawn principal défini.");
            }

            case "resetspawns" -> {
                if (args.length < 3) return usage(sender, "resetspawns <map>");
                GameMap map = mapsManager.getMap(args[2]);
                if (map == null) return mapNotFound(sender);
                mapsManager.resetSpawns(map.getName());
                sender.sendMessage("§aTous les spawns ont été supprimés.");
            }

            case "setcenter" -> {
                if (args.length < 3) return usage(sender, "setcenter <map>");
                GameMap map = mapsManager.getMap(args[2]);
                if (map == null) return mapNotFound(sender);
                mapsManager.setCenter(map.getName(), player.getLocation());
                sender.sendMessage("§aCentre défini.");
            }

            case "addspawn" -> {
                if (args.length < 3) return usage(sender, "addspawn <map>");
                GameMap map = mapsManager.getMap(args[2]);
                if (map == null) return mapNotFound(sender);
                mapsManager.addSpawn(map.getName(), player.getLocation());
                sender.sendMessage("§aSpawn ajouté.");
            }

            case "setdisplayname" -> {
                if (args.length < 4) return usage(sender, "setdisplayname <map> <nom>");
                GameMap map = mapsManager.getMap(args[2]);
                if (map == null) return mapNotFound(sender);
                String name = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                mapsManager.setDisplayName(map.getName(), name);
                sender.sendMessage("§aNom d’affichage mis à jour.");
            }

            case "setitem" -> {
                if (args.length < 3) return usage(sender, "setitem <map>");
                GameMap map = mapsManager.getMap(args[2]);
                if (map == null) return mapNotFound(sender);
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) {
                    sender.sendMessage("§cTu dois tenir un item en main !");
                    return true;
                }
                mapsManager.setItem(map.getName(), item.getType());
                sender.sendMessage("§aItem défini : " + item.getType().name());
            }

            case "list" -> {
                if (mapsManager.getAllMaps().isEmpty()) {
                    sender.sendMessage("§cAucune map enregistrée.");
                    return true;
                }
                sender.sendMessage("§aMaps disponibles :");
                mapsManager.getAllMaps().forEach(map -> {
                    sender.sendMessage(" - §e" + map.getDisplayName() + "§f (" + map.getName() + ") - Item: " + map.getItem().name());
                });
            }

            default -> sender.sendMessage("§cAction inconnue.");
        }

        return true;
    }

    private boolean usage(CommandSender sender, String usage) {
        sender.sendMessage("§cUsage: /uc map " + usage);
        return true;
    }

    private boolean mapNotFound(CommandSender sender) {
        sender.sendMessage("§cMap introuvable.");
        return true;
    }

    // ——————————————— TAB COMPLETION ———————————————

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("uc.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return sender.hasPermission("uc.admin") ? filter(List.of("map", "start", "music"), args[0]) : filter(List.of("music"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("map")) {
            return filter(List.of("create", "delete", "setspawn", "setcenter", "addspawn", "setdisplayname", "setitem", "list", "resetspawns"), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("map")) {
            return filter(mapsManager.getMapNames(), args[2]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> input, String start) {
        return input.stream()
            .filter(s -> s.toLowerCase().startsWith(start.toLowerCase()))
            .toList();
    }
}
