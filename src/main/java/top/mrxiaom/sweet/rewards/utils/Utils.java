package top.mrxiaom.sweet.rewards.utils;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;

public class Utils {
    public static Pair<Material, Integer> parseMaterial(ConfigurationSection section, String key, Pair<Material, Integer> def) {
        String value = section.getString(key, null);
        if (value == null) return def;
        String[] split = value.contains(":") ? value.split(":", 2) : new String[]{value};
        Integer data = split.length > 1 ? Util.parseInt(split[1]).orElse(null) : null;
        Material material = Material.matchMaterial(split[0]);
        if (material != null) {
            return Pair.of(material, data);
        }
        material = Util.valueOr(Material.class, split[0], null);
        return material != null ? Pair.of(material, data) : def;
    }
}
