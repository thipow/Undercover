package fr.thipow.undercover.game;

/**
 * Represents the different roles available in the Undercover game. Each role has a display name and an associated color
 * code.
 *
 * @author Thipow
 */
public enum ERoles {

    UNDERCOVER("Undercover", "§c", "undercover"), CIVIL("Civil", "§b", "civil"), MR_WHITE("Mr. White", "§f", "white");

    private final String name;
    private final String color;
    private final String identifier;

    /**
     * Constructs a new role with its display name and color code.
     *
     * @param name  The display name of the role.
     * @param color The color code associated with the role.
     */
    ERoles(String name, String color, String identifier) {
        this.name = name;
        this.color = color;
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
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
