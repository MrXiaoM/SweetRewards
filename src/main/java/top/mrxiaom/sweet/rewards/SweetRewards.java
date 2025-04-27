package top.mrxiaom.sweet.rewards;

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.OfflinePlayer;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;
import top.mrxiaom.sweet.rewards.databases.PointsDatabase;
import top.mrxiaom.sweet.rewards.databases.RewardStateDatabase;

public class SweetRewards extends BukkitPlugin {
    public static SweetRewards getInstance() {
        return (SweetRewards) BukkitPlugin.getInstance();
    }

    public SweetRewards() {
        super(options()
                .bungee(true)
                .adventure(true)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(false)
                .scanIgnore("top.mrxiaom.sweet.rewards.libs")
        );
        scheduler = new FoliaLibScheduler(this);
    }
    private PointsDatabase pointsDatabase;
    private RewardStateDatabase rewardStateDatabase;

    public PointsDatabase getPointsDatabase() {
        return pointsDatabase;
    }

    public RewardStateDatabase getRewardStateDatabase() {
        return rewardStateDatabase;
    }

    @Override
    protected void beforeLoad() {
        MinecraftVersion.replaceLogger(getLogger());
        MinecraftVersion.disableUpdateCheck();
        MinecraftVersion.disableBStats();
        MinecraftVersion.getVersion();
    }

    @Override
    protected void beforeEnable() {
        LanguageManager.inst()
                .setLangFile("messages.yml")
                .register(Messages.class, Messages::holder);
        options.registerDatabase(
                pointsDatabase = new PointsDatabase(this),
                rewardStateDatabase = new RewardStateDatabase(this)
        );
        if (Util.isPresent("me.clip.placeholderapi.expansion.PlaceholderExpansion")) {
            new Placeholder(this).register();
        }
    }

    @Override
    protected void afterEnable() {
        getLogger().info("SweetRewards 加载完毕");
    }

    public String key(OfflinePlayer player) {
        return player.getName(); // TODO
    }

    public OfflinePlayer key(String key) {
        return Util.getOfflinePlayer(key).orElse(null); // TODO
    }
}
