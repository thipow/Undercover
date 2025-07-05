package fr.thipow.undercover.game;

/**
 * Enum representing the different states of the game. Each state has a name that can be retrieved.
 */

public enum EStates {

    WAITING("En attente..."), PLAYING("Partie en cours"), ENDING("Fin du jeu");

    private final String stateName;

    /**
     * Constructor for the EStates enum.
     *
     * @param stateName The name of the state.
     */
    EStates(String stateName) {
        this.stateName = stateName;
    }

    /**
     * Gets the name of the state.
     *
     * @return The name of the state.
     */
    public String getStateName() {
        return stateName;
    }
}
