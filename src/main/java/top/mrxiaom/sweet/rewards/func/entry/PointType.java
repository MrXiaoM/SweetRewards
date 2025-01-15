package top.mrxiaom.sweet.rewards.func.entry;

import org.bukkit.configuration.ConfigurationSection;

public class PointType {
    public final String id;
    public final String table;
    public final String display;
    public final long initialValue;

    public PointType(String id, String prefix, String display, long initialValue) {
        this.id = id;
        this.table = (prefix + "points_" + id).toUpperCase();
        this.display = display;
        this.initialValue = initialValue;
    }

    public static PointType load(ConfigurationSection config, String id, String prefix) {
        String display = config.getString("display", id);
        long initialValue = config.getLong("initial-value", 0L);
        return new PointType(id, prefix, display, initialValue);
    }
}
