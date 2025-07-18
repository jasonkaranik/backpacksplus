package com.jasonkaranik.backpacksplus.enums;

import com.jasonkaranik.backpacksplus.Config;
import org.bukkit.Material;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the available colors for backpacks in the game.
 * <p>
 * Each color corresponds to a standard Minecraft dye color and is associated with
 * a unique texture ID that determines the backpack's appearance in-game. Players
 * can apply these colors using matching dye items from the game.
 * <p>
 * Example usage:
 * <pre>
 * BackpackColor color = BackpackColor.getByDye(Material.BROWN_DYE);
 * String textureId = color.getTextureID();
 * </pre>
 *
 * @see org.bukkit.Material
 * @since 1.0.0
 */
public enum BackpackColor {
    /**
     * Brown backpack texture.
     *
     * @since 1.0.0
     */
    BROWN("Brown", "2308bf5cc3e9decaf0770c3fdad1e042121cf39cc2505bbb866e18c6d23ccd0c", "#835432"),
    /**
     * Black backpack texture.
     *
     * @since 1.0.0
     */
    BLACK("Black", "a82641786a422088f75dcee70205d580600f69d6aa2f77d2678b58d89b6973a6", "#1D1D21"),
    /**
     * Blue backpack texture.
     *
     * @since 1.0.0
     */
    BLUE("Blue", "8dcc6eb40f3bada41e4339888d6d207437598bdbd175c2e731191d5a9a42d3c8", "#3C44AA"),
    /**
     * Cyan backpack texture.
     *
     * @since 1.0.0
     */
    CYAN("Cyan", "3e5abdb7374553d0565cbcb3295aed515a897ebce9e0bc60f1c1f8ae54c749df", "#169C9C"),
    /**
     * Green backpack texture.
     *
     * @since 1.0.0
     */
    GREEN("Green", "ca6ad8ad913def13bd5741655e77d134eb1b57f02970daa6b33082754d1affc4", "#5E7C16"),
    /**
     * Gray backpack texture.
     *
     * @since 1.0.0
     */
    GRAY("Gray", "bac77b76f0c64c14c04936a55796c49a82ff174838db930d52b0caacdd215917", "#474F52"),
    /**
     * Light Blue backpack texture.
     *
     * @since 1.0.0
     */
    LIGHT_BLUE("Light blue", "82c37bfa11863d002551a02b519d7154da826705d20982591522de2ea1b288c2", "#3AB3DA"),
    /**
     * Light Gray backpack texture.
     *
     * @since 1.0.0
     */
    LIGHT_GRAY("Light gray", "33c0bfa875ab8b83d8d9519774c6c7c45ad9a8843f68a4a5c0003743260e2ec5", "#9D9D97"),
    /**
     * Lime backpack texture.
     *
     * @since 1.0.0
     */
    LIME("Lime", "b2128f48d997186563fbc5b47a88c0d0aac92fa2c285cd1fae420c34fa8f2010", "#80C71F"),
    /**
     * Magenta backpack texture.
     *
     * @since 1.0.0
     */
    MAGENTA("Magenta", "92800349d126b9ffca7a2be659d796ac0e15df088efa2668d6d3dd638e0f9754", "#C74EBD"),
    /**
     * Orange backpack texture.
     *
     * @since 1.0.0
     */
    ORANGE("Orange", "270b5622b7d068f5788bf19a83983937b16c5972c91f5ee7bf54bcc3638f9a36", "#F9801D"),
    /**
     * Pink backpack texture.
     *
     * @since 1.0.0
     */
    PINK("Pink", "2e087cd975d264c72adca9efc6fc34476ec38e18df536b307635267a37faf076", "#F38BAA"),
    /**
     * Purple backpack texture.
     *
     * @since 1.0.0
     */
    PURPLE("Purple", "6187c732aa39d29134650b6a72c6f41b99085a26ec5e513ba18c40d5eca9e69c", "#8932B8"),
    /**
     * Red backpack texture.
     *
     * @since 1.0.0
     */
    RED("Red", "bce01699f796cbde95c849bee3cc369878452b40a41144366d66b4a0826cfaa0", "#B02E26"),
    /**
     * White backpack texture.
     *
     * @since 1.0.0
     */
    WHITE("White", "a2bb38516b29504186e11559cd5250ae218db4ddd27ae438726c847ce6b3c98", "#F9FFFE"),
    /**
     * Yellow backpack texture.
     *
     * @since 1.0.0
     */
    YELLOW("Yellow", "51bbbc5c24384ecb2f6844da285cccf9eb011c7a6670177cf75cd65513bc1274", "#FED83D");

    private final String friendly_name;
    private final String texture_id;
    private final String hex_color;

