package top.mrxiaom.sweet.rewards;

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.paper.PaperFactory;
import top.mrxiaom.pluginbase.resolver.DefaultLibraryResolver;
import top.mrxiaom.pluginbase.utils.ClassLoaderWrapper;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.inventory.InventoryFactory;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;
import top.mrxiaom.sweet.rewards.databases.PointsDatabase;
import top.mrxiaom.sweet.rewards.databases.RewardStateDatabase;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.UUID;

public class SweetRewards extends BukkitPlugin {
    public static SweetRewards getInstance() {
        return (SweetRewards) BukkitPlugin.getInstance();
    }

    public SweetRewards() throws Exception {
        super(options()
                .bungee(true)
                .adventure(true)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(false)
                .scanIgnore("top.mrxiaom.sweet.rewards.libs")
        );
        scheduler = new FoliaLibScheduler(this);

        info("正在检查依赖库状态");
        File librariesDir = ClassLoaderWrapper.isSupportLibraryLoader
                ? new File("libraries")
                : new File(this.getDataFolder(), "libraries");
        DefaultLibraryResolver resolver = new DefaultLibraryResolver(getLogger(), librariesDir);

        resolver.addLibrary(BuildConstants.LIBRARIES);

        List<URL> libraries = resolver.doResolve();
        info("正在添加 " + libraries.size() + " 个依赖库到类加载器");
        for (URL library : libraries) {
            this.classLoader.addURL(library);
        }
    }

    @Override
    protected @NotNull ClassLoaderWrapper initClassLoader(URLClassLoader classLoader) {
        return ClassLoaderWrapper.isSupportLibraryLoader
                ? new ClassLoaderWrapper(ClassLoaderWrapper.findLibraryLoader(classLoader))
                : new ClassLoaderWrapper(classLoader);
    }

    @Override
    public @NotNull InventoryFactory initInventoryFactory() {
        return PaperFactory.createInventoryFactory();
    }

    @Override
    public @NotNull ItemEditor initItemEditor() {
        return PaperFactory.createItemEditor();
    }

    private PointsDatabase pointsDatabase;
    private RewardStateDatabase rewardStateDatabase;
    private boolean onlineMode;

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

    @Override
    protected void beforeReloadConfig(FileConfiguration config) {
        String online = config.getString("online-mode", "auto").toLowerCase();
        switch (online) {
            case "true":
                onlineMode = true;
                break;
            case "false":
                onlineMode = false;
                break;
            case "auto":
            default:
                onlineMode = Bukkit.getOnlineMode();
                break;
        }
    }

    public String key(OfflinePlayer player) {
        if (player == null) return null;
        return onlineMode
                ? player.getUniqueId().toString()
                : player.getName();
    }

    public OfflinePlayer key(String key) {
        if (onlineMode) {
            return Util.getOfflinePlayer(UUID.fromString(key)).orElse(null);
        } else {
            return Util.getOfflinePlayer(key).orElse(null);
        }
    }
}
