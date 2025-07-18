package com.jasonkaranik.backpacksplus.enums;

import com.jasonkaranik.backpacksplus.Config;
import com.jasonkaranik.backpacksplus.utils.Utils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the available sizes for backpacks in the game.
 * <p>
 * Each size determines the number of rows in the backpack's inventory, with each row
 * containing 9 slots. Sizes range from SMALL (1 row) to JUMBO (5 rows), allowing
 * for different storage capacities.
 * <p>
 * Example usage:
 * <pre>
 * BackpackSize size = BackpackSize.MEDIUM;
 * int slots = size.getInventorySlots(); // Returns 18 (2 rows * 9 slots per row)
 * List&lt;String&gt; cost = size.getExpansionCostLore(); // Gets upgrade requirements
 * </pre>
 *
 * @see Config#getBackpackSizeExpansionCost(BackpackSize)
 * @since 1.0.0
 */
public enum BackpackSize {
    /**
     * Small backpack with 1 row of inventory space.
     * <p>
     * Total slots: 9
     * Used as the default starting size for new backpacks.
     *
     * @since 1.0.0
     */
    SMALL("Small", 1),

    /**
     * Medium backpack with 2 rows of inventory space.
     * <p>
     * Total slots: 18
     * First upgrade tier from SMALL.
     *
     * @since 1.0.0
     */
    MEDIUM("Medium", 2),

    /**
     * Large backpack with 3 rows of inventory space.
     * <p>
     * Total slots: 27
     * Second upgrade tier.
     *
     * @since 1.0.0
     */
    LARGE("Large", 3),

    /**
     * Greater backpack with 4 rows of inventory space.
     * <p>
     * Total slots: 36
     * Third upgrade tier.
     *
     * @since 1.0.0
     */
    GREATER("Greater", 4),

    /**
     * Jumbo backpack with 5 rows of inventory space.
     * <p>
     * Total slots: 45
     * Maximum size tier available.
     *
     * @since 1.0.0
     */
    JUMBO("Jumbo", 5);

    private final String friendly_name;
    private final int inv_rows;

    /**
     * Constructs a BackpackSize with the specified number of inventory rows and friendly name.
     * <p>
     * Each row in a Minecraft inventory contains 9 slots, so the total capacity
     * of a backpack is value * 9 slots.
     *
     * @param friendly_name The friendly name of the enum.
     * @param inv_rows      The number of inventory rows (1-5).
     * @since 1.0.0
     */
    BackpackSize(String friendly_name, int inv_rows) {
        this.friendly_name = friendly_name;
        this.inv_rows = inv_rows;
    }

