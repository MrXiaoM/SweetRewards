package top.mrxiaom.sweet.rewards.databases;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.sweet.rewards.SweetRewards;
import top.mrxiaom.sweet.rewards.func.AbstractPluginHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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
                        "`id` VARCHAR(48)," +
                        "`state` VARCHAR(48)" +
                ");")) {
            ps.execute();
        }
    }

    public Map<Character, Boolean> checkStates(Player player, String id, List<Character> keys) {
        try (Connection conn = plugin.getConnection()) {
            Map<Character, Boolean> states = new HashMap<>();
            String playerKey = plugin.key(player);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM `" + table + "` WHERE `player`=? AND `id`=?"
            )) {
                ps.setString(1, playerKey);
                ps.setString(2, id);
                try (ResultSet result = ps.executeQuery()) {
                    while (result.next()) {
                        String state = result.getString("state");
                        if (state.isEmpty()) continue;
                        char keyInDb = state.charAt(0);
                        if (keys.contains(keyInDb)) {
                            states.put(keyInDb, true);
                        }
                    }
                    for (Character key : keys) {
                        if (!states.containsKey(key)) {
                            states.put(key, false);
                        }
                    }
                }
            }
            return states;
        } catch (SQLException e) {
            warn(e);
            return null;
        }
    }

    public void markState(Player player, String id, char key) {
        try (Connection conn = plugin.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO `" + table + "`(`player`,`id`,`state`) VALUES(?,?,?);"
            )) {
            ps.setString(1, plugin.key(player));
            ps.setString(2, id);
            ps.setString(3, String.valueOf(key));
            ps.execute();
        } catch (SQLException e) {
            warn(e);
        }
    }

    public void resetStates(@Nullable OfflinePlayer player, String id) {
        try (Connection conn = plugin.getConnection();
            PreparedStatement ps = conn.prepareStatement(player == null
                    ? ("DELETE FROM `" + table + "` WHERE `id`=?;")
                    : ("DELETE FROM `" + table + "` WHERE `player`=? AND `id`=?;")
            )) {
            if (player == null) {
                ps.setString(1, id);
            } else {
                ps.setString(1, plugin.key(player));
                ps.setString(2, id);
            }
            ps.execute();
        } catch (SQLException e) {
            warn(e);
        }
    }
}
