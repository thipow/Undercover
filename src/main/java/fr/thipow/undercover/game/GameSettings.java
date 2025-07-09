package fr.thipow.undercover.game;

public class GameSettings {

    private static boolean showUndercoverNames = true;
    private static boolean whiteEnabled = false;
    private static int maxPlayer = 4;
    private static int undercoverCount = 1;
    private static int whiteCount = 0;

    public static int getWhiteCount() {
        return whiteCount;
    }

    public static void setWhiteCount(final int whiteCount) {
        GameSettings.whiteCount = whiteCount;
    }

    public static boolean isUndercoverNameShowing() {
        return showUndercoverNames;
    }

    public static void setShowUndercoverNames(final boolean showUndercoverNames) {
        GameSettings.showUndercoverNames = showUndercoverNames;
    }

    public static boolean isWhiteEnabled() {
        return whiteEnabled;
    }

    public static void setWhiteEnabled(final boolean whiteEnabled) {
        GameSettings.whiteEnabled = whiteEnabled;
    }

    public static int getMaxPlayer() {
        return maxPlayer;
    }

    public static void setMaxPlayer(final int maxPlayer) {
        GameSettings.maxPlayer = maxPlayer;
    }

    public static int getUndercoverCount() {
        return undercoverCount;
    }

    public static void setUndercoverCount(final int undercoverCount) {
        GameSettings.undercoverCount = undercoverCount;
    }
}
