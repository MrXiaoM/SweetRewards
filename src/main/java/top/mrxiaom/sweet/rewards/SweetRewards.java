package top.mrxiaom.sweet.rewards;
        
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.EconomyHolder;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.rewards.databases.PointsDatabase;

public class SweetRewards extends BukkitPlugin {
    public static SweetRewards getInstance() {
        return (SweetRewards) BukkitPlugin.getInstance();
    }

    public SweetRewards() {
        super(options()
                .bungee(true)
                .adventure(false)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(false)
                .vaultEconomy(false)
                .scanIgnore("top.mrxiaom.sweet.rewards.libs")
        );
    }
    private PointsDatabase pointsDatabase;

    public PointsDatabase getPointsDatabase() {
        return pointsDatabase;
    }

    @Override
    protected void beforeEnable() {
        options.registerDatabase(
                pointsDatabase = new PointsDatabase(this)
        );
        if (Util.isPresent("me.clip.placeholderapi.expansion.PlaceholderExpansion")) {
            new Placeholder(this).register();
        }
    }

    @Override
    protected void afterEnable() {
        getLogger().info("SweetRewards 加载完毕");
    }

    public String key(Player player) {
        return player.getName(); // TODO
    }
}
