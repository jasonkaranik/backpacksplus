package com.jasonkaranik.backpacksplus.utils;

import com.jasonkaranik.backpacksplus.BackpacksPlus;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class GUI {
    private final Map<Integer, Function<InventoryClickEvent, Boolean>> clickable_items = new HashMap<>();

    public Player player;
    public Inventory inv;
    private Listener listener;

    private long lastClick = 0L;

    public GUI(Player player, String title, Integer size) {
        if (player != null && title != null && size != null) {
            if (!title.isBlank()) {
                if (size > 0 && size % 9 == 0 && size <= 54) {
                    this.player = player;
                    this.inv = Bukkit.createInventory(null, size, title);
                } else {
                    throw new IllegalArgumentException("Size must be a positive multiple of 9 and not exceed 54");
                }
            } else {
                throw new IllegalArgumentException("Title cannot be blank");
            }
        } else {
            throw new IllegalArgumentException("Player, title, and size cannot be null");
        }
    }

    public abstract void onOpen();

    public abstract void onUpdate();

    public abstract boolean onClick(InventoryClickEvent event);

    public abstract void onClose();

    private void clear() {
        if (listener != null) {
            HandlerList.unregisterAll(listener);
            listener = null;
        }
    }

    private boolean canClick() {
        return (System.currentTimeMillis() - lastClick) > 250L;
    }

    public void setClickableItem(Integer slot, ItemStack item, Function<InventoryClickEvent, Boolean> callback) {
        if (slot != null && item != null && callback != null) {
            if (slot >= 0 && slot < inv.getSize()) {
                inv.setItem(slot, item);
                clickable_items.put(slot, callback);
            } else {
                throw new IllegalArgumentException(String.format("Slot must be within inventory bounds: %1$s", slot));
            }
        } else {
            throw new IllegalArgumentException("Slot, item, and callback cannot be null");
        }
    }

    public boolean close() {
        new BukkitRunnable() {
            public void run() {
                player.closeInventory();
            }
        }.runTaskLater(BackpacksPlus.getPlugin(), 1L);
        return true;
    }

    public void open() {
        if (listener == null) {
            listener = new Listener() {
                @EventHandler(priority = EventPriority.HIGHEST)
                private void onInventoryClick(InventoryClickEvent event) {
                    if (event.getInventory().equals(inv) && event.getWhoClicked().getUniqueId().equals(player.getUniqueId())) {
                        if (event.getClick() == ClickType.SWAP_OFFHAND) {
                            player.getInventory().setItemInOffHand(null);
                            event.setCancelled(true);
                            return;
                        }

                        if (BackpacksPlus.isBackpackItem(Utils.getAssociatedItemFromClickEvent(event))) {
                            event.setCancelled(true);
                            return;
                        }

                        if (canClick()) {
                            lastClick = System.currentTimeMillis();

                            int slot = event.getRawSlot();
                            if (clickable_items.containsKey(slot)) {
                                event.setCancelled(clickable_items.get(slot).apply(event));
                            } else {
                                event.setCancelled(onClick(event));
                            }
                            return;
                        }
                        event.setCancelled(true);
                    }
                }

                @EventHandler(priority = EventPriority.HIGHEST)
                private void onInventoryOpen(InventoryOpenEvent event) {
                    if (event.getInventory().equals(inv) && event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                        onOpen();
                    }
                }

                @EventHandler(priority = EventPriority.HIGHEST)
                private void onInventoryDrag(InventoryDragEvent event) {
                    if (event.getInventory().equals(inv) && event.getWhoClicked().getUniqueId().equals(player.getUniqueId())) {
                        event.setCancelled(true);
                    }
                }

                @EventHandler(priority = EventPriority.HIGHEST)
                private void onInventoryClose(InventoryCloseEvent event) {
                    if (event.getInventory().equals(inv) && event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                        onClose();
                        clear();
                    }
                }
            };

            Bukkit.getPluginManager().registerEvents(listener, BackpacksPlus.getPlugin());

            onUpdate();

            new BukkitRunnable() {
                public void run() {
                    player.openInventory(inv);
                }
            }.runTaskLater(BackpacksPlus.getPlugin(), 1L);
        } else {
            throw new IllegalStateException("GUI has already been initialized (opened)");
        }
    }

    public static class PREMADE_ITEMS {
        // NAVIGATION ITEMS
        public static ItemStack NAVIGATION_BACKWARDS_II = Utils.createSkullItemStack("816ea34a6a6ec5c051e6932f1c471b7012b298d38d179f1b487c413f51959cd4", "&aFirst Backpack", List.of(Utils.colorify("&7Go to the first backpack")));
        public static ItemStack NAVIGATION_BACKWARDS_I = Utils.createSkullItemStack("8652e2b936ca8026bd28651d7c9f2819d2e923697734d18dfdb13550f8fdad5f", "&aPrevious Backpack", List.of(Utils.colorify("&7Go to the previous backpack")));
        public static ItemStack NAVIGATION_FORWARDS_I = Utils.createSkullItemStack("2a3b8f681daad8bf436cae8da3fe8131f62a162ab81af639c3e0644aa6abac2f", "&aNext Backpack", List.of(Utils.colorify("&7Go to the next backpack")));
        public static ItemStack NAVIGATION_FORWARDS_II = Utils.createSkullItemStack("9c9ec71c1068ec6e03d2c9287f9da9193639f3a635e2fbd5d87c2fabe6499", "&aLast Backpack", List.of(Utils.colorify("&7Go to the last backpack")));
        public static ItemStack CLOSE = Utils.createItemStack(Material.BARRIER, "&cClose menu", List.of());

        // MISC
        public static ItemStack BORDER = Utils.createItemStack(Material.BLACK_STAINED_GLASS_PANE, "", List.of());
        public static ItemStack BACKPACK_CUSTOMIZER_EXPAND_MAX_SIZE_REACHED = Utils.createItemStack(Material.RED_STAINED_GLASS_PANE, "&cMax Size Reached", List.of("", Utils.colorify("&7Your backpack has reached"), Utils.colorify("&7the maximum size and"), Utils.colorify("&7cannot be expanded further")));
    }
}
