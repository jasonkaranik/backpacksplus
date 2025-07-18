package com.jasonkaranik.backpacksplus;

import com.jasonkaranik.backpacksplus.enums.BackpackColor;
import com.jasonkaranik.backpacksplus.enums.BackpackSize;
import com.jasonkaranik.backpacksplus.utils.ItemStackCodec;
import com.jasonkaranik.backpacksplus.utils.Utils;
import com.jasonkaranik.json.simple.extended.JSONObject;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages configuration settings and persistence for the BackpacksPlus plugin.
 * <p>
 * This class handles all aspects of the plugin's configuration including:
 * <ul>
 *   <li>Loading and saving configuration from JSON files</li>
 *   <li>Backpack settings (crafting, expansion costs, defaults)</li>
 *   <li>Permission configurations</li>
 * </ul>
 * <p>
 * The configuration is stored in JSON format and automatically creates missing keys
 * with default values during initialization. All configuration changes are persisted
 * to disk automatically.
 * <p>
 * Configuration categories include:
 * <ul>
 *   <li>Backpack mechanics (NBT storage, crafting recipes)</li>
 *   <li>Default backpack properties (size, color, display name)</li>
 *   <li>Permission requirements for various actions</li>
 * </ul>
 *
 * @since 1.0.0
 */
public class Config {
    private static final Path config_file_path = Path.of(BackpacksPlus.getPlugin().getDataFolder().getAbsolutePath(), "config.json");

    private static JSONObject config = null;

    private static boolean pendingChange = false;

    /**
     * Loads or updates the plugin's configuration file, ensuring all required settings exist.
     * <p>
     * This method performs the following operations:
     * <ul>
     *   <li>Loads existing configuration from disk if present</li>
     *   <li>Creates a new configuration file with defaults if none exists</li>
     *   <li>Validates and adds any missing configuration keys with default values</li>
     *   <li>Saves the updated configuration back to disk</li>
     * </ul>
     * <p>
     * Default settings are created for:
     * <ul>
     *   <li>Backpack crafting recipe and ingredients</li>
     *   <li>Size upgrade costs for different backpack tiers</li>
     *   <li>Default backpack properties (size, color, display name)</li>
     *   <li>Permission requirements for all backpack actions</li>
     * </ul>
     * <p>s
     * If any IO operations fail during loading or saving, errors are logged to the server
     * console and the server shuts down.
     *
     * @since 1.0.0
     */
    public static void update() {
        if (config == null) {
            File config_file = new File(config_file_path.toUri());
            if (config_file.exists()) {
                try {
                    config = new JSONObject(Files.readString(config_file_path));
                } catch (IOException e) {
                    Utils.logSevereErrorAndShutdown(String.format("Couldn't load config: %1$s", e.getMessage()));
                }
            } else {
                config = new JSONObject();
            }
        }

        // BACKPACK

        addMissing("backpack", new JSONObject());

        // BACKPACK - CRAFTING RECIPE

        addMissing("backpack.crafting_recipe", new JSONObject());
        addMissing("backpack.crafting_recipe.rows", new JSONObject());
        addMissing("backpack.crafting_recipe.rows.1", "SLS");
        addMissing("backpack.crafting_recipe.rows.2", "LCL");
        addMissing("backpack.crafting_recipe.rows.3", "LLL");

        addMissing("backpack.crafting_recipe.ingredients", new JSONObject());
        addMissing("backpack.crafting_recipe.ingredients.S", "STRING");
        addMissing("backpack.crafting_recipe.ingredients.L", "LEATHER");
        addMissing("backpack.crafting_recipe.ingredients.C", "CHEST");

        // BACKPACK - EXPANSION COST

        addMissing("backpack.expansion_cost", new JSONObject());
        addMissing("backpack.expansion_cost.SMALL", ItemStackCodec.encode(new ItemStack[]{})); // To prevent errors when BackpackSize.SMALL.getExpansionCost() is called.
        addMissing("backpack.expansion_cost.MEDIUM", ItemStackCodec.encode(new ItemStack[]{new ItemStack(Material.RABBIT_HIDE, 8), new ItemStack(Material.CHEST, 1)}));
        addMissing("backpack.expansion_cost.LARGE", ItemStackCodec.encode(new ItemStack[]{new ItemStack(Material.RABBIT_HIDE, 16), new ItemStack(Material.CHEST, 1)}));
        addMissing("backpack.expansion_cost.GREATER", ItemStackCodec.encode(new ItemStack[]{new ItemStack(Material.RABBIT_HIDE, 32), new ItemStack(Material.CHEST, 1)}));
        addMissing("backpack.expansion_cost.JUMBO", ItemStackCodec.encode(new ItemStack[]{new ItemStack(Material.RABBIT_HIDE, 64), new ItemStack(Material.CHEST, 1)}));

        // BACKPACK - DEFAULTS

        addMissing("backpack.defaults", new JSONObject());
        addMissing("backpack.defaults.display_name", "&aBackpack");
        addMissing("backpack.defaults.color", "BROWN");
        addMissing("backpack.defaults.size", "SMALL");
        addMissing("backpack.defaults.contents", ItemStackCodec.encode(new ItemStack[]{}));

        // BACKPACK - PERMISSIONS

        addMissing("backpack.permissions", new JSONObject());
        addMissing("backpack.permissions.open_backpack_config_menu", true);
        addMissing("backpack.permissions.open_backpacks", true);
        addMissing("backpack.permissions.dye_backpacks", true);
        addMissing("backpack.permissions.rename_backpacks", true);
        addMissing("backpack.permissions.expand_backpacks", true);

        if (pendingChange) {
            pendingChange = false;
            save();
        }
    }

