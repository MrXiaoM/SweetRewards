package top.mrxiaom.sweet.rewards.func;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.rewards.SweetRewards;
import top.mrxiaom.sweet.rewards.func.entry.Rewards;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@AutoRegister
public class RewardsManager extends AbstractModule {
    final Map<String, Rewards> rewardsMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public RewardsManager(SweetRewards plugin) {
        super(plugin);
        registerEvents();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        rewardsMap.clear();
        AtomicInteger totalRewards = new AtomicInteger(0);
        for (String s : config.getStringList("rewards-folders")) {
            File folder = s.startsWith("./") ? new File(plugin.getDataFolder(), s.substring(2)) : new File(s);
            if (!folder.exists()) {
                Util.mkdirs(folder);
                if (s.equals("./rewards")) {
                    plugin.saveResource("rewards/example.yml", new File(folder, "example.yml"));
                }
            }
            Util.reloadFolder(folder, false, (id, file) -> {
                if (rewardsMap.containsKey(id)) {
                    warn("[rewards] 重复的奖励界面ID: " + id);
                    return;
                }
                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                Rewards rewards = new Rewards(plugin, cfg, id);
                rewardsMap.put(id, rewards);
                totalRewards.addAndGet(rewards.rewards.size());
            });
        }
        info("加载了 " + rewardsMap.size() + " 个界面，共 " + totalRewards.get() + " 个累计点数奖励");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        for (Rewards rewards : rewards()) {
            rewards.sendNoticeMessageOrNot(player, rewards.joinNoticeMessage);
        }
    }

    public Set<String> keys() {
        return rewardsMap.keySet();
    }

    public Set<String> keys(Permissible p) {
        if (p.isOp()) return keys();
        Set<String> set = new HashSet<>();
        for (Rewards rewards : rewardsMap.values()) {
            if (rewards.permission == null || p.hasPermission(rewards.permission)) {
                set.add(rewards.id);
            }
        }
        return set;
    }

    public Collection<Rewards> rewards() {
        return Collections.unmodifiableCollection(rewardsMap.values());
    }

    @Nullable
    public Rewards get(String id) {
        return rewardsMap.get(id);
    }

    public static RewardsManager inst() {
        return instanceOf(RewardsManager.class);
    }
}
