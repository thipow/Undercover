package fr.thipow.undercover.game;

/**
 * Represents the different roles available in the Undercover game.
 * Each role has a display name and an associated color code.
 * @author Thipow
 */
public enum ERoles {

    UNDERCOVER("Undercover", "§c"),
    CIVIL("Civil", "§b"),
    MR_WHITE("Mr. White", "§f");

    private final String name;
    private final String color;

    /**
     * Constructs a new role with its display name and color code.
     *
     * @param name  The display name of the role.
     * @param color The color code associated with the role.
     */
    ERoles(String name, String color) {
        this.name = name;
        this.color = color;
    }

    /**
     * Gets the display name of the role.
     *
     * @return The display name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the color code associated with the role.
     *
     * @return The color code (e.g., §c, §b).
     */
    public String getColor() {
        return color;
    }

    /**
     * Returns the role name with its color code.
     *
     * @return The colored name (e.g., §cUndercover).
     */
    public String getColoredName() {
        return color + name;
    }

    /**
     * Returns the colored name as the string representation.
     *
     * @return Colored name.
     */
    @Override
    public String toString() {
        return getColoredName();
    }
}
