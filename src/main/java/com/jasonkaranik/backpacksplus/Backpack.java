package com.jasonkaranik.backpacksplus;

import com.jasonkaranik.backpacksplus.enums.BackpackColor;
import com.jasonkaranik.backpacksplus.enums.BackpackSize;
import com.jasonkaranik.backpacksplus.enums.Messages;
import com.jasonkaranik.backpacksplus.enums.Permissions;
import com.jasonkaranik.backpacksplus.guis.BackpackCustomizerGUI;
import com.jasonkaranik.backpacksplus.guis.BackpackGUI;
import com.jasonkaranik.backpacksplus.utils.ItemStackCodec;
import com.jasonkaranik.backpacksplus.utils.Utils;
import com.jasonkaranik.json.simple.extended.JSONObject;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a virtual backpack container that stores items and configuration data.
 * <p>
 * A Backpack is a customizable inventory container with properties including:
 * <ul>
 *   <li>Size - Determines inventory slot capacity</li>
 *   <li>Color - Visual appearance of the backpack</li>
 *   <li>Display Name - Custom name shown to players</li>
 *   <li>Contents - Stored items within the backpack</li>
 * </ul>
 * <p>
 * Each backpack is associated with a specific player (the holder) and can be accessed through
 * an in-game GUI. Backpacks can be expanded to larger sizes if configured to allow it.
 *
 * @see BackpackSize
 * @see BackpackColor
 * @see Config
 * @since 1.0.0
 */
public class Backpack {
    private final Player player;

    private final JSONObject data;

    private ItemStack item;

    private String id;

    private String display_name;

    private BackpackColor color;

    private BackpackSize size;

    private ItemStack[] contents;

    private long created_at;

    private long last_opened_at;

