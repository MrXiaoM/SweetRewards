package top.mrxiaom.sweet.rewards.func;
        
import top.mrxiaom.sweet.rewards.SweetRewards;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<SweetRewards> {
    public AbstractPluginHolder(SweetRewards plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(SweetRewards plugin, boolean register) {
        super(plugin, register);
    }
}
