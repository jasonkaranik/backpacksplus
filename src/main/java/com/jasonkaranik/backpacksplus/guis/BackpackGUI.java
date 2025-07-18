package com.jasonkaranik.backpacksplus.guis;

import com.jasonkaranik.backpacksplus.Backpack;
import com.jasonkaranik.backpacksplus.BackpacksPlus;
import com.jasonkaranik.backpacksplus.enums.Messages;
import com.jasonkaranik.backpacksplus.utils.GUI;
import com.jasonkaranik.backpacksplus.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class BackpackGUI extends GUI {
    private final Backpack backpack;

    public BackpackGUI(Player player, Backpack backpack) {
        super(player, Utils.colorify(backpack.getDisplayName()), backpack.getSize().getInventorySlots() + 9);

        this.backpack = backpack;
    }

    @Override
    public void onUpdate() {
        for (int i = 1; i < 5; i++) {
            setClickableItem(i, PREMADE_ITEMS.BORDER, event -> true);
        }

        setClickableItem(0, PREMADE_ITEMS.CLOSE, event -> close());

        setClickableItem(5, PREMADE_ITEMS.NAVIGATION_BACKWARDS_II, event -> goToBackpack(getNextBackpack(-1)));
        setClickableItem(6, PREMADE_ITEMS.NAVIGATION_BACKWARDS_I, event -> goToBackpack(getPreviousBackpack(backpack.getItemSlotNumberInInventory())));
        setClickableItem(7, PREMADE_ITEMS.NAVIGATION_FORWARDS_I, event -> goToBackpack(getNextBackpack(backpack.getItemSlotNumberInInventory())));
        setClickableItem(8, PREMADE_ITEMS.NAVIGATION_FORWARDS_II, event -> goToBackpack(getPreviousBackpack(player.getInventory().getSize())));

        ItemStack[] contents = backpack.getContents();
        for (int i = 0; i < contents.length; i++) {
            try {
                inv.setItem(i + 9, contents[i]);
            } catch (Exception ignored) {
            }
        }
    }

    private ItemStack getNextBackpack(int startIndex) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = startIndex + 1; i < contents.length; i++) {
            if (BackpacksPlus.isBackpackItem(contents[i])) {
                return contents[i];
            }
        }
        return null;
    }

    private ItemStack getPreviousBackpack(int startIndex) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = startIndex - 1; i >= 0; i--) {
            if (BackpacksPlus.isBackpackItem(contents[i])) {
                return contents[i];
            }
        }
        return null;
    }

    private boolean goToBackpack(ItemStack it) {
        if (it != null) {
            Backpack new_backpack = new Backpack(player, it);
            if (!new_backpack.getID().equals(backpack.getID())) {
                new_backpack.openContainer(false);
            }
        }
        return true;
    }

    @Override
    public void onOpen() {
        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1.0F, 1.0F);
    }

    @Override
    public boolean onClick(InventoryClickEvent event) {
        if (!BackpacksPlus.isBackpackItem(player.getInventory().getItemInMainHand())) {
            player.sendMessage(Messages.BACKPACK_NOT_FOUND_IN_INVENTORY.getMessage());
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
            close();
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        backpack.setContents(Arrays.copyOfRange(inv.getContents(), 9, inv.getSize()));
        backpack.save();
    }
}
