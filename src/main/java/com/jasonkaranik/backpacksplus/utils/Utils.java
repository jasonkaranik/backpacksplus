package com.jasonkaranik.backpacksplus.utils;

import com.jasonkaranik.backpacksplus.BackpacksPlus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class providing helper methods for the BackpacksPlus plugin.
 * <p>
 * This class contains utilities for:
 * <ul>
 *   <li>Color code conversion</li>
 *   <li>Inventory management</li>
 *   <li>Item name formatting</li>
 *   <li>Backpack-specific operations</li>
 *   <li>Player inventory manipulation</li>
 *   <li>Skull texture management</li>
 * </ul>
 *
 * @see BackpacksPlus
 * @since 1.0.0
 */
public class Utils {
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    /**
     * Converts Minecraft color codes in a string from '&amp;' notation to the internal format.
     *
     * @param msg The string containing color codes with '&amp;' notation.
     * @return The string with converted Minecraft color codes.
     * @throws IllegalArgumentException If the provided input string is null.
     * @see ChatColor#translateAlternateColorCodes(char, String)
     * @since 1.0.0
     */
    @Nonnull
    public static String colorify(String msg) {
        if (msg != null) {
            Matcher matcher = HEX_PATTERN.matcher(msg);
            while (matcher.find()) {
                String hexCode = matcher.group();
                net.md_5.bungee.api.ChatColor color = net.md_5.bungee.api.ChatColor.of(hexCode);
                msg = msg.replace(hexCode, color.toString());
            }

            return ChatColor.translateAlternateColorCodes('&', msg);
        } else {
            throw new IllegalArgumentException("Message cannot be null");
        }
    }

    /**
     * Logs a severe error message and initiates server shutdown.
     * <p>
     * This method performs two actions:
     * <ul>
     *   <li>Logs the provided message at SEVERE level using the Plugin's logger</li>
     *   <li>Triggers an immediate server shutdown</li>
     * </ul>
     * This should only be used for critical errors that make it impossible
     * for the plugin to function safely.
     *
     * @param msg The error message to log before shutdown.
     * @see Bukkit#shutdown()
     * @since 1.0.0
     */
    public static void logSevereErrorAndShutdown(String msg) {
        if (msg != null) {
            BackpacksPlus.getPlugin().getLogger().severe(msg);
        }
        Bukkit.shutdown();
    }

