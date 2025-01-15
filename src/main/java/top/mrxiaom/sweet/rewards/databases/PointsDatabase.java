package top.mrxiaom.sweet.rewards.databases;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.pluginbase.utils.Bytes;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.rewards.SweetRewards;
import top.mrxiaom.sweet.rewards.func.AbstractPluginHolder;
import top.mrxiaom.sweet.rewards.func.entry.PointType;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

public class PointsDatabase extends AbstractPluginHolder implements IDatabase, Listener {
    private final Map<String, PointType> pointTypeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, Map<String, Long>> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, Long> cooldown = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public PointsDatabase(SweetRewards plugin) {
        super(plugin);
        registerBungee();
        register();
    }

    @Override
    public void reload(Connection conn, String prefix) throws SQLException {
        FileConfiguration config = plugin.getConfig();
        pointTypeMap.clear();
        for (String s : config.getStringList("points-folders")) {
            File folder = s.startsWith("./") ? new File(plugin.getDataFolder(), s.substring(2)) : new File(s);
            if (!folder.exists()) {
                Util.mkdirs(folder);
                if (s.equals("./points")) {
                    plugin.saveResource("points/default.yml", new File(folder, "default.yml"));
                }
            }
            Util.reloadFolder(folder, false, (id, file) -> {
                if (pointTypeMap.containsKey(id)) {
                    warn("[points] 重复的点数ID: " + id);
                    return;
                }
                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                pointTypeMap.put(id, PointType.load(cfg, id, prefix));
            });
        }
        info("[points] 加载了 " + pointTypeMap.size() + " 种点数");
        for (PointType point : pointTypeMap.values()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "CREATE TABLE if NOT EXISTS `" + point.table + "`(" +
                      "`player` VARCHAR(48) PRIMARY KEY," +
                      "`point` BIGINT" +
                    ");")) {
                ps.execute();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(PlayerJoinEvent e) {
        cache.remove(plugin.key(e.getPlayer()));
    }
    @EventHandler
    public void on(PlayerKickEvent e) {
        cache.remove(plugin.key(e.getPlayer()));
    }
    @EventHandler
    public void on(PlayerQuitEvent e) {
        cache.remove(plugin.key(e.getPlayer()));
    }

    @Override
    public void receiveBungee(String subChannel, DataInputStream in) throws IOException {
        if (subChannel.equals("SWEETREWARDS_DELETE_CACHE")) {
            String key = in.readUTF();
            long now = System.currentTimeMillis();
            long next = cooldown.getOrDefault(key, now);
            if (now >= next) { // 以免 BungeeCord 特有的一下一堆消息涌入，做个冷却
                cooldown.put(key, now + 3000L);
                cache.remove(key);
            }
        }
    }

    public void sendDeleteCache(String key) {
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null) {
            ByteArrayDataOutput out = Bytes.newDataOutput();
            out.writeUTF("Forward");
            out.writeUTF("ALL");
            out.writeUTF("SWEETREWARDS_DELETE_CACHE");
            try (ByteArrayOutputStream output = new ByteArrayOutputStream();
                DataOutputStream msg = new DataOutputStream(output)
            ) {
                msg.writeUTF(key);

                byte[] bytes = output.toByteArray();
                out.writeShort(bytes.length);
                out.write(bytes);
                player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            } catch (IOException e) {
                warn(e);
            }
        }
    }

    @Nullable
    public PointType get(String id) {
        return pointTypeMap.get(id);
    }

    private Map<String, Long> cache(String key) {
        Map<String, Long> map = cache.get(key);
        if (map == null) {
            Map<String, Long> cacheMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            cache.put(key, cacheMap);
            return cacheMap;
        }
        return map;
    }
    public long getPoints(PointType type, OfflinePlayer player) {
        String key = plugin.key(player);
        Map<String, Long> cacheMap = cache(key);
        Long cacheValue = cacheMap.get(type.id);
        if (cacheValue != null) return cacheValue;
        try (Connection conn = plugin.getConnection()) {
            long point = getPoints(conn, type.table, key, type.initialValue);
            cacheMap.put(type.id, point);
            return point;
        } catch (SQLException e) {
            warn(e);
        }
        return type.initialValue;
    }
    private long getPoints(Connection conn, String table, String key, long def) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM `" + table + "` WHERE `player`=?;"
        )) {
            try (ResultSet resultSet = ps.executeQuery()) {
                if (!resultSet.next()) return def;
                return resultSet.getLong("point");
            }
        }
    }
    public Long addPoints(PointType type, OfflinePlayer player, long toAdd) {
        try (Connection conn = plugin.getConnection()) {
            String key = plugin.key(player);
            long point = getPoints(conn, type.table, key, type.initialValue);
            long newPoint = point + toAdd;
            setPoints(conn, type.id, type.table, key, newPoint);
            return newPoint;
        } catch (SQLException e) {
            warn(e);
            return null;
        }
    }
    public Long setPoints(PointType type, OfflinePlayer player, long point) {
        try (Connection conn = plugin.getConnection()) {
            setPoints(conn, type.id, type.table, plugin.key(player), point);
            return point;
        } catch (SQLException e) {
            warn(e);
            return null;
        }
    }
    private void setPoints(Connection conn, String id, String table, String key, long point) throws SQLException {
        Map<String, Long> cacheMap = cache(key);
        cacheMap.put(id, point);
        sendDeleteCache(key);
        if (plugin.options.database().isMySQL()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO `" + table + "`(`player`,`point`) VALUES(?, ?) on duplicate key update `point`=?;"
            )) {
                ps.setString(1, key);
                ps.setLong(2, point);
                ps.setLong(3, point);
                ps.execute();
            }
        }
        if (plugin.options.database().isSQLite()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT OR REPLACE INTO `" + table + "`(`player`,`point`) VALUES(?, ?);"
            )) {
                ps.setString(1, key);
                ps.setLong(2, point);
                ps.execute();
            }
        }
    }
}