    /**
     * Adds a configuration key-value pair if the key doesn't already exist in the configuration.
     * <p>
     * This utility method is used during configuration initialization to populate missing
     * settings with their default values. If the specified key is already present in the
     * configuration, no action is taken to preserve existing user settings.
     * <p>
     * When a missing key is detected and added, the method sets a pending change flag
     * to indicate that the configuration needs to be saved to disk. This ensures that
     * all newly added default values are persisted automatically.
     *
     * @param key   The configuration key to check and potentially add, using dot notation for nested properties.
     * @param value The default value to set if the key is missing.
     * @since 1.0.0
     */
    private static void addMissing(String key, Object value) {
        if (!config.containsKey(key)) {
            config.put(key, value);
            pendingChange = true;
        }
    }

    /**
     * Saves the current configuration settings to the JSON configuration file on disk.
     * <p>
     * This method writes the entire configuration object to the config.json file in the
     * plugin's data folder. The configuration is serialized to JSON format using the
     * JSONObject's toJSONString() method.
     * <p>
     * This method is called automatically by {@link #update()} when configuration
     * changes are detected, ensuring that all modifications are persisted immediately.
     * <p>
     * If the save operation fails due to IO issues (such as insufficient disk space,
     * file permissions, or other filesystem errors), a severe error is logged and
     * the server is shut down to prevent data inconsistency.
     *
     * @since 1.0.0
     */
    private static void save() {
        try {
            Files.writeString(config_file_path, config.toJSONString());
        } catch (IOException e) {
            Utils.logSevereErrorAndShutdown(String.format("Couldn't save config: %1$s", e.getMessage()));
        }
    }

    /**
     * Retrieves the pattern for the first row of the backpack crafting recipe.
     * <p>
     * The pattern uses Minecraft's crafting recipe format where each character
     * corresponds to an ingredient defined in the ingredients map.
     *
     * @return Three-character string representing the first recipe row (e.g. "SLS").
     * @see #setFirstCraftingRecipeRow(String)
     * @see #getSecondCraftingRecipeRow()
     * @see #getThirdCraftingRecipeRow()
     * @see #getCraftingRecipeIngredients()
     * @since 1.0.0
     */
    @Nonnull
    public static String getFirstCraftingRecipeRow() {
        return (String) config.get("backpack.crafting_recipe.rows.1");
    }

