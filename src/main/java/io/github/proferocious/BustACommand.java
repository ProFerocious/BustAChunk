/*
This file is part of Bust a Chunk.

Bust a Chunk is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Bust a Chunk is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Bust a Chunk.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.proferocious;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class BustACommand implements CommandExecutor {
    private final BustAChunk plugin;

    public BustACommand(BustAChunk plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && "give".equalsIgnoreCase(args[0]) && sender.hasPermission("bustachunk.give")) {
            Player target;
            if (args.length == 1) {
                if (sender instanceof Player) {
                    target = ((Player) sender);
                } else {
                    sender.sendMessage(this.plugin.getMessageGiveTargetNeeded());
                    return true;
                }
            } else {
                target = this.plugin.getServer().getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(this.plugin.getMessageGiveTargetNeeded());
                    return true;
                }
            }

            ItemStack itemStack = new ItemStack(Material.ENDER_PORTAL_FRAME, 1, (short) 0);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(this.plugin.getItemName());
            itemMeta.setLore(this.plugin.getLore());
            itemStack.setItemMeta(itemMeta);

            target.getInventory().addItem(itemStack);
        } else if (args.length > 0 && "lockdown".equalsIgnoreCase(args[0]) && sender.hasPermission("bustachunk.lockdown")) {
            this.plugin.flipLockdown();
        } else {
            sender.sendMessage(ChatColor.AQUA + "Bust a Chunk!");
            if (this.plugin.isLockdown()) {
                sender.sendMessage(ChatColor.RED + "  Currently in lockdown!");
            }
            if (sender.hasPermission("bustachunk.give")) {
                sender.sendMessage(ChatColor.AQUA + "/" + label + " give <player>");
            }
            if (sender.hasPermission("bustachunk.lockdown")) {
                sender.sendMessage(ChatColor.AQUA + "/" + label + " lockdown");
            }
        }
        return true;
    }
}
