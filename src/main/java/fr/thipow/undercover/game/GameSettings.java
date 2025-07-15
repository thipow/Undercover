package fr.thipow.undercover.game;

import fr.thipow.undercover.Undercover;
import fr.thipow.undercover.game.maps.GameMap;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Centralizes all configurable game settings.
 *
 * @author Thipow
 */
public class GameSettings {

    private static final FileConfiguration CONFIG = Undercover.getInstance().getConfig();
    private static final String            PREFIX = CONFIG.getString("messages.prefix", "§7[Undercover]");

    // Default configuration values
    private static boolean showUndercoverNames;
    private static boolean whiteEnabled;
    private static int     maxPlayers;
    private static int     undercoverCount;
    private static int     meetingTime;
    private static GameMap currentMap;

    // Runtime-modifiable settings
    private static boolean privateVote;
    private static boolean showRole;

    static {
        loadDefaults();
    }

    /**
     * Loads default values from config.yml.
     */
    public static void loadDefaults() {
        currentMap = Undercover.getInstance().getMapsManager().getFirstMap();
        showUndercoverNames = CONFIG.getBoolean("default-setting.showUndercoverNames", false);
        whiteEnabled = CONFIG.getBoolean("default-setting.whiteEnabled", true);
        maxPlayers = CONFIG.getInt("default-settings.slots", 12);
        undercoverCount = CONFIG.getInt("default-settings.undercoverCount", 2);
        meetingTime = CONFIG.getInt("default-settings.meeting-time", 20);
        privateVote = CONFIG.getBoolean("default-settings.private-votes", false);
        showRole = CONFIG.getBoolean("default-settings.show-roles", false);
    }

    public static GameMap getCurrentMap() {
        return currentMap;
    }

    public static void setCurrentMap(final GameMap currentMap) {
        GameSettings.currentMap = currentMap;
        int mapSpawnCount = Undercover.getInstance().getMapsManager().getMap(currentMap.getName()).getSpawns().size();
        if(mapSpawnCount < maxPlayers) {
            setMaxPlayers(mapSpawnCount);
        }
    }

    public static String getPrefix() {
        return PREFIX;
    }

    public static boolean isShowUndercoverNames() {
        return showUndercoverNames;
    }

    public static void setShowUndercoverNames(boolean value) {
        showUndercoverNames = value;
    }

    public static boolean isWhiteEnabled() {
        return whiteEnabled;
    }

    public static void setWhiteEnabled(boolean value) {
        whiteEnabled = value;
    }

    public static int getMaxPlayers() {
        return maxPlayers;
    }

    public static void setMaxPlayers(int value) {
        maxPlayers = value;
    }

    public static int getUndercoverCount() {
        return undercoverCount;
    }

    public static void setUndercoverCount(int value) {
        undercoverCount = value;
    }

    public static boolean isPrivateVote() {
        return privateVote;
    }

    public static void setPrivateVote(boolean value) {
        privateVote = value;
    }

    public static boolean isShowRole() {
        return showRole;
    }

    public static void setShowRole(boolean value) {
        showRole = value;
    }

    public static int getMeetingTime() {
        return meetingTime;
    }

    public static void setMeetingTime(final int meetingTime) {
        GameSettings.meetingTime = meetingTime;
    }
}