    /**
     * Constructs a BackpackColor with the specified texture ID and friendly name.
     * <p>
     * The texture ID is a hex string that represents the custom player head skin
     * texture used to visually represent the backpack in-game. Each color has a
     * unique texture that matches its name.
     *
     * @param friendly_name The friendly name of the enum.
     * @param texture_id    The hex string representing the custom skin texture ID.
     * @param hex_color     The hex color representing the dye item associated with this color.
     * @since 1.0.0
     */
    BackpackColor(String friendly_name, String texture_id, String hex_color) {
        this.friendly_name = friendly_name;
        this.texture_id = texture_id;
        this.hex_color = hex_color;
    }

    /**
     * Checks if a color exists in the BackpackColor enum by its name.
     * <p>
     * The check is case-insensitive. For example, both "RED" and "red" will match
     * the RED enum constant.
     *
     * @param key The color name to check.
     * @return {@code true} if the color exists in BackpackColor enum, {@code false} otherwise.
     * @throws IllegalArgumentException If the provided key is null.
     * @since 1.0.0
     */
    public static boolean containsColor(String key) {
        if (key != null) {
            try {
                return BackpackColor.valueOf(key.toUpperCase()) != null;
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        } else {
            throw new IllegalArgumentException("Key cannot be null");
        }
    }

    /**
     * Retrieves a BackpackColor enum value by its name, with fallback to default color.
     * <p>
     * The search is case-insensitive. If the requested color is not found, returns
     * the default color specified in the config. If the default color is also invalid,
     * returns BROWN.
     *
     * @param key The name of the color to retrieve.
     * @return The matching BackpackColor enum value, or the default color if not found.
     * @throws IllegalArgumentException If the provided key is null.
     * @see Config#getDefaultBackpackColor()
     * @since 1.0.0
     */
    @Nonnull
    public static BackpackColor getByName(String key) {
        if (key != null) {
            try {
                return BackpackColor.valueOf(key.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return Config.getDefaultBackpackColor();
            }
        } else {
            throw new IllegalArgumentException("Key cannot be null");
        }
    }

    /**
     * Converts a Minecraft dye material to its corresponding backpack color.
     * <p>
     * The material must be a dye item (ending with "_DYE"). For example,
     * Material.BLUE_DYE will return BackpackColor.BLUE.
     *
     * @param material The Material enum value of the dye.
     * @return The corresponding BackpackColor.
     * @throws IllegalArgumentException If the material is null or not a dye item.
     * @see org.bukkit.Material
     * @since 1.0.0
     */
    @Nonnull
    public static BackpackColor getByDye(Material material) {
        if (material.toString().endsWith("_DYE")) {
            return getByName(material.toString().replace("_DYE", "").toUpperCase());
        } else {
            throw new IllegalArgumentException("Material cannot be null");
        }
    }

    /**
     * Finds the next color in the BackpackColor enum sequence.
     * <p>
     * Colors are ordered as they are declared in the enum. When reaching the end
     * of the sequence, the behavior depends on the loop parameter.
     * <p>
     * If the current color is not found in the enum, returns null.
     *
     * @param currentColor The name of the current color.
     * @param loop         Whether to loop back to the first color when reaching the end (true), or return null (false).
     * @return The next color in sequence, or null if at the end and loop is false or if the current color is not found.
     * @throws IllegalArgumentException If the provided current color or loop parameter is null.
     * @since 1.0.0
     */
    @Nullable
    public static BackpackColor getNextColor(String currentColor, Boolean loop) {
        if (currentColor != null && loop != null) {
            BackpackColor[] values = BackpackColor.values();
            for (int i = 0; i < values.length; i++) {
                if (values[i].name().equals(currentColor)) {
                    if ((i == values.length - 1) && !loop) {
                        return null;
                    }
                    int nextIndex = loop ? (i + 1) % values.length : i + 1;
                    return values[nextIndex];
                }
            }
            return null;
        } else {
            throw new IllegalArgumentException("currentColor and loop cannot be null");
        }
    }

    /**
     * Retrieves a formatted display name for this color.
     *
     * @return A formatted string representing the color name.
     * @since 1.0.0
     */
    public String getFriendlyName() {
        return this.friendly_name;
    }

    /**
     * Retrieves the hex color code associated with this color.
     *
     * @return A string containing the hex color code (e.g., "#835432").
     * @since 1.0.0
     */
    public String getHexColor() {
        return this.hex_color;
    }

    /**
     * Retrieves the skin texture ID for this backpack color.
     * <p>
     * This ID is used to apply the custom skin texture that represents
     * the backpack in-game. The texture ID is a hex string that can be used
     * with Minecraft's player head skin system to display the backpack with
     * the appropriate color.
     *
     * @return The hex string representing the custom skin texture ID.
     * @since 1.0.0
     */
    public String getTextureID() {
        return this.texture_id;
    }

    /**
     * Gets the corresponding Minecraft dye item material for this color.
     * <p>
     * For example, BackpackColor.BROWN returns Material.BROWN_DYE.
     *
     * @return The Material enum value for the matching dye item.
     * @see org.bukkit.Material
     * @since 1.0.0
     */
    public Material getDyeItem() {
        return Material.getMaterial(String.format("%1$s_DYE", this.name()));
    }
}
