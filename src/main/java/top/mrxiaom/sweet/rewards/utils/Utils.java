package top.mrxiaom.sweet.rewards.utils;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import top.mrxiaom.pluginbase.utils.Util;

public class Utils {
    public static Material parseMaterial(ConfigurationSection section, String key, Material def) {
        String value = section.getString(key, null);
        if (value == null) return def;
        Material material = Material.matchMaterial(value);
        if (material != null) {
            return material;
        }
        return Util.valueOr(Material.class, value, def);
    }
}
