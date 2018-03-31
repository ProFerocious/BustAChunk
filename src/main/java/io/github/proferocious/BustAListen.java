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

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public final class BustAListen implements Listener {
    private final BustAChunk plugin;
    private final Set<Chunk> activeChunks = new HashSet<>();

    public BustAListen(BustAChunk plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlace(final BlockPlaceEvent event) {
        if (!event.getItemInHand().hasItemMeta()) {
            return;
        }
        if (!event.getItemInHand().getItemMeta().hasLore()) {
            return;
        }
        List<String> lore = event.getItemInHand().getItemMeta().getLore();
        if (!lore.get(lore.size() - 1).equalsIgnoreCase(this.plugin.getLoreIdentifier())) {
            return;
        }
        if (this.plugin.isLockdown()) {
            event.getPlayer().sendMessage(this.plugin.getMessageDenyLockdown());
            event.setCancelled(true);
            return;
        }
        final Chunk chunk = event.getBlock().getChunk();
        int chunkX = chunk.getX() * 16 + 8;
        int chunkZ = chunk.getZ() * 16 + 8;
        if (this.activeChunks.contains(chunk)) {
            event.getPlayer().sendMessage(this.plugin.getMessageAlready());
            event.setCancelled(true);
            return;
        }
        final FPlayer player = FPlayers.getInstance().getByPlayer(event.getPlayer());
        final Faction blockFaction = Board.getInstance().getFactionAt(new FLocation(event.getBlock().getLocation()));
        Relation relation = blockFaction.getRelationTo(player);
        if ((!this.plugin.isAllowAlly() && relation == Relation.ALLY) ||
                (!this.plugin.isAllowEnemy() && relation == Relation.ENEMY) ||
                (!this.plugin.isAllowNeutral() && blockFaction.isNormal() && relation == Relation.NEUTRAL) ||
                (!this.plugin.isAllowOwn() && relation == Relation.MEMBER) ||
                (!this.plugin.isAllowTruce() && relation == Relation.TRUCE) ||
                (!this.plugin.isAllowSafezone() && Factions.getInstance().getSafeZone().equals(blockFaction)) ||
                (!this.plugin.isAllowWarzone() && Factions.getInstance().getWarZone().equals(blockFaction)) ||
                (!this.plugin.isAllowWilderness() && Factions.getInstance().getWilderness().equals(blockFaction))
                ) {
            event.getPlayer().sendMessage(this.plugin.getMessageDeny());
            event.setCancelled(true);
            return;
        }

        this.activeChunks.add(chunk);

        Set<ArmorStand> stands = new HashSet<>();

        int startingHeight = event.getBlockPlaced().getY() - 1;
        final Queue<Block> blocks = new LinkedList<>();

        this.processBlocks(chunk, startingHeight, blocks::add);
        for (int y = startingHeight + 10 - (startingHeight % 10); y >= 0; y -= 10) {
            Location location = new Location(chunk.getWorld(), chunkX, y, chunkZ);
            ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);
            armorStand.setCanPickupItems(false);
            armorStand.setCustomNameVisible(true);
            armorStand.setGravity(false);
            armorStand.setVisible(false);

            armorStand.setCustomName(BustAListen.this.plugin.getItemName());
            stands.add(armorStand);
        }

        this.plugin.getServer().getOnlinePlayers().stream().filter((p) -> ((event.getBlock().getWorld() == p.getWorld()) && (Math.abs(p.getLocation().getBlockX() - event.getBlock().getX()) <= 50) && (Math.abs(p.getLocation().getBlockZ() - event.getBlock().getZ()) <= 50))).forEachOrdered((p) -> {
            p.sendMessage(this.plugin.getMessageUse());
        });

        BukkitRunnable killBlock = new BukkitRunnable() {
            @Override
            public void run() {
                event.getBlock().setType(Material.AIR);
            }
        };
        killBlock.runTaskLater(this.plugin, 1L);

        final BukkitRunnable cleanup = new BukkitRunnable() {
            @Override
            public void run() {
                BustAListen.this.processBlocks(chunk, startingHeight, block -> block.setType(Material.AIR));
                BustAListen.this.activeChunks.remove(chunk);
                stands.forEach(ArmorStand::remove);
            }
        };

        BukkitRunnable countdown = new BukkitRunnable() {
            private int seconds = 10;

            @Override
            public void run() {
                for (ArmorStand armorStand : stands) {
                    armorStand.setCustomName(this.seconds + "");
                }
                if (--this.seconds < 0) {
                    this.cancel();
                    cleanup.runTask(BustAListen.this.plugin);
                }
            }
        };

        final BukkitRunnable buster = new BukkitRunnable() {
            @Override
            public void run() {
                for (int x = 0; x < 300; x++) {
                    Block block = blocks.poll();
                    if (block == null) {
                        this.cancel();
                        countdown.runTaskTimer(BustAListen.this.plugin, 1L, 20L);
                        break;
                    }
                    block.setType((block.getY() % 10 == 0) ? Material.GLASS : Material.AIR);
                }
            }
        };
        buster.runTaskTimer(this.plugin, 1, 10L);
    }

    private void processBlocks(Chunk chunk, int startingHeight, Consumer<Block> consumer) {
        Block block;
        for (int y = startingHeight; y >= 0; y--) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    block = chunk.getBlock(x, y, z);
                    if (!(block.getType() == Material.AIR) && !(block.getType() == Material.BEDROCK)) {
                        consumer.accept(block);
                    }
                }
            }
        }
    }
}
