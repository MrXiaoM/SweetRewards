package top.mrxiaom.sweet.rewards.func;

import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IRunTask;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.rewards.SweetRewards;
import top.mrxiaom.sweet.rewards.databases.PointsDatabase;
import top.mrxiaom.sweet.rewards.func.entry.PointType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoRegister
public class RankManager extends AbstractModule {
    public static class Rank {
        public final String name;
        public final long points;

        public Rank(String name, long points) {
            this.name = name;
            this.points = points;
        }
    }
    int top;
    IRunTask task;
    Map<String, Map<Integer, Rank>> rankMap = new HashMap<>();
    public RankManager(SweetRewards plugin) {
        super(plugin);
    }

    public int getTop() {
        return top;
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        top = config.getInt("rank.top", 0);
        long refreshInterval = config.getLong("rank.refresh-interval", 60) * 20L;
        cancelTask();
        if (top > 0 && refreshInterval > 0) {
            task = plugin.getScheduler().runTaskTimerAsync(() -> {
                for (Map<Integer, Rank> map : rankMap.values()) {
                    map.clear();
                }
                rankMap.clear();
                PointsDatabase db = plugin.getPointsDatabase();
                for (PointType pointType : db.values()) {
                    Map<Integer, Rank> map = new HashMap<>();
                    List<Rank> ranks = db.calculateRank(pointType, top);
                    if (ranks == null) continue;
                    for (int i = 0; i < ranks.size() && i < top; ) {
                        Rank rank = ranks.get(i++);
                        map.put(i, rank);
                    }
                    ranks.clear();
                    rankMap.put(pointType.id, map);
                }
            }, refreshInterval, refreshInterval);
        }
    }

    public void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public void onDisable() {
        cancelTask();
    }

    @Nullable
    public Rank get(String pointType, int num) {
        Map<Integer, Rank> map = rankMap.get(pointType);
        return map == null ? null : map.get(num);
    }

    public static RankManager inst() {
        return instanceOf(RankManager.class);
    }
}