    /**
     * Creates a new Backpack instance for a specific player using saved NBT configuration data.
     * <p>
     * This constructor initializes all backpack properties including size, color, contents,
     * and metadata. If any properties are missing from the data, default values are applied.
     *
     * @param player The player who owns this backpack.
     * @param item   ItemStack containing backpack properties and contents.
     * @throws IllegalArgumentException If either the provided player or item parameter is null.
     */
    public Backpack(Player player, ItemStack item) {
        if (player != null && item != null) {
            this.player = player;
            this.item = item;
            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                data = container.has(BackpacksPlus.getBackpackDataKey(), PersistentDataType.STRING) ? new JSONObject(container.get(BackpacksPlus.getBackpackDataKey(), PersistentDataType.STRING)) : new JSONObject();
            } else {
                throw new IllegalArgumentException("Item has no metadata");
            }
            update();
        } else {
            throw new IllegalArgumentException("Player and Item cannot be null");
        }
    }

    /**
     * Updates the backpack's internal state and validates its configuration.
     * <p>
     * This method:
     * <ul>
     *   <li>Ensures all required properties exist, applying defaults if needed</li>
     *   <li>Validates and updates the backpack contents</li>
     *   <li>Adjusts contents if they exceed the current size limit</li>
     *   <li>Updates the physical item representation</li>
     *   <li>Maintains data consistency across all properties</li>
     * </ul>
     * <p>
     * If the backpack contents exceed the current size limit, the excess items
     * are preserved in the data under 'uncut_contents' for potential future manual restoration.
     *
     * @since 1.0.0
     */
    private void update() {
        addMissing("display_name", Config.getDefaultBackpackDisplayName());
        addMissing("color", Config.getDefaultBackpackColor().name());
        addMissing("size", Config.getDefaultBackpackSize().name());
        addMissing("contents", Config.getDefaultBackpackContentsSerialized());
        addMissing("created_at", System.currentTimeMillis());
        addMissing("last_opened_at", -1L);

        id = (String) data.get("id");

        display_name = (String) data.get("display_name");

        color = BackpackColor.getByName((String) data.get("color"));

        size = BackpackSize.getByName((String) data.get("size"));

        contents = ItemStackCodec.decode((String) data.get("contents"));

        created_at = ((Number) data.get("created_at")).longValue();

        last_opened_at = ((Number) data.get("last_opened_at")).longValue();

        if (getOccupiedSlotsNumber() > size.getInventorySlots()) {
            data.put("uncut_contents", ItemStackCodec.encode(contents));
            contents = Arrays.copyOfRange(contents, 0, size.getInventorySlots());
        }
    }

    /**
     * Adds a backpack data property if the key doesn't already exist in the data container.
     * <p>
     * This utility method is used during backpack initialization to populate missing
     * properties with their default values. If the specified key is already present in the
     * backpack's data, no action is taken to preserve existing backpack settings.
     * <p>
     * When a missing key is detected and added, the property is immediately available
     * for use by other backpack methods. This ensures that all backpack instances have
     * consistent data structure regardless of when they were created or last updated.
     *
     * @param key   The data property key to check and potentially add.
     * @param value The default value to set if the key is missing.
     * @since 1.0.0
     */
    private void addMissing(String key, Object value) {
        if (!data.containsKey(key)) {
            data.put(key, value);
        }
    }

    /**
     * Retrieves this backpack's raw configuration data.
     *
     * @return JSONObject containing all backpack properties and metadata.
     * @since 1.0.0
     */
    @Nonnull
    public JSONObject getJSONData() {
        return data;
    }

    /**
     * Retrieves the unique identifier for this backpack.
     * <p>
     * The ID is unique per backpack and is used for storage and retrieval.
     * It remains constant throughout the backpack's lifetime.
     *
     * @return The unique identifier of this backpack.
     * @since 1.0.0
     */
    public String getID() {
        return id;
    }

    /**
     * Retrieves the timestamp when this backpack was created.
     * <p>
     * This timestamp is set when the backpack is first created and
     * remains unchanged throughout its lifetime.
     *
     * @return Unix timestamp (milliseconds) of backpack creation.
     * @see #getLastOpenedTime()
     * @since 1.0.0
     */
    public long getCreationTime() {
        return created_at;
    }

    /**
     * Retrieves the timestamp of the last time this backpack was opened.
     * <p>
     * The timestamp is updated each time the backpack inventory is
     * successfully opened through the {@link #openContainer(Boolean)} method.
     *
     * @return Unix timestamp (milliseconds) of last access, or -1 if never opened.
     * @see #getCreationTime()
     * @since 1.0.0
     */
    public long getLastOpenedTime() {
        return last_opened_at;
    }

    /**
     * Retrieves the custom display name of the backpack.
     * <p>
     * This name is shown in the inventory GUI and on the physical item.
     * It may contain color codes and formatting codes.
     *
     * @return The configured display name of the backpack.
     * @see #setDisplayName(String)
     * @since 1.0.0
     */
    @Nonnull
    public String getDisplayName() {
        return display_name;
    }

    /**
     * Updates the backpack's display name.
     *
     * @param newName The new display name to set.
     * @throws IllegalArgumentException If the provided name is null.
     * @see #getDisplayName()
     * @since 1.0.0
     */
    public void setDisplayName(String newName) {
        if (newName != null) {
            data.put("display_name", newName);
            update();
        } else {
            throw new IllegalArgumentException("Name cannot be null");
        }
    }

    /**
     * Retrieves the current color of the backpack.
     * <p>
     * The color determines the appearance of the physical backpack item
     * through its associated texture ID.
     *
     * @return The current BackpackColor enum value.
     * @see #setColor(BackpackColor)
     * @since 1.0.0
     */
    @Nonnull
    public BackpackColor getColor() {
        return color;
    }

    /**
     * Updates the backpack's color.
     * <p>
     * Changes the appearance of the physical backpack item by updating
     * its texture based on the new color's texture ID.
     *
     * @param newColor The new BackpackColor enum value to apply.
     * @throws IllegalArgumentException If the provided color is null.
     * @see #getColor()
     * @since 1.0.0
     */
    public void setColor(BackpackColor newColor) {
        if (newColor != null) {
            data.put("color", newColor.name());
            update();
        } else {
            throw new IllegalArgumentException("Color cannot be null");
        }
    }

    /**
     * Retrieves the current size of the backpack.
     * <p>
     * The size determines the available inventory slots of the physical backpack item.
     *
     * @return The current BackpackSize enum value.
     * @see #setSize(BackpackSize)
     * @since 1.0.0
     */
    @Nonnull
    public BackpackSize getSize() {
        return size;
    }

    /**
     * Updates the backpack's size.
     * <p>
     * Changes the size of the physical backpack item by updating
     * the available inventory slots based on the new size's available inventory slots.
     *
     * @param newSize The new BackpackSize enum value to apply.
     * @throws IllegalArgumentException If the provided size is null.
     * @see #getSize()
     * @since 1.0.0
     */
    public void setSize(BackpackSize newSize) {
        if (newSize != null) {
            data.put("size", newSize.name());
            update();
        } else {
            throw new IllegalArgumentException("Size cannot be null");
        }
    }

    /**
     * Retrieves the current contents of the backpack.
     *
     * @return Array of ItemStacks representing the backpack contents.
     * @see #setContents(ItemStack[])
     * @since 1.0.0
     */
    @Nonnull
    public ItemStack[] getContents() {
        return contents;
    }

    /**
     * Updates the entire contents of the backpack.
     *
     * @param newContents Array of ItemStacks to store in the backpack.
     * @throws IllegalArgumentException       If the provided contents array is null.
     * @throws ArrayIndexOutOfBoundsException If the contents exceed the backpack's size.
     * @see #getContents()
     * @since 1.0.0
     */
    public void setContents(ItemStack[] newContents) {
        if (contents != null) {
            if (Utils.getItemStackArraySize(newContents) <= size.getInventorySlots()) {
                data.put("contents", ItemStackCodec.encode(newContents));
                update();
            } else {
                throw new ArrayIndexOutOfBoundsException("Contents array is larger than the backpack's size");
            }
        } else {
            throw new IllegalArgumentException("Contents cannot be null");
        }
    }

    /**
     * Generates a formatted list of the backpack's contents for display.
     * <p>
     * This method produces a condensed summary of items stored in the backpack
     * for display in the item's lore. It shows up to 5 individual items with
     * their quantities, followed by a summary line if there are additional items.
     * <p>
     * The format follows: "&amp;8 - [Item Name] &amp;8x[Quantity]"
     *
     * @return List of formatted strings describing contents.
     * @since 1.0.0
     */
    public List<String> createContentsLore() {
        List<String> list = new ArrayList<>();
        if (getOccupiedSlotsNumber() > 0) {
            list.addAll(List.of("", Utils.colorify("&7Contents:")));
            for (ItemStack item : contents) {
                if (list.size() < 7) {
                    if (item != null && item.getType() != Material.AIR) {
                        list.add(Utils.colorify(String.format("&8 - %1$s &8x%2$s", Utils.getItemName(item), item.getAmount())));
                    }
                } else {
                    list.add(Utils.colorify("&8&o and more.."));
                    break;
                }
            }
        }
        return list;
    }

    /**
     * Calculates the number of inventory slots containing items.
     *
     * @return Number of slots containing non-null items.
     * @since 1.0.0
     */
    public int getOccupiedSlotsNumber() {
        return Utils.getItemStackArraySize(contents);
    }

    /**
     * Finds the slot number where this backpack is located in the player's inventory.
     * <p>
     * This method scans through the player's inventory to locate the physical
     * backpack item that corresponds to this Backpack instance by comparing IDs.
     * It's useful for inventory manipulation operations.
     *
     * @return The inventory slot index where this backpack is located, or -1 if not found.
     * @throws IllegalStateException If the player is offline.
     * @since 1.0.0
     */
    public int getItemSlotNumberInInventory() {
        if (player.isOnline()) {
            ItemStack[] plr_contents = player.getInventory().getContents();
            for (int i = 0; i < plr_contents.length; i++) {
                ItemStack it = plr_contents[i];

                if (BackpacksPlus.isBackpackItem(it)) {
                    Backpack bp = new Backpack(player, it);
                    if (getID().equals(bp.getID())) {
                        return i;
                    }
                }
            }
            return -1;
        } else {
            throw new IllegalStateException("Player is offline");
        }
    }

    /**
     * Retrieves the next available backpack size in the progression sequence.
     *
     * @return The next BackpackSize in the sequence, or {@code null} if this is already the largest available size.
     * @see BackpackSize#getNextSize(String, Boolean)
     * @since 1.0.0
     */
    @Nullable
    public BackpackSize getNextSize() {
        return BackpackSize.getNextSize(size.name(), false);
    }

    /**
     * Retrieves the items required to expand to the next size.
     * <p>
     * This method returns the specific items and quantities needed to upgrade
     * the backpack to the next available size tier. These items are typically
     * consumed during the expansion process.
     *
     * @return Array of required ItemStacks, or null if expansion is unavailable.
     * @see #canExpand()
     * @see #createNextSizeExpansionCostLore()
     * @since 1.0.0
     */
    @Nullable
    public ItemStack[] getNextSizeExpansionCost() {
        if (canExpand()) {
            return getNextSize().getExpansionCost();
        }
        return null;
    }

    /**
     * Creates a formatted description of expansion requirements to expand to the next size.
     * <p>
     * The returned list includes:
     * <ul>
     *   <li>Names and quantities of required items</li>
     *   <li>Any additional requirements or conditions</li>
     * </ul>
     *
     * @return List of formatted strings describing expansion cost, or null if at maximum size.
     * @see #canExpand()
     * @see #getNextSizeExpansionCost()
     * @since 1.0.0
     */
    @Nullable
    public List<String> createNextSizeExpansionCostLore() {
        if (canExpand()) {
            return getNextSize().getExpansionCostLore();
        }
        return null;
    }

    /**
     * Checks if the backpack can be expanded to a larger size.
     *
     * @return {@code true} if a larger size is available, {@code false} if at maximum size.
     * @since 1.0.0
     */
    public boolean canExpand() {
        return getNextSize() != null;
    }

    /**
     * Creates or updates the ItemStack representation of this backpack.
     * <p>
     * This method synchronizes the backpack's internal state with its physical
     * ItemStack representation in the player's inventory. It updates the item's
     * display name, lore, metadata, and NBT data to reflect current properties.
     * <p>
     * The method locates the backpack in the player's inventory using the
     * {@link #getItemSlotNumberInInventory()} method and replaces it with the
     * updated version.
     *
     * @throws IllegalStateException If the player is offline.
     * @since 1.0.0
     */
    public void save() {
        if (player.isOnline()) {
            int currentSlot = getItemSlotNumberInInventory();
            if (currentSlot > -1) {
                item = new ItemStack(item) {{
                    SkullMeta meta = Utils.applySkinTexture((SkullMeta) getItemMeta(), color.getTextureID());
                    meta.setDisplayName(Utils.colorify(display_name));
                    meta.setLore(new ArrayList<>(List.of(Utils.colorify(String.format("&7%1$s Backpack (%2$s Slots)", size.getFriendlyName(), size.getInventorySlots())), "", Utils.colorify("&e&lLeft/Right-Click &eto open"), Utils.colorify("&e&lShift-Left-Click &eto customize"))) {{
                        addAll(createContentsLore());
                    }});
                    meta.getPersistentDataContainer().set(BackpacksPlus.getBackpackDataKey(), PersistentDataType.STRING, getJSONData().toJSONString());
                    setItemMeta(meta);
                }};

                player.getInventory().setItem(currentSlot, item);
            }
        } else {
            throw new IllegalStateException("Player is offline");
        }
    }

    /**
     * Attempts to expand the backpack to the next available size.
     * <p>
     * This method upgrades the backpack to the next size tier if available.
     * It does not verify or consume the expansion cost items - that validation
     * is performed separately by the customization GUI before calling this method.
     * <p>
     * After expansion, the backpack's storage capacity is increased according
     * to the new size's specifications, allowing more items to be stored.
     *
     * @return {@code true} if expansion was successful, {@code false} if no larger size is available.
     * @see #canExpand()
     * @since 1.0.0
     */
    public boolean expand() {
        if (canExpand()) {
            setSize(getNextSize());
            return true;
        }
        return false;
    }

    /**
     * Opens the backpack inventory GUI for the owner.
     * <p>
     * This method displays the backpack's contents in a custom inventory GUI,
     * allowing the player to add, remove, or rearrange items. It updates the
     * last opened timestamp and handles permission verification.
     *
     * @param ignorePerm If {@code true}, bypasses permission checks entirely. If {@code false}, enforces the configured permission requirements. If {@code null}, behaves the same as {@code false}.
     * @throws IllegalStateException If the player is offline.
     * @since 1.0.0
     */
    public void openContainer(Boolean ignorePerm) {
        if (player.isOnline()) {
            if (ignorePerm != null && !ignorePerm) {
                if (Config.getPermissionToOpenBackpacksRequired()) {
                    if (!Permissions.OPEN_BACKPACK.check(player)) {
                        player.sendMessage(Messages.NO_PERMISSION.getMessage());
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                        return;
                    }
                }
            }

            data.put("last_opened_at", System.currentTimeMillis());
            update();
            new BackpackGUI(player, this).open();
        } else {
            throw new IllegalStateException("Player is offline");
        }
    }

    /**
     * Opens the backpack customizer GUI for the owner.
     * <p>
     * The customizer GUI allows:
     * <ul>
     *   <li>Changing the display name</li>
     *   <li>Modifying the color</li>
     *   <li>Expanding the size (if available)</li>
     * </ul>
     *
     * @throws IllegalStateException If the player is offline.
     * @since 1.0.0
     */
    public void openCustomizer() {
        if (player.isOnline()) {
            new BackpackCustomizerGUI(player, this).open();
        } else {
            throw new IllegalStateException("Player is offline");
        }
    }
}