    /**
     * Retrieves the ItemStack involved in an inventory click event.
     * <p>
     * This method handles two scenarios:
     * <ul>
     *   <li>Normal clicks: Returns the currently clicked item</li>
     *   <li>Number key presses: Returns the item in the corresponding hotbar slot</li>
     * </ul>
     *
     * @param event The InventoryClickEvent to analyze.
     * @return The ItemStack involved in the click event, or null if no item was involved.
     * @throws IllegalArgumentException If the provided event is null.
     * @see InventoryClickEvent#getCurrentItem()
     * @see InventoryClickEvent#getHotbarButton()
     * @since 1.0.0
     */
    @Nullable
    public static ItemStack getAssociatedItemFromClickEvent(InventoryClickEvent event) {
        if (event != null) {
            return event.getClick() == ClickType.NUMBER_KEY ? event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) : event.getCurrentItem();
        } else {
            throw new IllegalArgumentException("Event cannot be null");
        }
    }

    /**
     * Calculates the number of non-empty slots in an ItemStack array.
     * <p>
     * A slot is considered occupied if it contains a non-null item that isn't AIR.
     * This is useful for determining how many actual items are in an inventory
     * section.
     *
     * @param array The ItemStack array to analyze.
     * @return The number of slots containing actual items.
     * @throws IllegalArgumentException If the provided array is null.
     * @since 1.0.0
     */
    public static int getItemStackArraySize(ItemStack[] array) {
        return ((Number) Arrays.stream(array).filter(item -> item != null && item.getType() != Material.AIR).count()).intValue();
    }

    /**
     * Formats a Material enum name into a readable display name.
     * <p>
     * Example: DIAMOND_SWORD becomes "Diamond Sword".
     *
     * @param material The Material to format.
     * @return The formatted display name.
     * @throws IllegalArgumentException If the provided material is null.
     * @since 1.0.0
     */
    @Nonnull
    public static String getDefaultMaterialName(Material material) {
        if (material != null) {
            String str = material.toString().replace("_", " ").toLowerCase();
            String[] words = str.split(" ");
            StringBuilder result = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
                }
            }
            return result.toString().trim();
        } else {
            throw new IllegalArgumentException("Material cannot be null");
        }
    }

    /**
     * Gets the display name of an ItemStack, using custom name if available.
     * <p>
     * Priority order:
     * <br>
     * 1. Custom display name if set
     * <br>
     * 2. Default material name if no custom name
     *
     * @param item The ItemStack to get the name from.
     * @return The item's display name.
     * @throws IllegalArgumentException If the provided item is null.
     * @see #getDefaultMaterialName(Material)
     * @since 1.0.0
     */
    @Nonnull
    public static String getItemName(ItemStack item) {
        if (item != null) {
            String name = null;
            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName()) {
                    name = meta.getDisplayName();
                }
            }
            return name != null ? name : getDefaultMaterialName(item.getType());
        } else {
            throw new IllegalArgumentException("Item cannot be null");
        }
    }

    /**
     * Creates an ItemStack with a specified material, display name, and lore.
     * <p>
     * This method constructs a new ItemStack with the given material and applies
     * custom display name and lore text. The display name will be automatically
     * color-coded using the color codes from the '&amp;' notation.
     *
     * @param material    The Minecraft material type for the item.
     * @param displayName The display name of the item (will be color-coded).
     * @param lore        List of lore lines to add to the item.
     * @return A configured ItemStack with the specified properties.
     * @throws IllegalArgumentException If the provided material, display name or lore are null.
     * @since 1.0.0
     */
    @Nonnull
    public static ItemStack createItemStack(Material material, String displayName, List<String> lore) {
        if (material != null & displayName != null && lore != null) {
            return new ItemStack(material) {{
                ItemMeta meta = getItemMeta();
                meta.setDisplayName(colorify(displayName));
                meta.setLore(lore);
                setItemMeta(meta);
            }};
        } else {
            throw new IllegalArgumentException("Material, display name and lore cannot be null");
        }
    }

    /**
     * Creates a custom Player Head ItemStack with a specific skin texture, display name, and lore.
     *
     * @param texture     The base64-encoded skin texture string.
     * @param displayName The display name of the skull item (will be color-coded).
     * @param lore        List of lore lines to add to the skull item.
     * @return A configured Player Head ItemStack with custom skin and properties.
     * @throws IllegalArgumentException If the provided texture, display name or lore are null.
     * @since 1.0.0
     */
    @Nonnull
    public static ItemStack createSkullItemStack(String texture, String displayName, List<String> lore) {
        if (texture != null & displayName != null && lore != null) {
            return new ItemStack(Material.PLAYER_HEAD) {{
                ItemMeta meta = applySkinTexture((SkullMeta) getItemMeta(), texture);
                meta.setDisplayName(colorify(displayName));
                meta.setLore(lore);
                setItemMeta(meta);
            }};
        } else {
            throw new IllegalArgumentException("Texture, display name and lore cannot be null");
        }
    }

    /**
     * Applies a custom skin texture to a player head item.
     * <p>
     * Uses Minecraft's texture system to apply a custom texture to
     * a skull item, typically used for backpack appearances.
     * <p>
     * The texture parameter should be the texture ID hash from Minecraft's
     * texture database, not the full URL or base64 encoded string.
     * The method will construct the proper URL format internally.
     *
     * @param meta    The SkullMeta to modify.
     * @param texture The texture ID to apply.
     * @return The modified SkullMeta.
     * @throws IllegalArgumentException If the provided meta or texture are null.
     * @throws IllegalStateException    If the provided texture URL is invalid.
     * @since 1.0.0
     */
    @Nonnull
    public static SkullMeta applySkinTexture(SkullMeta meta, String texture) {
        if (meta != null && texture != null) {
            PlayerProfile playerProfile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = playerProfile.getTextures();
            try {
                textures.setSkin(URI.create(String.format("http://textures.minecraft.net/texture/%1$s", texture)).toURL());
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
            playerProfile.setTextures(textures);
            meta.setOwnerProfile(playerProfile);
            return meta;
        } else {
            throw new IllegalArgumentException("Skull Meta and Texture cannot be null");
        }
    }

    /**
     * Checks if a player has sufficient quantities of specific items.
     * <p>
     * Items are matched using the isSimilar() method, which ensures that not only
     * the material type matches, but also metadata like durability, enchantments,
     * and custom NBT data.
     *
     * @param player The player to check.
     * @param items  Array of ItemStacks representing required items and quantities.
     * @return {@code true} if player has all required items, {@code false} otherwise.
     * @throws IllegalArgumentException If the provided player or items array is null.
     * @see #removeAmountOfItems(Player, ItemStack[])
     * @since 1.0.0
     */
    public static boolean hasAmountOfItems(Player player, ItemStack[] items) {
        if (player != null && items != null) {
            int totalFound = 0;
            for (ItemStack i : items) {
                if (i != null && i.getType() != Material.AIR) {
                    int totalFound2 = 0;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.isSimilar(i)) {
                            totalFound2 += item.getAmount();
                            if (totalFound2 >= i.getAmount()) {
                                totalFound++;
                                break;
                            }
                        }
                    }
                }
            }
            return totalFound == items.length;
        }
        return false;
    }

    /**
     * Removes specific quantities of items from a player's inventory.
     * <p>
     * The method first checks if the player has all the required items using
     * {@link #hasAmountOfItems(Player, ItemStack[])} before attempting removal.
     * It then iterates through the inventory, removing items that match the
     * specified ItemStacks until the required quantities are met.
     *
     * @param player The player to remove items from.
     * @param items  Array of ItemStacks to remove.
     * @return {@code true} if all items were removed successfully.
     * @throws IllegalArgumentException If the provided player or items array is null.
     * @see #hasAmountOfItems(Player, ItemStack[])
     * @since 1.0.0
     */
    public static boolean removeAmountOfItems(Player player, ItemStack[] items) {
        if ((player != null && items != null) && (hasAmountOfItems(player, items))) {
            int totalRemoved = 0;
            for (ItemStack i : items) {
                if (i != null && i.getType() != Material.AIR) {
                    int remainingToRemove = i.getAmount();
                    Inventory inventory = player.getInventory();

                    for (int slot = 0; slot < inventory.getSize(); slot++) {
                        ItemStack item = inventory.getItem(slot);
                        if (item != null && item.isSimilar(i)) {
                            if (item.getAmount() > remainingToRemove) {
                                item.setAmount(item.getAmount() - remainingToRemove);
                                totalRemoved++;
                                break;
                            } else {
                                remainingToRemove -= item.getAmount();
                                inventory.setItem(slot, new ItemStack(Material.AIR));
                                if (remainingToRemove == 0) {
                                    totalRemoved++;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            new BukkitRunnable() {
                public void run() {
                    player.updateInventory();
                }
            }.runTaskLater(BackpacksPlus.getPlugin(), 1L);
            return totalRemoved == items.length;
        }
        return false;
    }
}
