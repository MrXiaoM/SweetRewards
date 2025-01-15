package top.mrxiaom.sweet.rewards.func.entry;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.func.gui.actions.IAction;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.rewards.SweetRewards;
import top.mrxiaom.sweet.rewards.utils.Utils;

import java.util.List;

import static top.mrxiaom.pluginbase.func.AbstractGuiModule.loadActions;

public class Reward {
    public final char id;
    public final String key;
    public final PointType type;
    public final long point;
    public final Material material, materialNotReach, materialAlready;
    public final Integer data, dataNotReach, dataAlready;
    public final LoadedIcon icon;
    public final List<String> opNotReach, opAvailable, opAlready;
    public final List<IAction> rewards;

    public Reward(char id, String key, PointType type, long point,
                  Material material, Material materialNotReach, Material materialAlready,
                  Integer data, Integer dataNotReach, Integer dataAlready, LoadedIcon icon,
                  List<String> opNotReach, List<String> opAvailable, List<String> opAlready,
                  List<IAction> rewards
    ) {
        this.id = id;
        this.key = key;
        this.type = type;
        this.point = point;
        this.material = material;
        this.materialNotReach = materialNotReach;
        this.materialAlready = materialAlready;
        this.data = data;
        this.dataNotReach = dataNotReach;
        this.dataAlready = dataAlready;
        this.icon = icon;
        this.opNotReach = opNotReach;
        this.opAvailable = opAvailable;
        this.opAlready = opAlready;
        this.rewards = rewards;
    }

    public static Reward load(SweetRewards plugin, String parent, ConfigurationSection section, String id,
                              List<String> def1, List<String> def2, List<String> def3) {
        if (id.length() != 1) {
            plugin.warn("[rewards/" + parent + "] '" + id + "' 的ID过长");
            return null;
        }
        char realId = id.charAt(0);
        LoadedIcon icon = LoadedIcon.load(section, id);
        String typeStr = section.getString(id + ".type", "");
        PointType type = plugin.getPointsDatabase().get(typeStr);
        if (type == null) {
            plugin.warn("[rewards/" + parent + "] '" + id + "' 的点数类型 '" + typeStr + "' 不存在");
            return null;
        }
        long point = section.getLong(id + ".point", 0L);
        if (point <= 0L) {
            plugin.warn("[rewards/" + parent + "] '" + id + "' 的需求点数应当大于0");
            return null;
        }
        Pair<Material, Integer> material = Utils.parseMaterial(section, id + ".material", null);
        Pair<Material, Integer> materialNotReach = Utils.parseMaterial(section, id + ".material-not-reach", material);
        Pair<Material, Integer> materialAlready = Utils.parseMaterial(section, id + ".material-already", material);
        if (material == null || materialNotReach == null || materialAlready == null) {
            plugin.warn("[rewards/" + parent + "] '" + id + "' 的物品图标设置不正确");
            return null;
        }
        List<String> opNotReach = getList(section, id + ".operations.not-reach", def1);
        List<String> opAvailable = getList(section, id + ".operations.available", def2);
        List<String> opAlready = getList(section, id + ".operations.already", def3);
        List<IAction> rewards = loadActions(section, id + ".rewards");
        if (rewards.isEmpty()) {
            plugin.warn("[rewards/" + parent + "] '" + id + "' 的奖励为空");
            return null;
        }
        return new Reward(realId, parent + "." + id, type, point,
                material.getKey(), materialNotReach.getKey(), materialAlready.getKey(),
                material.getValue(), materialNotReach.getValue(), materialAlready.getValue(),
                icon, opNotReach, opAvailable, opAlready, rewards);
    }

    private static List<String> getList(ConfigurationSection section, String key, List<String> def) {
        if (section.contains(key) && section.isList(key)) {
            return section.getStringList(key);
        }
        return def;
    }
}
