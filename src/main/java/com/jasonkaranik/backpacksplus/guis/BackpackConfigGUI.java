package com.jasonkaranik.backpacksplus.guis;

import com.jasonkaranik.backpacksplus.BackpacksPlus;
import com.jasonkaranik.backpacksplus.Config;
import com.jasonkaranik.backpacksplus.enums.BackpackColor;
import com.jasonkaranik.backpacksplus.enums.BackpackSize;
import com.jasonkaranik.backpacksplus.enums.Messages;
import com.jasonkaranik.backpacksplus.utils.GUI;
import com.jasonkaranik.backpacksplus.utils.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class BackpackConfigGUI extends GUI {

    public BackpackConfigGUI(Player player) {
        super(player, "Backpack Config", 9 * 4);
    }

    @Override
    public void onUpdate() {
        for (int i = 0; i < 36; i++) {
            if ((i >= 1 && i <= 9) || (i >= 17 && i <= 18) || i >= 26) {
                setClickableItem(i, PREMADE_ITEMS.BORDER, event -> true);
            }
        }

        setClickableItem(0, PREMADE_ITEMS.CLOSE, event -> close());

        BackpackColor color = Config.getDefaultBackpackColor();
        BackpackSize size = Config.getDefaultBackpackSize();

        ItemStack default_display_name_item = Utils.createItemStack(Material.NAME_TAG, "&aDefault Display Name", List.of("", Utils.colorify("&7Click while holding a name tag"), Utils.colorify("&7to change the default display name"), "", Utils.colorify(String.format("&7Current name: &e%1$s", Config.getDefaultBackpackDisplayName()))));
        ItemStack default_color_item = Utils.createItemStack(color.getDyeItem(), "&aDefault Color", new ArrayList<>(List.of("", Utils.colorify("&7Click to cycle through colors"), Utils.colorify("&7and set as the default"), "")) {{
            addAll(Arrays.stream(BackpackColor.values()).map(_color -> Utils.colorify((_color == color ? "&e&l-> " : "") + _color.getHexColor() + _color.getFriendlyName())).toList());
        }});
        ItemStack default_size_item = Utils.createItemStack(Material.RABBIT_HIDE, "&aDefault Size", new ArrayList<>(List.of("", Utils.colorify("&7Click to cycle through sizes"), Utils.colorify("&7and set as the default"), "")) {{
            addAll(Arrays.stream(BackpackSize.values()).map(_size -> Utils.colorify((_size == size ? "&e&l-> " : "") + "&e" + _size.getFriendlyName())).toList());
        }});
        ItemStack default_contents_item = Utils.createItemStack(Material.CHEST_MINECART, "&aDefault Contents", new ArrayList<>(List.of("", Utils.colorify("&7Place items in your inventory"), Utils.colorify("&7to set as default contents"), "", Utils.colorify("&7Current contents:"))) {{
            addAll(createDefaultContentsLore());
        }});

        setClickableItem(10, default_display_name_item, e -> {
            ItemStack mainItem = player.getInventory().getItemInMainHand();
            if (mainItem.getType() == Material.NAME_TAG) {
                if (mainItem.hasItemMeta()) {
                    ItemMeta meta = mainItem.getItemMeta();
                    if (meta.hasDisplayName()) {
                        String newDisplayName = meta.getDisplayName();
                        if (!newDisplayName.startsWith("&")) newDisplayName = "&f" + newDisplayName;
                        int lengthOfColorCodes = (((Number) newDisplayName.chars().filter(c -> c == '&').count()).intValue() * 2);
                        if ((newDisplayName.length() - lengthOfColorCodes) <= 30) {
                            Config.setDefaultBackpackDisplayName(newDisplayName);
                            player.sendMessage(String.format(Messages.SET_DEFAULT_DISPLAY_NAME_SUCCESS.getMessage(), Utils.colorify(newDisplayName)));
                            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F);
                            onUpdate();
                        } else {
                            player.sendMessage(Messages.RENAME_FAIL_NAME_TOO_LONG.getMessage());
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                        }
                    }
                } else {
                    player.sendMessage(Messages.RENAME_FAIL_NAME_TAG_NOT_RENAMED.getMessage());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            } else {
                player.sendMessage(Messages.SET_DISPLAY_NAME_FAIL_INCORRECT_MATERIAL.getMessage());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
            return true;
        });

        setClickableItem(12, default_color_item, e -> {
            BackpackColor next = BackpackColor.getNextColor(Config.getDefaultBackpackColor().name(), true);
            Config.setDefaultBackpackColor(next);
            player.sendMessage(String.format(Messages.SET_DEFAULT_COLOR_SUCCESS.getMessage(), Utils.colorify(next.getHexColor() + next.getFriendlyName())));
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F);
            onUpdate();
            return true;
        });

        setClickableItem(14, default_size_item, e -> {
            BackpackSize next = BackpackSize.getNextSize(Config.getDefaultBackpackSize().name(), true);
            Config.setDefaultBackpackSize(next);
            player.sendMessage(String.format(Messages.SET_DEFAULT_SIZE_SUCCESS.getMessage(), Utils.colorify(next.getFriendlyName())));
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F);
            onUpdate();
            return true;
        });

        setClickableItem(16, default_contents_item, e -> {
            Config.setDefaultBackpackContents(Arrays.stream(player.getInventory().getContents()).filter(item -> item != null && !BackpacksPlus.isBackpackItem(item)).toArray(ItemStack[]::new));
            player.sendMessage(Messages.SET_DEFAULT_CONTENTS_SUCCESS.getMessage());
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F);
            onUpdate();
            return true;
        });

        setClickableItem(19, createExpansionCostItem(BackpackSize.MEDIUM), e -> setHotbarItemsAsExpansionCost(BackpackSize.MEDIUM));
        setClickableItem(21, createExpansionCostItem(BackpackSize.LARGE), e -> setHotbarItemsAsExpansionCost(BackpackSize.LARGE));
        setClickableItem(23, createExpansionCostItem(BackpackSize.GREATER), e -> setHotbarItemsAsExpansionCost(BackpackSize.GREATER));
        setClickableItem(25, createExpansionCostItem(BackpackSize.JUMBO), e -> setHotbarItemsAsExpansionCost(BackpackSize.JUMBO));
    }

    private ItemStack createExpansionCostItem(BackpackSize size) {
        return Utils.createItemStack(Material.RABBIT_HIDE, String.format("&a%1$s Size Expansion Cost", size.getFriendlyName()), new ArrayList<>(List.of("", Utils.colorify("&7Set the items in your hotbar"), Utils.colorify("&7to define the upgrade cost"), Utils.colorify("&7for your backpack."), "", Utils.colorify(String.format("&7Upgrading to: &e%1$s", size.getFriendlyName())), "", Utils.colorify("&7Current cost:"))) {{
            addAll(size.getExpansionCostLore());
        }});
    }

    @Nonnull
    private List<String> createDefaultContentsLore() {
        List<String> list = new ArrayList<>();
        ItemStack[] contents = Config.getDefaultBackpackContents();
        for (ItemStack item : contents) {
            if (item != null && item.getType() != Material.AIR) {
                list.add(Utils.colorify(String.format("&8 - &a%1$s &8x%2$s", Utils.getItemName(item), item.getAmount())));
            }
        }
        return list;
    }

    private boolean setHotbarItemsAsExpansionCost(BackpackSize size) {
        Config.setBackpackSizeExpansionCost(size, IntStream.range(0, 9).mapToObj(i -> player.getInventory().getItem(i)).takeWhile(item -> item != null && item.getType() != Material.AIR && !BackpacksPlus.isBackpackItem(item)).toArray(ItemStack[]::new));
        player.sendMessage(String.format(Messages.SET_EXPANSION_COST_SUCCESS.getMessage(), size.getFriendlyName()));
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F);
        onUpdate();
        return true;
    }

    @Override
    public void onOpen() {

    }

    @Override
    public boolean onClick(InventoryClickEvent event) {
        return true;
    }

    @Override
    public void onClose() {

    }
}
