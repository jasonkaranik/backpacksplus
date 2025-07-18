package com.jasonkaranik.backpacksplus.enums;

import org.bukkit.entity.Player;

/**
 * Represents the various permission nodes used throughout the BackpacksPlus plugin.
 * <p>
 * This enum provides a centralized way to manage and check permissions for different
 * backpack operations. All permission strings are automatically prefixed with
 * "backpacksplus." when created.
 * <p>
 * Usage example:
 * <pre>
 * if (Permissions.OPEN_BACKPACK.check(player)) {
 *     // Allow player to open backpack
 * }
 * </pre>
 *
 * @see org.bukkit.entity.Player#hasPermission(String)
 * @since 1.0.0
 */
public enum Permissions {
    /**
     * Permission required to open the menu containing backpack configuration settings.
     *
     * @since 1.0.0
     */
    OPEN_BACKPACK_CONFIG_MENU("open_backpack_config_menu"),
    /**
     * Permission required to open backpacks.
     *
     * @since 1.0.0
     */
    OPEN_BACKPACK("open_backpacks"),
    /**
     * Permission required to dye backpacks.
     *
     * @since 1.0.0
     */
    DYE_BACKPACK("dye_backpacks"),
    /**
     * Permission required to rename backpacks.
     *
     * @since 1.0.0
     */
    RENAME_BACKPACK("rename_backpacks"),
    /**
     * Permission required to expand backpacks.
     *
     * @since 1.0.0
     */
    EXPAND_BACKPACK("expand_backpacks");

    private final String perm;

    /**
     * Constructs a Permissions enum with the specified permission string.
     * <p>
     * The provided value is automatically prefixed with "backpacksplus." to form
     * the complete permission node that will be checked against during permission
     * validation.
     *
     * @param perm The permission string without the plugin prefix (e.g., "open_backpacks")
     */
    Permissions(String perm) {
        this.perm = String.format("backpacksplus.%1$s", perm);
    }

    /**
     * Checks if a player has a specific permission.
     * <p>
     * Verifies if the player has been granted the base permission string
     * configured for this permission check. The method ensures null-safety
     * by explicitly validating the player parameter.
     *
     * @param player The player to check permissions for.
     * @return {@code true} if the player has the permission, {@code false} otherwise.
     * @throws IllegalArgumentException If the provided player is null.
     * @see Player#hasPermission(String)
     * @since 1.0.0
     */
    public boolean check(Player player) {
        if (player != null) {
            return player.hasPermission(this.perm);
        } else {
            throw new IllegalArgumentException("Player cannot be null");
        }
    }
}
