package top.mrxiaom.sweet.rewards.func.entry;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AbstractGuiModule;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.gui.IGui;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.sweet.rewards.SweetRewards;
import top.mrxiaom.sweet.rewards.databases.PointsDatabase;
import top.mrxiaom.sweet.rewards.func.AbstractPluginHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static top.mrxiaom.pluginbase.func.AbstractGuiModule.getInventory;

public class Rewards extends AbstractPluginHolder {
    public final String title;
    public final char[] inventory;
    public final Map<Character, Reward> rewards = new HashMap<>();
    public final Map<Character, LoadedIcon> otherIcons = new HashMap<>();
    public Rewards(SweetRewards plugin, MemorySection config, String id) {
        super(plugin);
        ConfigurationSection section;
        title = config.getString("title", "");
        inventory = getInventory(config, "inventory");
        List<String> defOpNotReach = config.getStringList("default-operations.not-reach");
        List<String> defOpAvailable = config.getStringList("default-operations.available");
        List<String> defOpAlready = config.getStringList("default-operations.already");
        section = config.getConfigurationSection("rewards");
        if (section != null) for (String key : section.getKeys(false)) {
            Reward reward = Reward.load(plugin, id, section, key, defOpNotReach, defOpAvailable, defOpAlready);
            if (reward != null) {
                rewards.put(reward.id, reward);
            }
        }
        section = config.getConfigurationSection("other-icons");
        if (section != null) for (String key : section.getKeys(false)) {
            LoadedIcon icon = LoadedIcon.load(section, key);
            if (key.length() != 1) {
                plugin.warn("[rewards/" + id + "] 其它图标 " + key + " 的图标ID过长，请改成单个字符");
                continue;
            }
            otherIcons.put(key.charAt(0), icon);
        }
    }

    private ItemStack applyMainIcon(Gui instance, Player player, char id, int index) {
        Reward reward = rewards.get(id);
        if (reward != null) {
            PointsDatabase db = plugin.getPointsDatabase();
            long point = db.getPoint(reward.type, player);
            long require = reward.point;
            // TODO: 添加主要图标
            if (point < require) {

            }
        }
        return null;
    }

    public Gui createGui(Player player) {
        return new Gui(player);
    }

    public class Gui implements IGui {
        protected Player player;
        private Gui(Player player) {
            this.player = player;
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        public void updateInventory(BiConsumer<Integer, ItemStack> setItem) {
            for (int i = 0; i < inventory.length; i++) {
                char id = inventory[i];
                if (id == ' ' || id == '　') {
                    setItem.accept(i, null);
                    continue;
                }
                ItemStack item = applyMainIcon(this, player, id, i);
                if (item != null) {
                    setItem.accept(i, item);
                    continue;
                }
                LoadedIcon icon = otherIcons.get(id);
                if (icon != null) {
                    setItem.accept(i, icon.generateIcon(player));
                    continue;
                }
                setItem.accept(i, null);
            }
        }

        public void updateInventory(Inventory inv) {
            updateInventory(inv::setItem);
        }

        public void updateInventory(InventoryView view) {
            updateInventory(view::setItem);
            player.updateInventory();
        }

        @Override
        public Inventory newInventory() {
            Inventory inv = Bukkit.createInventory(null, inventory.length, PAPI.setPlaceholders(player, title));
            updateInventory(inv);
            return inv;
        }

        @Override
        public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType,
                            int slot, ItemStack currentItem, ItemStack cursor,
                            InventoryView view, InventoryClickEvent event
        ) {
            event.setCancelled(true);
            Character id = getClickedId(slot);
            if (id != null) {
                Reward reward = rewards.get(id);
                if (reward != null && click.equals(ClickType.LEFT)) {
                    PointsDatabase db = plugin.getPointsDatabase();
                    long point = db.getPoint(reward.type, player);
                    long require = reward.point;
                    // TODO: 处理点击奖励图标
                    if (point < require) {

                    }
                    return;
                }
                LoadedIcon icon = otherIcons.get(id);
                if (icon != null) {
                    icon.click(player, click);
                }
            }
        }

        public Character getClickedId(int slot) {
            return AbstractGuiModule.getClickedId(inventory, slot);
        }
    }
}
