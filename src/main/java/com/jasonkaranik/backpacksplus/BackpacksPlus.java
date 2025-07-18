package com.jasonkaranik.backpacksplus;

import com.jasonkaranik.backpacksplus.commands.BackpackConfigCommand;
import com.jasonkaranik.backpacksplus.enums.BackpackColor;
import com.jasonkaranik.backpacksplus.utils.Utils;
import com.jasonkaranik.json.simple.extended.JSONObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

/**
 * Main plugin class for BackpacksPlus - A comprehensive storage solution plugin for Minecraft servers that adds fully customizable backpacks to enhance your players' inventory management experience.
 * <p>
 * This class manages the plugin lifecycle and provides central access to:
 * <ul>
 *   <li>Configuration management</li>
 *   <li>Backpack storage and retrieval</li>
 *   <li>Custom crafting recipes</li>
 *   <li>Permission systems</li>
 * </ul>
 * <p>
 * The plugin adds customizable backpacks that players can craft, upgrade, and use
 * to store items. Backpacks support different sizes, colors, and custom names,
 * with data persistence across server restarts.
 *
 * @since 1.0.0
 */
public final class BackpacksPlus extends JavaPlugin {
    private static NamespacedKey backpack_data_key;
    private static BackpacksPlus plugin;

    /**
     * Creates a new backpack item instance.
     * <p>
     * This method returns a fresh copy of the base backpack item with a unique id.
     *
     * @return A new ItemStack representing a backpack.
     * @since 1.0.0
     */
    public static ItemStack getNewBackpackItem() {
        return new ItemStack(Material.PLAYER_HEAD) {{
            ItemMeta meta = Utils.applySkinTexture((SkullMeta) getItemMeta(), Config.getDefaultBackpackColor().getTextureID());
            meta.setDisplayName(Utils.colorify(Config.getDefaultBackpackDisplayName()));
            JSONObject data = new JSONObject();
            data.put("id", UUID.randomUUID().toString());
            meta.getPersistentDataContainer().set(backpack_data_key, PersistentDataType.STRING, data.toJSONString());
            setItemMeta(meta);
        }};
    }

    /**
     * Retrieves the plugin instance.
     * <p>
     * This method provides access to the main plugin instance,
     * allowing interaction with other plugin components and systems.
     *
     * @return The BackpacksPlus plugin instance.
     * @since 1.0.0
     */
    public static BackpacksPlus getPlugin() {
        return plugin;
    }

    /**
     * Retrieves the NamespacedKey used for backpack data storage.
     * <p>
     * This key is used to identify and access backpack data stored in
     * item meta's PersistentDataContainer.
     *
     * @return The NamespacedKey for backpack data.
     * @see org.bukkit.persistence.PersistentDataContainer
     * @since 1.0.0
     */
    public static NamespacedKey getBackpackDataKey() {
        return backpack_data_key;
    }

    /**
     * Determines if an ItemStack represents a backpack item.
     * <p>
     * Checks for the material type and presence of a specific persistent data tag
     * that identifies backpack items.
     *
     * @param item The ItemStack to check.
     * @return {@code true} if the item is a backpack, {@code false} otherwise.
     * @since 1.0.0
     */
    public static boolean isBackpackItem(ItemStack item) {
        if (item != null) {
            if (item.getType() == Material.PLAYER_HEAD) {
                if (item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    return meta.getPersistentDataContainer().has(backpack_data_key, PersistentDataType.STRING);
                }
            }
        }
        return false;
    }

    @Override
    public void onEnable() {
        plugin = this;

        backpack_data_key = new NamespacedKey(this, "backpack_data");

        UpdateChecker.check();

        if (!this.getDataFolder().exists()) {
            try {
                if (!this.getDataFolder().mkdir()) {
                    Utils.logSevereErrorAndShutdown("Couldn't create plugin data folder: Permission denied");
                }
            } catch (SecurityException e) {
                Utils.logSevereErrorAndShutdown(String.format("Couldn't create plugin data folder: %1$s", e.getMessage()));
            }
        }

        Config.update();

        ItemStack backpack_item = new ItemStack(Material.PLAYER_HEAD) {{
            ItemMeta meta = Utils.applySkinTexture((SkullMeta) getItemMeta(), BackpackColor.BROWN.getTextureID());
            meta.setDisplayName(Utils.colorify("&aBackpack"));
            meta.getPersistentDataContainer().set(backpack_data_key, PersistentDataType.STRING, "{}");
            setItemMeta(meta);
        }};

        ShapedRecipe backpack_item_recipe = new ShapedRecipe(new NamespacedKey(this, "backpack"), backpack_item);
        backpack_item_recipe.shape(Config.getFirstCraftingRecipeRow(), Config.getSecondCraftingRecipeRow(), Config.getThirdCraftingRecipeRow());
        backpack_item_recipe.setGroup("misc");
        for (Object o : Config.getCraftingRecipeIngredients().entrySet()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) o;
            backpack_item_recipe.setIngredient(entry.getKey().charAt(0), Material.getMaterial(entry.getValue()));
        }
        Bukkit.addRecipe(backpack_item_recipe);

        this.getCommand("backpackconfig").setExecutor(new BackpackConfigCommand());

        Bukkit.getPluginManager().registerEvents(new Listeners(), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
    }
}
