package com.winthier.lightswitch;

import com.winthier.claims.Claims;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class LightSwitchPlugin extends JavaPlugin implements Listener {
    private Set<Block> blocks = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    final BlockFace[] dirs = {BlockFace.DOWN, BlockFace.UP, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH};
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (!event.getPlayer().hasPermission("lightswitch.switch")) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        final Block block = event.getClickedBlock();
        if (block.getType() != Material.REDSTONE_LAMP_OFF) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) return;
        if (event.getPlayer().getInventory().getItemInOffHand().getType() != Material.AIR) return;
        if (getServer().getPluginManager().getPlugin("Claims") != null && !Claims.getInstance().canBuild(event.getPlayer().getUniqueId(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ())) return;
        Block neighbor = null;
        for (BlockFace dir: dirs) {
            Block nbor = block.getRelative(dir);
            if (nbor.getType() == Material.AIR) {
                neighbor = nbor;
                break;
            }
        }
        if (neighbor == null) return;
        blocks.add(block);
        event.setCancelled(true);
        BlockState state = neighbor.getState();
        neighbor.setType(Material.REDSTONE_BLOCK);
        state.update(true, false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockRedstone(BlockRedstoneEvent event)
    {
        Block block = event.getBlock();
        if (block.getType() != Material.REDSTONE_LAMP_ON) return;
        if (!blocks.contains(block)) return;
        event.setNewCurrent(15);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event)
    {
        blocks.remove(event.getBlock());
    }
}
