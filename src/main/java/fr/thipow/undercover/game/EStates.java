package fr.thipow.undercover.game;

/**
 * Represents the different states of the Undercover game.
 * Each state has a display name used in messages and interfaces.
 * @author Thipow
 */
public enum EStates {

    WAITING("En attente..."),
    PLAYING("Partie en cours"),
    ENDING("Fin du jeu");

    private final String displayName;

    /**
     * Constructs a new game state with the given display name.
     *
     * @param displayName The name displayed for this state.
     */
    EStates(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the display name of the game state.
     *
     * @return Display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the display name as the string representation.
     *
     * @return Display name.
     */
    @Override
    public String toString() {
        return displayName;
    }
}
