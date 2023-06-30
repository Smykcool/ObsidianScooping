package me.smykcool.obsidianscooping;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import world.bentobox.bentobox.BentoBox;

import java.sql.*;
import java.util.Set;

public final class ObsidianScooping extends JavaPlugin {

    private Database database;
    private Set<World> worlds;

    @Override
    public void onEnable() {

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        PluginManager pluginManager = getServer().getPluginManager();
        Plugin bentoBox = pluginManager.getPlugin("BentoBox");

        if (bentoBox == null) {
            getLogger().severe("Cannot find plugin BentoBox!");
            pluginManager.disablePlugin(this);
            return;
        }

        database = new Database(this);
        try {
            database.initialize();
            int purgeDays = getConfig().getInt("purgeDays");
            if (purgeDays != 0) {
                int deletedRows = database.purge(purgeDays);
                getLogger().info("Deleted " + deletedRows + " records from database older than " + purgeDays + " days");
            }
        }
        catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }

        worlds = BentoBox.getInstance().getIWM().getWorlds();
        pluginManager.registerEvents(new Events(this), this);
    }

    @Override
    public void onDisable() {
        try {
            if (database != null)
                database.closeConnection();
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Database getDatabase() {
        return database;
    }

    public Set<World> getWorlds() {
        return worlds;
    }
}