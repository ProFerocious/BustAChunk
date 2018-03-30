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

import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class BustAChunk extends JavaPlugin {
    private boolean lockdown;
    private String itemName;
    private List<String> lore;
    private String loreIdentifier;
    private boolean allowAlly;
    private boolean allowEnemy;
    private boolean allowNeutral;
    private boolean allowOwn;
    private boolean allowSafezone;
    private boolean allowTruce;
    private boolean allowWarzone;
    private boolean allowWilderness;
    private String messageUse;
    private String messageDeny;
    private String messageDenyLockdown;
    private String messageGiveTargetNeeded;
    private String messageAlready;
    private String messageLockdownOn;
    private String messageLockdownOff;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getCommand("bustachunk").setExecutor(new BustACommand(this));
        this.getServer().getPluginManager().registerEvents(new BustAListen(this), this);

        this.itemName = this.loadString("item.display", "&bBust a Chunk!");
        this.loreIdentifier = this.loadString("item.lore-identifier", "Busts Chunks!");
        this.lore = this.loadLore("item.lore", new String[]{"&3An ancient weapon", "&3Destroys everything below it when placed in a chunk"});

        this.allowAlly = this.getConfig().getBoolean("allowed.ally", false);
        this.allowEnemy = this.getConfig().getBoolean("allowed.enemy", false);
        this.allowNeutral = this.getConfig().getBoolean("allowed.neutral", false);
        this.allowOwn = this.getConfig().getBoolean("allowed.owen", true);
        this.allowSafezone = this.getConfig().getBoolean("allowed.safezone", false);
        this.allowTruce = this.getConfig().getBoolean("allowed.truce", false);
        this.allowWarzone = this.getConfig().getBoolean("allowed.warzone", false);
        this.allowWilderness = this.getConfig().getBoolean("allowed.wilderness", true);

        this.messageAlready = this.loadString("messages.already", "&cOnly one Bust a Chunk can be active in a chunk at once!");
        this.messageDeny = this.loadString("message.deny", "&cYou cannot place Bust a Chunk in this chunk!");
        this.messageDenyLockdown = this.loadString("message.deny-lockdown", "&cBust a Chunk is temporarily disabled!");
        this.messageGiveTargetNeeded = this.loadString("message.give-target-needed", "&cNeed a valid target to give to!");
        this.messageLockdownOff = this.loadString("messages.lockdown-off", "&bBust a Chunk now enabled!");
        this.messageLockdownOn = this.loadString("messages.lockdown-on", "&cBust a Chunk temporarily disabled!");
        this.messageUse = this.loadString("messages.use", "&cTime to bust a chunk! Be careful! The glass layers may last only ten seconds!");
    }

    private String loadString(String name, String defaultText) {
        return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString(name, defaultText));
    }

    private List<String> loadLore(String name, String[] defaultText) {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        if (this.getConfig().contains(name)) {
            for (String s : this.getConfig().getStringList(name)) {
                builder.add(ChatColor.translateAlternateColorCodes('&', s));
            }
        } else {
            for (String s : defaultText) {
                builder.add(ChatColor.translateAlternateColorCodes('&', s));
            }
        }
        builder.add(this.loreIdentifier);
        return builder.build();
    }

    public boolean isLockdown() {
        return this.lockdown;
    }

    public void flipLockdown() {
        this.lockdown = !this.lockdown;
        this.getServer().broadcast(this.lockdown ? this.getMessageLockdownOn() : this.getMessageLockdownOff(), "bustachunk.lockdownnotice");
    }

    public String getItemName() {
        return this.itemName;
    }

    public List<String> getLore() {
        return this.lore;
    }

    public String getLoreIdentifier() {
        return this.loreIdentifier;
    }

    public boolean isAllowAlly() {
        return this.allowAlly;
    }

    public boolean isAllowEnemy() {
        return this.allowEnemy;
    }

    public boolean isAllowNeutral() {
        return this.allowNeutral;
    }

    public boolean isAllowOwn() {
        return this.allowOwn;
    }

    public boolean isAllowSafezone() {
        return this.allowSafezone;
    }

    public boolean isAllowTruce() {
        return this.allowTruce;
    }

    public boolean isAllowWarzone() {
        return this.allowWarzone;
    }

    public boolean isAllowWilderness() {
        return this.allowWilderness;
    }

    public String getMessageUse() {
        return this.messageUse;
    }

    public String getMessageDeny() {
        return this.messageDeny;
    }

    public String getMessageDenyLockdown() {
        return this.messageDenyLockdown;
    }

    public String getMessageGiveTargetNeeded() {
        return this.messageGiveTargetNeeded;
    }

    public String getMessageAlready() {
        return this.messageAlready;
    }

    public String getMessageLockdownOn() {
        return this.messageLockdownOn;
    }

    public String getMessageLockdownOff() {
        return this.messageLockdownOff;
    }
}
