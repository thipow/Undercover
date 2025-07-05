package fr.thipow.undercover.game;

/**
 * Enum representing the different roles in the game. Each role has a name and a color associated with it.
 */

public enum ERoles {

    UNDERCOVER("Undercover", "§c"), CIVIL("Civil", "§b"), WHITE("Mr.White", "§f");

    private final String name;
    private final String color;

    /**
     * Constructor for the ERoles enum.
     *
     * @param name  The name of the role.
     * @param color The color associated with the role.
     */
    ERoles(final String name, final String color) {
        this.name = name;
        this.color = color;
    }

    /**
     * Gets the name of the role.
     *
     * @return The name of the role.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the color associated with the role.
     *
     * @return The color of the role.
     */
    public String getColor() {
        return color;
    }
}
