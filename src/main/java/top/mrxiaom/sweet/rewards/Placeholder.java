package top.mrxiaom.sweet.rewards;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.rewards.databases.PointsDatabase;
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
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.startsWith("points_")) {
            String id = params.substring(7);
            PointsDatabase db = plugin.getPointsDatabase();
            PointType type = db.get(id);
            if (type == null) return "TYPE_NOT_FOUND";
            long point = db.getPoint(type, player);
            return String.valueOf(point);
        }
        return super.onPlaceholderRequest(player, params);
    }
}
