package com.jasonkaranik.backpacksplus.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Utility class for converting Bukkit ItemStack arrays to and from Base64-encoded strings.
 * <p>
 * This class provides methods to encode ItemStacks into a storable string format
 * and decode them back into usable ItemStack arrays. The encoding process
 * preserves all item metadata including:
 * <ul>
 *   <li>Item attributes and modifiers</li>
 *   <li>Custom NBT data</li>
 *   <li>Enchantments</li>
 *   <li>Display names and lore</li>
 *   <li>Durability and damage values</li>
 * </ul>
 * <p>
 * The encoded format uses Base64 encoding to ensure the resulting string is safe
 * for storage in databases and configuration files.
 *
 * @since 1.0.0
 */
public class ItemStackCodec {
    /**
     * Converts an array of ItemStacks into a Base64-encoded string representation.
     * <p>
     * This method handles the encoding process by:
     * <ol>
     *   <li>Writing the array length as an integer</li>
     *   <li>Writing each ItemStack object sequentially</li>
     *   <li>Encoding the resulting byte array in Base64</li>
     * </ol>
     *
     * @param obj The ItemStack array to encode.
     * @return A Base64-encoded string containing the encoded items, or an empty string if the input is null or an error occurs during encoding. The empty string return value allows for safe decoding attempts.
     * @see #decode(String)
     * @since 1.0.0
     */
    public static String encode(ItemStack[] obj) {
        try {
            if (obj != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

                dataOutput.writeInt(obj.length);

                for (ItemStack itemStack : obj) {
                    dataOutput.writeObject(itemStack);
                }

                dataOutput.close();
                return Base64Coder.encodeLines(outputStream.toByteArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encode(new ItemStack[]{});
    }

    /**
     * Converts a Base64-encoded string back into an array of ItemStacks.
     * <p>
     * This method handles the decoding process by:
     * <ol>
     *   <li>Decoding the Base64 string into bytes</li>
     *   <li>Reading the array length integer</li>
     *   <li>Reading each ItemStack object sequentially</li>
     * </ol>
     *
     * @param str The Base64-encoded string to decode.
     * @return An array of ItemStacks containing the decode items. Returns an empty array if the input is null or an error occurs during decoding. Never returns null to ensure safety in calling code.
     * @see #encode(ItemStack[])
     * @since 1.0.0
     */
    public static ItemStack[] decode(String str) {
        try {
            if (str != null) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(str));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                ItemStack[] items = new ItemStack[dataInput.readInt()];

                for (int i = 0; i < items.length; i++) {
                    items[i] = (ItemStack) dataInput.readObject();
                }

                dataInput.close();
                return items;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ItemStack[]{};
    }
}
