package top.mrxiaom.sweet.rewards.utils;

import org.bukkit.configuration.ConfigurationSection;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.rewards.func.entry.Material;

public class Utils {
    public static Material parseMaterial(ConfigurationSection section, String key, Material def) {
        String value = section.getString(key, null);
        if (value == null) return def;
        Integer customModelData;
        if (value.contains(";")) {
            String[] split = value.split(";", 2);
            value = split[0];
            customModelData = Util.parseInt(split[1]).orElse(null);
        } else {
            customModelData = null;
        }
        String[] split = value.contains(":") ? value.split(":", 2) : new String[]{value};
        Integer data = split.length > 1 ? Util.parseInt(split[1]).orElse(null) : null;
        org.bukkit.Material material = org.bukkit.Material.matchMaterial(split[0]);
        if (material != null) {
            return new Material(material, data, customModelData);
        }
        material = Util.valueOr(org.bukkit.Material.class, split[0], null);
        if (material != null) {
            return new Material(material, data, customModelData);
        }
        return def;
    }
}
