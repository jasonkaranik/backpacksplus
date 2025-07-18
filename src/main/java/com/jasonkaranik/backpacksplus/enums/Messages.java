package com.jasonkaranik.backpacksplus.enums;

import com.jasonkaranik.backpacksplus.utils.Utils;

/**
 * Represents the various messages displayed to players by the BackpacksPlus plugin.
 * <p>
 * This enum provides a centralized way to manage all player-facing messages,
 * ensuring consistency throughout the plugin. Messages support color codes using
 * the '&amp;' notation which are automatically converted to Minecraft's color format
 * when retrieved.
 * <p>
 * Some messages contain format placeholders (e.g., %1$s) that are replaced with
 * dynamic content when the message is displayed to players.
 * <p>
 * Usage example:
 * <pre>
 * player.sendMessage(Messages.NO_PERMISSION.getMessage());
 * </pre>
 *
 * @see Utils#colorify(String)
 * @since 1.0.0
 */
public enum Messages {
    /**
     * Shown when a player attempts an action without the required permission.
     *
     * @since 1.0.0
     */
    NO_PERMISSION("&cYou don't have permission to do that!"),
    /**
     * Shown when a player attempts to open or customize a backpack that is no longer
     * in their inventory. This typically occurs when the backpack was dropped, moved,
     * or removed after the player initiated the interaction.
     *
     * @since 1.0.0
     */
    BACKPACK_NOT_FOUND_IN_INVENTORY("&cBackpack is no longer in your inventory!"),
    /**
     * Shown when the plugin fails to remove items from a player's inventory.
     *
     * @since 1.0.0
     */
    COULD_NOT_REMOVE_ITEMS_FROM_INVENTORY("&cCouldn't remove items from your inventory."),
    /**
     * Shown when a player successfully sets the default contents for new backpacks.
     *
     * @since 1.0.0
     */
    SET_DEFAULT_CONTENTS_SUCCESS("&aDefault contents saved!"),
    /**
     * Shown when a player successfully sets the default color for new backpacks.
     * The %1$s parameter represents the new default color.
     *
     * @since 1.0.0
     */
    SET_DEFAULT_COLOR_SUCCESS("&aDefault color set to &e%1$s&a!"),
    /**
     * Shown when a player successfully sets the default size for new backpacks.
     * The %1$s parameter represents the new default size.
     *
     * @since 1.0.0
     */
    SET_DEFAULT_SIZE_SUCCESS("&aDefault size set to &e%1$s!"),
    /**
     * Shown when a player successfully sets the default display name for new backpacks.
     * The %1$s parameter represents the new default display name.
     *
     * @since 1.0.0
     */
    SET_DEFAULT_DISPLAY_NAME_SUCCESS("&aDefault display name set to &e%1$s&a!"),
    /**
     * Shown when a player tries to set the default display name without holding a name tag.
     *
     * @since 1.0.0
     */
    SET_DISPLAY_NAME_FAIL_INCORRECT_MATERIAL("&cYou need to be holding a name tag."),
    /**
     * Shown when a player successfully sets the cost for expanding a backpack to a specific size.
     * The %1$s parameter represents the backpack size.
     *
     * @since 1.0.0
     */
    SET_EXPANSION_COST_SUCCESS("&aExpansion cost for &e%1$s &abackpacks set!"),
    /**
     * Shown when a player successfully dyes their backpack.
     * The %1$s parameter represents the new color.
     *
     * @since 1.0.0
     */
    DYE_SUCCESS("&aBackpack dyed &e%1$s&a!"),
    /**
     * Shown when a player tries to dye their backpack the same color it already is.
     *
     * @since 1.0.0
     */
    DYE_FAIL_SAME_COLOR("&cYour backpack is already that color."),
    /**
     * Shown when a player tries to dye a backpack with an incorrect item.
     *
     * @since 1.0.0
     */
    DYE_FAIL_INCORRECT_MATERIAL("&cYou need to use a dye."),
    /**
     * Shown when a player successfully renames their backpack.
     * The %1$s parameter represents the new name.
     *
     * @since 1.0.0
     */
    RENAME_SUCCESS("&aBackpack renamed to &e%1$s&a!"),
    /**
     * Shown when a player tries to rename a backpack with a name exceeding 30 characters.
     *
     * @since 1.0.0
     */
    RENAME_FAIL_NAME_TOO_LONG("&cName too long! (30 characters max)"),
    /**
     * Shown when a player tries to rename a backpack or change the default backpack display name using an unnamed name tag.
     *
     * @since 1.0.0
     */
    RENAME_FAIL_NAME_TAG_NOT_RENAMED("&cName tag must be renamed first."),
    /**
     * Shown when a player tries to rename a backpack with an incorrect item.
     *
     * @since 1.0.0
     */
    RENAME_FAIL_INCORRECT_MATERIAL("&cYou need to use a name tag."),
    /**
     * Shown when a player successfully expands their backpack.
     * The %1$s parameter represents the new size.
     *
     * @since 1.0.0
     */
    EXPAND_SUCCESS("&aBackpack expanded to &e%1$s &asize!"),
    /**
     * Shown when a player tries to expand a backpack without meeting the item requirements.
     *
     * @since 1.0.0
     */
    EXPAND_FAIL_MISSING_ITEMS("&cMissing required items for expansion."),
    /**
     * Shown when a player tries to expand a backpack that has already reached its maximum size.
     *
     * @since 1.0.0
     */
    EXPAND_FAIL_MAX_SIZE("&cBackpack has reached maximum size.");

    private final String msg;

    /**
     * Constructs a Messages enum with the specified message string.
     * <p>
     * The provided value contains the raw message text which may include:
     * <ul>
     *   <li>Color codes using the '&amp;' notation (e.g., '&amp;c' for red text)</li>
     *   <li>Format placeholders (e.g., '%1$s') for dynamic content</li>
     * </ul>
     * Color codes are converted to Minecraft's format when the message is retrieved
     * via the {@link #getMessage()} method.
     *
     * @param msg The raw message string with color codes and/or format placeholders
     * @see #getMessage()
     */
    Messages(String msg) {
        this.msg = msg;
    }

    /**
     * Gets the color-coded message string.
     * <p>
     * Returns the message with all color codes converted from '&amp;' notation
     * to the internal Minecraft color format using the {@link Utils#colorify(String)} method.
     *
     * @return The processed message with converted color codes.
     * @see Utils#colorify(String)
     * @since 1.0.0
     */
    public String getMessage() {
        return Utils.colorify(this.msg);
    }
}