    /**
     * Sets the pattern for the first row of the backpack crafting recipe.
     * <p>
     * Example:
     * <pre>
     * // Set first row to string, leather, string
     * setFirstCraftingRecipeRow("SLS");
     * </pre>
     *
     * @param value Three-character string where each character maps to an ingredient.
     * @throws IllegalArgumentException If the provided value is null or not exactly 3 characters.
     * @throws IllegalStateException    If pattern contains undefined ingredients.
     * @see #getFirstCraftingRecipeRow()
     * @see #setSecondCraftingRecipeRow(String)
     * @see #setThirdCraftingRecipeRow(String)
     * @see #getCraftingRecipeIngredients()
     * @since 1.0.0
     */
    public static void setFirstCraftingRecipeRow(String value) {
        if (value != null) {
            config.put("backpack.crafting_recipe.rows.1", value);
            save();
        } else {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }

    /**
     * Retrieves the pattern for the second row of the backpack crafting recipe.
     * <p>
     * The pattern uses Minecraft's crafting recipe format where each character
     * corresponds to an ingredient defined in the ingredients map.
     *
     * @return Three-character string representing the second recipe row (e.g. "LCL").
     * @see #setSecondCraftingRecipeRow(String)
     * @see #getFirstCraftingRecipeRow()
     * @see #getThirdCraftingRecipeRow()
     * @see #getCraftingRecipeIngredients()
     * @since 1.0.0
     */
    @Nonnull
    public static String getSecondCraftingRecipeRow() {
        return (String) config.get("backpack.crafting_recipe.rows.2");
    }

    /**
     * Sets the pattern for the second row of the backpack crafting recipe.
     * <p>
     * Example:
     * <pre>
     * // Set second row to all leather, chest, leather
     * setSecondCraftingRecipeRow("LCL");
     * </pre>
     *
     * @param value Three-character string where each character maps to an ingredient.
     * @throws IllegalArgumentException If the provided value is null or not exactly 3 characters.
     * @throws IllegalStateException    If pattern contains undefined ingredients.
     * @see #getSecondCraftingRecipeRow()
     * @see #setFirstCraftingRecipeRow(String)
     * @see #setThirdCraftingRecipeRow(String)
     * @see #getCraftingRecipeIngredients()
     * @since 1.0.0
     */
    public static void setSecondCraftingRecipeRow(String value) {
        if (value != null) {
            config.put("backpack.crafting_recipe.rows.2", value);
            save();
        } else {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }

    /**
     * Retrieves the pattern for the third row of the backpack crafting recipe.
     * <p>
     * The pattern uses Minecraft's crafting recipe format where each character
     * corresponds to an ingredient defined in the ingredients map.
     *
     * @return Three-character string representing the third recipe row (e.g. "LLL").
     * @see #setThirdCraftingRecipeRow(String)
     * @see #getFirstCraftingRecipeRow()
     * @see #getSecondCraftingRecipeRow()
     * @see #getCraftingRecipeIngredients()
     * @since 1.0.0
     */
    @Nonnull
    public static String getThirdCraftingRecipeRow() {
        return (String) config.get("backpack.crafting_recipe.rows.3");
    }

    /**
     * Sets the pattern for the third row of the backpack crafting recipe.
     * <p>
     * Example:
     * <pre>
     * // Set third row to all leather
     * setThirdCraftingRecipeRow("LLL");
     * </pre>
     *
     * @param value Three-character string where each character maps to an ingredient.
     * @throws IllegalArgumentException If the provided value is null or not exactly 3 characters.
     * @throws IllegalStateException    If pattern contains undefined ingredients.
     * @see #getThirdCraftingRecipeRow()
     * @see #setFirstCraftingRecipeRow(String)
     * @see #setSecondCraftingRecipeRow(String)
     * @see #getCraftingRecipeIngredients()
     * @since 1.0.0
     */
    public static void setThirdCraftingRecipeRow(String value) {
        if (value != null) {
            config.put("backpack.crafting_recipe.rows.3", value);
            save();
        } else {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }

    /**
     * Retrieves the mapped ingredients used in the backpack crafting recipe.
     * <p>
     * Each key in the JSON object represents a character used in the recipe patterns,
     * and the corresponding value defines the acceptable ingredients for that position.
     *
     * <p>
     * Example format:
     * <pre>
     * {
     *   "S": "STRING",
     *   "L": "LEATHER",
     *   "C": "CHEST"
     * }
     * </pre>
     *
     * @return JSONObject mapping pattern characters to ingredient specifications.
     * @see #setCraftingRecipeIngredients(JSONObject)
     * @see #getFirstCraftingRecipeRow()
     * @see #getSecondCraftingRecipeRow()
     * @see #getThirdCraftingRecipeRow()
     * @since 1.0.0
     */
    @Nonnull
    public static JSONObject getCraftingRecipeIngredients() {
        return (JSONObject) config.get("backpack.crafting_recipe.ingredients");
    }

    /**
     * Sets the ingredients map for the backpack crafting recipe in the configuration.
     * The ingredients define what items are needed to craft a backpack.
     * <p>
     * Each key in the provided JSONObject should be a single character that appears in the
     * crafting pattern rows. The corresponding value should be a valid Minecraft material name.
     * <p>
     * Example:
     * <pre>
     * JSONObject ingredients = new JSONObject();
     * ingredients.put("L", "LEATHER");
     * ingredients.put("S", "STRING");
     * ingredients.put("C", "CHEST");
     * setCraftingRecipeIngredients(ingredients);
     * </pre>
     *
     * @param value JSONObject containing the crafting recipe ingredients mapping item types to quantities.
     * @throws IllegalArgumentException If the provided value is null.
     * @see #getCraftingRecipeIngredients()
     * @see #setFirstCraftingRecipeRow(String)
     * @see #setSecondCraftingRecipeRow(String)
     * @see #setThirdCraftingRecipeRow(String)
     * @since 1.0.0
     */
    public static void setCraftingRecipeIngredients(JSONObject value) {
        if (value != null) {
            config.put("backpack.crafting_recipe.ingredients", value);
            save();
        } else {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }

    /**
     * Retrieves the item cost required to expand a backpack to that specific size.
     * Different backpack sizes may have different expansion costs associated with them.
     * <p>
     * The returned items represent the resources a player must provide to upgrade
     * their backpack to the specified size tier.
     *
     * @param size The backpack size to get the expansion cost for.
     * @return Array of ItemStacks representing the items and quantities needed for expansion.
     * @throws IllegalArgumentException If the provided size is null.
     * @see #setBackpackSizeExpansionCost(BackpackSize, ItemStack[])
     * @since 1.0.0
     */
    @Nonnull
    public static ItemStack[] getBackpackSizeExpansionCost(BackpackSize size) {
        if (size != null) {
            return ItemStackCodec.decode((String) config.get(String.format("backpack.expansion_cost.%1$s", size.name())));
        } else {
            throw new IllegalArgumentException("Size cannot be null");
        }
    }

    /**
     * Sets the item cost required to expand a backpack to that specific size.
     * This cost will be required when players attempt to upgrade their backpack to that specific size.
     *
     * @param size  The backpack size to set expansion cost for.
     * @param value Array of ItemStacks defining the items and quantities needed for expansion.
     * @throws IllegalArgumentException If either the provided size or value is null.
     * @see #getBackpackSizeExpansionCost(BackpackSize)
     * @since 1.0.0
     */
    public static void setBackpackSizeExpansionCost(BackpackSize size, ItemStack[] value) {
        if (size != null && value != null) {
            config.put(String.format("backpack.expansion_cost.%1$s", size.name()), ItemStackCodec.encode(value));
            save();
        } else {
            throw new IllegalArgumentException("Size and Value cannot be null");
        }
    }

    /**
     * Retrieves the default display name given to newly created backpacks.
     * This name is used when a new backpack is created.
     * <p>
     * The returned string may contain color codes prefixed with '&amp;' that need
     * to be translated before display.
     *
     * @return The default backpack display name.
     * @see #setDefaultBackpackDisplayName(String)
     * @since 1.0.0
     */
    @Nonnull
    public static String getDefaultBackpackDisplayName() {
        return (String) config.get("backpack.defaults.display_name");
    }

    /**
     * Sets the default display name for newly created backpacks.
     * This name will be applied to all new backpacks.
     *
     * @param value The new default display name to use.
     * @throws IllegalArgumentException If the provided value is null.
     * @see #getDefaultBackpackDisplayName()
     * @since 1.0.0
     */
    public static void setDefaultBackpackDisplayName(String value) {
        if (value != null) {
            config.put("backpack.defaults.display_name", value);
            save();
        } else {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }

    /**
     * Retrieves the default color applied to newly created backpacks.
     * If the configured color is invalid, returns BROWN as a fallback.
     *
     * @return The default BackpackColor (defaults to BROWN if invalid).
     * @see #setDefaultBackpackColor(BackpackColor)
     * @since 1.0.0
     */
    @Nonnull
    public static BackpackColor getDefaultBackpackColor() {
        String color = (String) config.get("backpack.defaults.color");
        return BackpackColor.containsColor(color) ? BackpackColor.getByName(color) : BackpackColor.BROWN;
    }

    /**
     * Sets the default color for newly created backpacks.
     * This color will be applied to all new backpacks.
     *
     * @param value The BackpackColor to use as default.
     * @throws IllegalArgumentException If the provided value is null.
     * @see #getDefaultBackpackColor()
     * @since 1.0.0
     */
    public static void setDefaultBackpackColor(BackpackColor value) {
        if (value != null) {
            config.put("backpack.defaults.color", value.name());
            save();
        } else {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }

    /**
     * Retrieves the default size for newly created backpacks.
     * If the configured size is invalid, returns SMALL as a fallback.
     *
     * @return The default BackpackSize (defaults to SMALL if invalid).
     * @see #setDefaultBackpackSize(BackpackSize)
     * @since 1.0.0
     */
    @Nonnull
    public static BackpackSize getDefaultBackpackSize() {
        String size = (String) config.get("backpack.defaults.size");
        return BackpackSize.containsSize(size) ? BackpackSize.getByName(size) : BackpackSize.SMALL;
    }

    /**
     * Sets the default size for newly created backpacks.
     * This size will be applied to all new backpacks.
     *
     * @param value The BackpackSize to use as default.
     * @throws IllegalArgumentException If the provided value is null.
     * @see #getDefaultBackpackSize()
     * @since 1.0.0
     */
    public static void setDefaultBackpackSize(BackpackSize value) {
        if (value != null) {
            config.put("backpack.defaults.size", value.name());
            save();
        } else {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }

    /**
     * Retrieves the default items that should be present in newly created backpacks.
     * Returns an empty array if no default contents are configured.
     * <p>
     * These items are automatically added to a player's backpack when they
     * first craft one, providing a starter set of items or materials.
     *
     * @return Array of ItemStacks representing the default contents.
     * @see #setDefaultBackpackContents(ItemStack[])
     * @since 1.0.0
     */
    @Nonnull
    public static ItemStack[] getDefaultBackpackContents() {
        return ItemStackCodec.decode((String) config.get("backpack.defaults.contents"));
    }

    /**
     * Sets the default items that should be present in newly created backpacks.
     * These items will be automatically added to new backpacks upon creation.
     *
     * @param value Array of ItemStacks defining the default contents.
     * @throws IllegalArgumentException If the provided value is null.
     * @see #getDefaultBackpackContents()
     * @since 1.0.0
     */
    public static void setDefaultBackpackContents(ItemStack[] value) {
        if (value != null) {
            config.put("backpack.defaults.contents", ItemStackCodec.encode(value));
            save();
        } else {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }

    /**
     * Retrieves the serialized string representation of the default items that should
     * be present in newly created backpacks. Returns the raw encoded string before
     * deserialization.
     * <p>
     * This method returns the same data as {@link #getDefaultBackpackContents()} but
     * in its serialized form.
     *
     * @return Serialized string representation of the default backpack contents.
     * @see #getDefaultBackpackContents()
     * @see #setDefaultBackpackContents(ItemStack[])
     * @since 1.0.0
     */
    @Nonnull
    public static String getDefaultBackpackContentsSerialized() {
        return (String) config.get("backpack.defaults.contents");
    }

    /**
     * Checks if players need specific permission to open the backpack configuration.
     * Controls access to the backpack configuration menu.
     * <p>
     * When set to true, players will need the permission node "backpacksplus.open_backpack_config_menu"
     * to access the configuration menu. When false, any player can access it.
     *
     * @return {@code true} if permission is required to open the configuration menu, {@code false} if anyone can open it.
     * @see #setPermissionToOpenBackpackConfigMenuRequired(Boolean)
     * @since 1.0.0
     */
    public static boolean getPermissionToOpenBackpackConfigMenuRequired() {
        return (boolean) config.get("backpack.permissions.open_backpack_config_menu");
    }

    /**
     * Sets whether players need specific permission to open the backpack configuration menu.
     * Controls access to the backpack configuration menu.
     *
     * @param value {@code true} to require permission, {@code false} to allow anyone.
     * @throws IllegalArgumentException If the provided value is null.
     * @see #getPermissionToOpenBackpackConfigMenuRequired()
     * @since 1.0.0
     */
    public static void setPermissionToOpenBackpackConfigMenuRequired(Boolean value) {
        if (value != null) {
            config.put("backpack.permissions.open_backpack_config_menu", value);
            save();
        } else {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }

    /**
     * Checks if players need specific permission to open and access backpack contents.
     * Controls who can open backpacks.
     *
     * @return {@code true} if permission is required to open backpacks, {@code false} if anyone can open them.
     * @see #setPermissionToOpenBackpacksRequired(Boolean)
     * @since 1.0.0
     */
    public static boolean getPermissionToOpenBackpacksRequired() {
        return (boolean) config.get("backpack.permissions.open_backpacks");
    }

    /**
     * Sets whether players need specific permission to open and access backpack contents.
     * Controls who can open backpacks.
     *
     * @param value {@code true} to require permission, {@code false} to allow anyone.
     * @throws IllegalArgumentException If the provided value is null.
     * @see #getPermissionToOpenBackpacksRequired()
     * @since 1.0.0
     */
    public static void setPermissionToOpenBackpacksRequired(Boolean value) {
        if (value != null) {
            config.put("backpack.permissions.open_backpacks", value);
            save();
        } else {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }

    /**
     * Checks if players need specific permission to dye backpacks.
     * Controls who can dye backpacks.
     *
     * @return {@code true} if permission is required to dye backpacks, {@code false} if anyone can dye them.
     * @see #setPermissionToDyeBackpacksRequired(Boolean)
     * @since 1.0.0
     */
    public static boolean getPermissionToDyeBackpacksRequired() {
        return (boolean) config.get("backpack.permissions.dye_backpacks");
    }

    /**
     * Sets whether players need specific permission to dye backpacks.
     * Controls who can dye backpacks.
     *
     * @param value {@code true} to require permission, {@code false} to allow anyone.
     * @throws IllegalArgumentException If the provided value is null.
     * @see #getPermissionToDyeBackpacksRequired()
     * @since 1.0.0
     */
    public static void setPermissionToDyeBackpacksRequired(Boolean value) {
        if (value != null) {
            config.put("backpack.permissions.dye_backpacks", value);
            save();
        } else {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }

    /**
     * Checks if players need specific permission to rename backpacks.
     * Controls who can rename backpacks.
     *
     * @return {@code true} if permission is required to rename backpacks, {@code false} if anyone can rename them.
     * @see #setPermissionToRenameBackpacksRequired(Boolean)
     * @since 1.0.0
     */
    public static boolean getPermissionToRenameBackpacksRequired() {
        return (boolean) config.get("backpack.permissions.rename_backpacks");
    }

    /**
     * Sets whether players need specific permission to rename backpacks.
     * Controls who can rename backpacks.
     *
     * @param value {@code true} to require permission, {@code false} to allow anyone.
     * @throws IllegalArgumentException If the provided value is null.
     * @see #getPermissionToRenameBackpacksRequired()
     * @since 1.0.0
     */
    public static void setPermissionToRenameBackpacksRequired(Boolean value) {
        if (value != null) {
            config.put("backpack.permissions.rename_backpacks", value);
            save();
        } else {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }

    /**
     * Checks if players need specific permission to expand a backpack's size.
     * Controls who can expand backpacks.
     *
     * @return {@code true} if permission is required to expand backpacks, {@code false} if anyone can expand them.
     * @see #setPermissionToExpandBackpacksRequired(Boolean)
     * @since 1.0.0
     */
    public static boolean getPermissionToExpandBackpacksRequired() {
        return (boolean) config.get("backpack.permissions.expand_backpacks");
    }

    /**
     * Sets whether players need specific permission to expand a backpack's size.
     * Controls who can expand backpacks.
     *
     * @param value {@code true} to require permission, {@code false} to allow anyone.
     * @throws IllegalArgumentException If the provided value is null.
     * @see #getPermissionToExpandBackpacksRequired()
     * @since 1.0.0
     */
    public static void setPermissionToExpandBackpacksRequired(Boolean value) {
        if (value != null) {
            config.put("backpack.permissions.expand_backpacks", value);
            save();
        } else {
            throw new IllegalArgumentException("Value cannot be null");
        }
    }
}