    /**
     * Checks if a size exists in the BackpackSize enum by its name.
     * <p>
     * The check is case-insensitive. For example, both "SMALL" and "small" will
     * match the SMALL enum constant.
     *
     * @param key The size name to check.
     * @return {@code true} if the size exists in BackpackSize enum, {@code false} otherwise.
     * @throws IllegalArgumentException If the provided key is null.
     * @since 1.0.0
     */
    public static boolean containsSize(String key) {
        if (key != null) {
            try {
                return BackpackSize.valueOf(key.toUpperCase()) != null;
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        } else {
            throw new IllegalArgumentException("Key cannot be null");
        }
    }

    /**
     * Retrieves a BackpackSize enum value by its name, with fallback to default size.
     * <p>
     * The search is case-insensitive. If the requested size is not found, returns
     * the default size specified in the config. If the default size is also invalid,
     * returns SMALL.
     *
     * @param key The name of the size to retrieve.
     * @return The matching BackpackSize enum value, or the default size if not found.
     * @throws IllegalArgumentException If the provided key is null.
     * @see Config#getDefaultBackpackSize()
     * @since 1.0.0
     */
    @Nonnull
    public static BackpackSize getByName(String key) {
        if (key != null) {
            try {
                return BackpackSize.valueOf(key.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return Config.getDefaultBackpackSize();
            }
        } else {
            throw new IllegalArgumentException("Key cannot be null");
        }
    }

    /**
     * Finds the next size in the BackpackSize enum sequence.
     * <p>
     * Sizes are ordered as they are declared in the enum: SMALL → MEDIUM → LARGE → GREATER → JUMBO.
     * When reaching the end of the sequence, the behavior depends on the loop parameter.
     * <p>
     * If the current size is not found in the enum, returns null.
     *
     * @param currentSize The name of the current size.
     * @param loop        Whether to loop back to SMALL when reaching JUMBO (true), or return null (false).
     * @return The next size in sequence, or null if at JUMBO and loop is false or if the current size is not found.
     * @throws IllegalArgumentException If the provided currentSize or loop parameter is null.
     * @since 1.0.0
     */
    @Nullable
    public static BackpackSize getNextSize(String currentSize, Boolean loop) {
        if (currentSize != null && loop != null) {
            BackpackSize[] values = BackpackSize.values();
            for (int i = 0; i < values.length; i++) {
                if (values[i].name().equals(currentSize)) {
                    if ((i == values.length - 1) && !loop) {
                        return null;
                    }
                    int nextIndex = loop ? (i + 1) % values.length : i + 1;
                    return values[nextIndex];
                }
            }
            return null;
        } else {
            throw new IllegalArgumentException("currentSize and loop cannot be null");
        }
    }

    /**
     * Retrieves the item cost required to expand a backpack to this size.
     * <p>
     * The expansion cost is defined in the plugin configuration and represents
     * the items and quantities that must be consumed to upgrade a backpack
     * to this size tier from the previous tier.
     * <p>
     * For example, calling this method on BackpackSize.MEDIUM returns the
     * items needed to upgrade from SMALL to MEDIUM.
     *
     * @return Array of ItemStacks representing the items and quantities needed for expansion.
     * @see Config#getBackpackSizeExpansionCost(BackpackSize)
     * @since 1.0.0
     */
    @Nonnull
    public ItemStack[] getExpansionCost() {
        return Config.getBackpackSizeExpansionCost(this);
    }

    /**
     * Retrieves the expansion cost lore for upgrading to this size.
     * <p>
     * Each line in the returned list represents an item requirement for upgrading
     * the backpack, formatted with color codes for display in item lore.
     * <p>
     * The format follows: "&amp;8 - &amp;a[Item Name] &amp;8x[Quantity]", where:
     * - Item names are displayed in green (&amp;a)
     * - Quantities and decorative elements are displayed in dark gray (&amp;8)
     * - Empty or air items are excluded from the list
     *
     * @return List of colored strings representing the upgrade requirements.
     * @see Config#getBackpackSizeExpansionCost(BackpackSize)
     * @see Utils#colorify(String)
     * @since 1.0.0
     */
    @Nonnull
    public List<String> getExpansionCostLore() {
        List<String> list = new ArrayList<>();
        ItemStack[] cost = Config.getBackpackSizeExpansionCost(this);
        for (ItemStack item : cost) {
            if (item != null && item.getType() != Material.AIR) {
                list.add(Utils.colorify(String.format("&8 - &a%1$s &8x%2$s", Utils.getItemName(item), item.getAmount())));
            }
        }
        return list;
    }

    /**
     * Retrieves a formatted display name for this size.
     *
     * @return A formatted string representing the size name.
     * @since 1.0.0
     */
    public String getFriendlyName() {
        return this.friendly_name;
    }

    /**
     * Calculates the total number of inventory slots for this backpack size.
     * <p>
     * The total slots are calculated by multiplying the number of rows by 9
     * (the number of slots per row in Minecraft inventories).
     *
     * @return The total number of inventory slots available.
     * @since 1.0.0
     */
    public int getInventorySlots() {
        return this.inv_rows * 9;
    }
}
