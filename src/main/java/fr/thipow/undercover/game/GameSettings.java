package fr.thipow.undercover.game;

public class GameSettings {

    private static boolean undercoverChat = true;
    private static boolean whiteEnabled = true;
    private static int maxPlayer = 12;
    private static int undercoverCount = 2;
    private static int whiteCount = 1;

    public static int getWhiteCount() {
        return whiteCount;
    }

    public static void setWhiteCount(final int whiteCount) {
        GameSettings.whiteCount = whiteCount;
    }

    public static boolean isUndercoverChat() {
        return undercoverChat;
    }

    public static void setUndercoverChat(final boolean undercoverChat) {
        GameSettings.undercoverChat = undercoverChat;
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
