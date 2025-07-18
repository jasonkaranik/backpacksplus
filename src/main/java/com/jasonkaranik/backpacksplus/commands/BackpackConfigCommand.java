package com.jasonkaranik.backpacksplus.commands;

import com.jasonkaranik.backpacksplus.Config;
import com.jasonkaranik.backpacksplus.enums.Messages;
import com.jasonkaranik.backpacksplus.enums.Permissions;
import com.jasonkaranik.backpacksplus.guis.BackpackConfigGUI;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackpackConfigCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (Config.getPermissionToOpenBackpackConfigMenuRequired()) {
                if (!Permissions.OPEN_BACKPACK_CONFIG_MENU.check(player)) {
                    player.sendMessage(Messages.NO_PERMISSION.getMessage());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    return true;
                }
            }

            new BackpackConfigGUI(player).open();
        }
        return true;
    }
}
