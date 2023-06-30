package me.smykcool.obsidianscooping;

import org.bukkit.block.Block;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Database {

    private final ObsidianScooping plugin;
    private Connection connection;

    public Database(ObsidianScooping plugin) {
        this.plugin = plugin;
    }

    public Connection getConnection() throws SQLException, ClassNotFoundException {

        if (connection != null)
            return connection;

        Class.forName("org.sqlite.JDBC");
        File file = new File(plugin.getDataFolder(), plugin.getConfig().getString("database"));
        connection = DriverManager.getConnection("jdbc:sqlite:" + file);

        return connection;
    }

    public void initialize() throws SQLException, ClassNotFoundException {

        Statement statement = getConnection().createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS locations (w TEXT, x INT, y INT, z INT, dt TEXT)");
        statement.close();
    }

    public void closeConnection() throws SQLException {
        if (connection != null)
            connection.close();
    }

    public void blockSave(Block block, Date date) throws SQLException, ClassNotFoundException {
        PreparedStatement statement = getConnection().prepareStatement("INSERT OR REPLACE INTO locations (w, x, y, z, dt) VALUES (?, ?, ?, ?, ?)");
        statement.setString(1, block.getWorld().getName());
        statement.setInt(2, block.getX());
        statement.setInt(3, block.getY());
        statement.setInt(4, block.getZ());
        statement.setString(5, new SimpleDateFormat("yyyy-MM-dd").format(date));
        statement.executeUpdate();
        statement.close();
    }

    public boolean blockExists(Block block) throws SQLException, ClassNotFoundException {
        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM locations WHERE w = ? AND x = ? AND y = ? AND z = ?");
        statement.setString(1, block.getWorld().getName());
        statement.setInt(2, block.getX());
        statement.setInt(3, block.getY());
        statement.setInt(4, block.getZ());
        ResultSet result = statement.executeQuery();
        boolean exists = result.next();
        statement.close();
        return exists;
    }

    public void blockDelete(Block block) throws SQLException, ClassNotFoundException {
        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM locations WHERE w = ? AND x = ? AND y = ? AND z = ?");
        statement.setString(1, block.getWorld().getName());
        statement.setInt(2, block.getX());
        statement.setInt(3, block.getY());
        statement.setInt(4, block.getZ());
        statement.executeUpdate();
        statement.close();
    }

    public int purge(int days) throws SQLException, ClassNotFoundException {
        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM locations WHERE DATE(dt) < DATE('now', -? || ' day')");
        statement.setInt(1, days);
        int deletedRows = statement.executeUpdate();
        statement.close();
        return deletedRows;
    }
}
