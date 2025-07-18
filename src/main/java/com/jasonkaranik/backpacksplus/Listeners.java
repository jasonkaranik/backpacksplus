package com.jasonkaranik.backpacksplus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Listeners implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem()) {
            if (BackpacksPlus.isBackpackItem(event.getItem())) {
                Player player = event.getPlayer();
                ItemStack item = event.getItem();
                Action action = event.getAction();

                if (!BackpacksPlus.isBackpackItem(player.getInventory().getItemInOffHand())) {
                    // LEFT_CLICK_AIR is called when dropping items for some reason, so that's why it's not being used.
                    if ((action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) || (!player.isSneaking() && action == Action.LEFT_CLICK_BLOCK)) {
                        new Backpack(player, item).openContainer(false);
                    } else if (player.isSneaking() && action == Action.LEFT_CLICK_BLOCK) {
                        new Backpack(player, item).openCustomizer();
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (BackpacksPlus.isBackpackItem(event.getInventory().getResult())) {
            event.getInventory().setResult(BackpacksPlus.getNewBackpackItem());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPrepareAnvil(PrepareAnvilEvent event) {
        if (BackpacksPlus.isBackpackItem(event.getResult())) {
            event.setResult(null);
        }
    }
}
