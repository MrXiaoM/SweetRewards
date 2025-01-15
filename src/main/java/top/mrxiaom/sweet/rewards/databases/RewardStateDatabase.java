package top.mrxiaom.sweet.rewards.databases;

import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.sweet.rewards.SweetRewards;
import top.mrxiaom.sweet.rewards.func.AbstractPluginHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RewardStateDatabase extends AbstractPluginHolder implements IDatabase {
    private String table;
    public RewardStateDatabase(SweetRewards plugin) {
        super(plugin);
    }

    @Override
    public void reload(Connection conn, String prefix) throws SQLException {
        table = prefix + "state";
        try (PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE if NOT EXISTS `" + table + "`(" +
                        "`player` VARCHAR(48)," +
                        "`state` VARCHAR(48)" +
                ");")) {
            ps.execute();
        }
    }

    public Map<String, Boolean> checkStates(Player player, List<String> keys) {
        try (Connection conn = plugin.getConnection()) {
            Map<String, Boolean> states = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            String playerKey = plugin.key(player);
            for (String key : keys) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM `" + table + "` WHERE `player`=? AND `state`=?"
                )) {
                    ps.setString(1, playerKey);
                    ps.setString(2, key);
                    try (ResultSet result = ps.executeQuery()) {
                        states.put(key, result.next());
                    }
                }
            }
            return states;
        } catch (SQLException e) {
            warn(e);
            return null;
        }
    }

    public void markState(Player player, String key) {
        try (Connection conn = plugin.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO `" + table + "`(`player`,`state`) VALUES(?,?);"
            )) {
            ps.setString(1, plugin.key(player));
            ps.setString(2, key);
            ps.execute();
        } catch (SQLException e) {
            warn(e);
        }
    }
}
