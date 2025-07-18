package com.jasonkaranik.backpacksplus.guis;

import com.jasonkaranik.backpacksplus.Backpack;
import com.jasonkaranik.backpacksplus.BackpacksPlus;
import com.jasonkaranik.backpacksplus.Config;
import com.jasonkaranik.backpacksplus.enums.BackpackColor;
import com.jasonkaranik.backpacksplus.enums.Messages;
import com.jasonkaranik.backpacksplus.enums.Permissions;
import com.jasonkaranik.backpacksplus.utils.GUI;
import com.jasonkaranik.backpacksplus.utils.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BackpackCustomizerGUI extends GUI {
    private final Backpack backpack;

    public BackpackCustomizerGUI(Player player, Backpack backpack) {
        super(player, "Backpack Customizer", 9 * 3);

        this.backpack = backpack;
    }

    @Override
    public void onUpdate() {
        for (int i = 0; i < 27; i++) {
            if ((i >= 1 && i < 10) || i >= 17) {
                setClickableItem(i, PREMADE_ITEMS.BORDER, event -> true);
            }
        }

        setClickableItem(0, PREMADE_ITEMS.CLOSE, event -> close());

        BackpackColor color = backpack.getColor();
        ItemStack backpack_dye_placeholder_item = Utils.createItemStack(color.getDyeItem(), "&aDye Backpack", List.of("", Utils.colorify("&7Place a dye item here"), Utils.colorify("&7to change the backpack color"), "", Utils.colorify(String.format("&7Current color: %1$s", color.getHexColor() + color.getFriendlyName()))));
        ItemStack backpack_rename_placeholder_item = Utils.createItemStack(Material.NAME_TAG, "&aRename Backpack", List.of("", Utils.colorify("&7Place a name tag here"), Utils.colorify("&7to rename the backpack"), "", Utils.colorify(String.format("&7Current name: %1$s", backpack.getDisplayName()))));
        ItemStack backpack_expand_placeholder_item;
        if (backpack.canExpand()) {
            backpack_expand_placeholder_item = Utils.createItemStack(Material.RABBIT_HIDE, "&aExpand Backpack", new ArrayList<>(List.of("", Utils.colorify("&7Expand your backpack size"), Utils.colorify("&7to hold more items"), "", Utils.colorify(String.format("&7Current size: &e%1$s", backpack.getSize().getFriendlyName())), Utils.colorify(String.format("&7Next size: &e%1$s", backpack.getNextSize().getFriendlyName())), "", Utils.colorify("&7Required items:"))) {{
                addAll(backpack.createNextSizeExpansionCostLore());
            }});
        } else {
            backpack_expand_placeholder_item = PREMADE_ITEMS.BACKPACK_CUSTOMIZER_EXPAND_MAX_SIZE_REACHED;
        }

        setClickableItem(11, backpack_dye_placeholder_item, event -> {
            ItemStack cursorItem = event.getCursor();
            if (cursorItem != null && cursorItem.getType().toString().endsWith("_DYE")) {
                if (Config.getPermissionToDyeBackpacksRequired()) {
                    if (!Permissions.DYE_BACKPACK.check(player)) {
                        player.sendMessage(Messages.NO_PERMISSION.getMessage());
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                        return true;
                    }
                }

                BackpackColor currentColor = backpack.getColor();
                BackpackColor newColor = BackpackColor.getByDye(cursorItem.getType());
                if (!currentColor.name().equalsIgnoreCase(newColor.name())) {
                    if (cursorItem.getAmount() > 1) {
                        cursorItem.setAmount(cursorItem.getAmount() - 1);
                        player.setItemOnCursor(cursorItem);
                    } else {
                        player.setItemOnCursor(new ItemStack(Material.AIR));
                    }
                    backpack.setColor(newColor);
                    backpack.save();
                    player.sendMessage(String.format(Messages.DYE_SUCCESS.getMessage(), Utils.colorify(newColor.getHexColor() + newColor.getFriendlyName())));
                    player.playSound(player.getLocation(), Sound.ITEM_DYE_USE, 1.0F, 1.0F);
                    onUpdate();
                } else {
                    player.sendMessage(Messages.DYE_FAIL_SAME_COLOR.getMessage());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            } else {
                player.sendMessage(Messages.DYE_FAIL_INCORRECT_MATERIAL.getMessage());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
            return true;
        });

        setClickableItem(13, backpack_rename_placeholder_item, event -> {
            ItemStack cursorItem = event.getCursor();
            if (cursorItem != null && cursorItem.getType() == Material.NAME_TAG) {
                if (Config.getPermissionToRenameBackpacksRequired()) {
                    if (!Permissions.RENAME_BACKPACK.check(player)) {
                        player.sendMessage(Messages.NO_PERMISSION.getMessage());
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                        return true;
                    }
                }

                if (cursorItem.hasItemMeta()) {
                    ItemMeta meta = cursorItem.getItemMeta();
                    if (meta.hasDisplayName()) {
                        String newDisplayName = meta.getDisplayName();
                        if (!newDisplayName.startsWith("&")) newDisplayName = "&f" + newDisplayName;
                        int lengthOfColorCodes = (((Number) newDisplayName.chars().filter(c -> c == '&').count()).intValue() * 2);
                        if ((newDisplayName.length() - lengthOfColorCodes) <= 30) {
                            if (cursorItem.getAmount() > 1) {
                                cursorItem.setAmount(cursorItem.getAmount() - 1);
                                player.setItemOnCursor(cursorItem);
                            } else {
                                player.setItemOnCursor(new ItemStack(Material.AIR));
                            }
                            backpack.setDisplayName(newDisplayName);
                            backpack.save();
                            player.sendMessage(String.format(Messages.RENAME_SUCCESS.getMessage(), Utils.colorify(newDisplayName)));
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0F, 1.0F);
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
                player.sendMessage(Messages.RENAME_FAIL_INCORRECT_MATERIAL.getMessage());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
            return true;
        });

        setClickableItem(15, backpack_expand_placeholder_item, e -> {
            if (Config.getPermissionToExpandBackpacksRequired()) {
                if (!Permissions.EXPAND_BACKPACK.check(player)) {
                    player.sendMessage(Messages.NO_PERMISSION.getMessage());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    return true;
                }
            }

            if (backpack.canExpand()) {
                ItemStack[] expansion_cost = backpack.getNextSizeExpansionCost();
                if (Utils.hasAmountOfItems(player, expansion_cost)) {
                    if (Utils.removeAmountOfItems(player, expansion_cost)) {
                        backpack.expand();
                        backpack.save();
                        player.sendMessage(String.format(Messages.EXPAND_SUCCESS.getMessage(), backpack.getSize().getFriendlyName()));
                        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1.0F, 1.0F);
                        onUpdate();
                    } else {
                        player.sendMessage(Messages.COULD_NOT_REMOVE_ITEMS_FROM_INVENTORY.getMessage());
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    }
                } else {
                    player.sendMessage(Messages.EXPAND_FAIL_MISSING_ITEMS.getMessage());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            } else {
                player.sendMessage(Messages.EXPAND_FAIL_MAX_SIZE.getMessage());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
            return true;
        });
    }

    @Override
    public void onOpen() {

    }

    @Override
    public boolean onClick(InventoryClickEvent event) {
        if (!BackpacksPlus.isBackpackItem(player.getInventory().getItemInMainHand())) {
            player.sendMessage(Messages.BACKPACK_NOT_FOUND_IN_INVENTORY.getMessage());
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            close();
            return true;
        }

        int slot = event.getRawSlot();
        return slot == 10 || slot == 12 || slot == 14 || slot == 16;
    }

    @Override
    public void onClose() {
    }
}
