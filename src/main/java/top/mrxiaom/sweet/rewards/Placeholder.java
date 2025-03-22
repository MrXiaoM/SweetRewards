package top.mrxiaom.sweet.rewards;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.rewards.databases.PointsDatabase;
import top.mrxiaom.sweet.rewards.func.RankManager;
import top.mrxiaom.sweet.rewards.func.entry.PointType;

public class Placeholder extends PlaceholderExpansion {
    SweetRewards plugin;
    public Placeholder(SweetRewards plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean register() {
        try {
            unregister();
        } catch (Throwable ignored) {
        }
        return super.register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getDescription().getName();
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.startsWith("rank_")) {
            String[] split = params.substring(5).split("_", 4);
            if (split.length < 3) return "WRONG_USAGE";
            RankManager manager = RankManager.inst();
            String pointType = split[0];
            int top = Util.parseInt(split[1]).orElse(0);
            if (top < 1 || top > manager.getTop()) return "WRONG_USAGE";
            String type = split[2];
            String def = split.length > 3 ? split[3] : null;
            if (type.equals("name")) {
                RankManager.Rank rank = manager.get(pointType, top);
                if (rank != null) {
                    return rank.name;
                } else {
                    return def == null ? "" : def;
                }
            }
            if (type.equals("points")) {
                RankManager.Rank rank = manager.get(pointType, top);
                if (rank != null) {
                    return String.valueOf(rank.points);
                } else {
                    return def == null ? "0" : def;
                }
            }
            return "WRONG_USAGE";
        }
        return super.onRequest(player, params);
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.startsWith("points_")) {
            String id = params.substring(7);
            PointsDatabase db = plugin.getPointsDatabase();
            PointType type = db.get(id);
            if (type == null) return "TYPE_NOT_FOUND";
            long point = db.getPoints(type, player);
            return String.valueOf(point);
        }

        return super.onPlaceholderRequest(player, params);
    }
}
