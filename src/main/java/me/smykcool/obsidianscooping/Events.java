package me.smykcool.obsidianscooping;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import world.bentobox.bentobox.BentoBox;

import java.sql.SQLException;
import java.util.Date;

public class Events implements Listener {

    private final ObsidianScooping plugin;
    private final FileConfiguration config;

    public Events(ObsidianScooping plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @EventHandler
    public void onObsidianCreate(BlockFormEvent e) {
        BlockState b = e.getNewState();
        if (b.getType() != Material.OBSIDIAN)
            return;
        if (!plugin.getWorlds().contains(b.getWorld()))
            return;
        try {
            plugin.getDatabase().blockSave(b.getBlock(), new Date());
        }
        catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onBucketClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        Block b = e.getClickedBlock();
        Player p = e.getPlayer();
        if (!plugin.getWorlds().contains(b.getWorld()) || b.getType() != Material.OBSIDIAN ||
                p.getInventory().getItemInMainHand().getType() != Material.BUCKET ||
                !p.hasPermission(config.getString("usePermission")))
            return;

        if (!BentoBox.getInstance().getIslandsManager().locationIsOnIsland(p, b.getLocation()) &&
                !p.hasPermission(config.getString("bypassPermission")))
            return;
        try {
            if (!plugin.getDatabase().blockExists(b))
                return;
            ItemStack lavaBucket = new ItemStack(Material.LAVA_BUCKET, 1);
            plugin.getDatabase().blockDelete(b);
            p.getInventory().removeItem(new ItemStack(Material.BUCKET, 1));
            if (!p.getInventory().addItem(lavaBucket).isEmpty())
                p.getWorld().dropItemNaturally(p.getLocation(), lavaBucket);
            b.setType(Material.AIR);
            e.setCancelled(true);
        }
        catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (b.getType() != Material.OBSIDIAN || !plugin.getWorlds().contains(b.getWorld()))
            return;
        try {
            if (!plugin.getDatabase().blockExists(b))
                return;
            plugin.getDatabase().blockDelete(b);
        }
        catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }
}