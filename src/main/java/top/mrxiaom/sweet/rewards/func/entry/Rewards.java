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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.func.AbstractGuiModule;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.gui.IGui;
import top.mrxiaom.pluginbase.utils.AdventureUtil;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.PAPI;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.rewards.Messages;
import top.mrxiaom.sweet.rewards.SweetRewards;
import top.mrxiaom.sweet.rewards.databases.PointsDatabase;
import top.mrxiaom.sweet.rewards.databases.RewardStateDatabase;
import top.mrxiaom.sweet.rewards.func.AbstractPluginHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static top.mrxiaom.pluginbase.func.AbstractGuiModule.getInventory;

public class Rewards extends AbstractPluginHolder {
    public final String id;
    public final String title;
    public final char[] inventory;
    public final @Nullable String permission;
    public final List<String> addNoticeMessage, joinNoticeMessage;
    public final Map<Character, Reward> rewards = new HashMap<>();
    public final Map<Character, LoadedIcon> otherIcons = new HashMap<>();
    public Rewards(SweetRewards plugin, MemorySection config, String id) {
        super(plugin);
        ConfigurationSection section;
        this.id = id;
        this.title = config.getString("title", "");
        this.inventory = getInventory(config, "inventory");
        String perm = config.getString("permission", null);
        this.permission = perm == null ? null : perm.replace("%id%", id);
        List<String> defOpNotReach = config.getStringList("default-operations.not-reach");
        List<String> defOpAvailable = config.getStringList("default-operations.available");
        List<String> defOpAlready = config.getStringList("default-operations.already");
        this.addNoticeMessage = config.getStringList("add-notice-message");
        this.joinNoticeMessage = config.getStringList("join-notice-message");
        section = config.getConfigurationSection("rewards");
        if (section != null) for (String key : section.getKeys(false)) {
            Reward reward = Reward.load(plugin, id, section, key, defOpNotReach, defOpAvailable, defOpAlready);
            if (reward != null) {
                this.rewards.put(reward.id, reward);
            }
        }
        section = config.getConfigurationSection("other-icons");
        if (section != null) for (String key : section.getKeys(false)) {
            LoadedIcon icon = LoadedIcon.load(section, key);
            if (key.length() != 1) {
                plugin.warn("[rewards/" + id + "] 其它图标 " + key + " 的图标ID过长，请改成单个字符");
                continue;
            }
            this.otherIcons.put(key.charAt(0), icon);
        }
    }

    public void sendNoticeMessageOrNot(Player player, List<String> messages) {
        if (messages.isEmpty()) return;
        if (permission != null && !player.hasPermission(permission)) return;
        List<Character> keys = new ArrayList<>(rewards.keySet());
        PointsDatabase pdb = plugin.getPointsDatabase();
        RewardStateDatabase db = plugin.getRewardStateDatabase();
        Map<Character, Boolean> states = db.checkStates(player, id, keys);
        keys.removeIf(it -> states.getOrDefault(it, false));
        int count = 0;
        for (Character key : keys) {
            Reward reward = rewards.get(key);
            if (reward == null) continue;
            long point = pdb.getPoints(reward.type, player);
            long require = reward.point;
            if (point >= require) {
                count++;
            }
        }
        if (count > 0) {
            Pair<String, Object>[] pairs = Pair.array(2);
            pairs[0] = Pair.of("%id%", id);
            pairs[1] = Pair.of("%count%", count);
            for (String message : messages) {
                String s = Pair.replace(message, pairs);
                AdventureUtil.sendMessage(player, PAPI.setPlaceholders(player, s));
            }
        }
    }

    private ItemStack applyMainIcon(Gui instance, Player player, char id, int index) {
        Reward reward = rewards.get(id);
        if (reward != null) {
            PointsDatabase db = plugin.getPointsDatabase();
            long point = db.getPoints(reward.type, player);
            long require = reward.point;
            boolean already = instance.hasUsed(reward);
            boolean notReach = point < require;
            String sPoint = String.valueOf(require);
            String sPoints = String.valueOf(point);
            ItemStack baseItem;
            Integer customModelData;
            if (already) {
                baseItem = reward.materialAlready.getNewItem();
                customModelData = reward.materialAlready.getCustomModelData();
            } else if (notReach) {
                baseItem = reward.materialNotReach.getNewItem();
                customModelData = reward.materialNotReach.getCustomModelData();
            } else {
                baseItem = reward.material.getNewItem();
                customModelData = reward.material.getCustomModelData();
            }
            ItemStack item = reward.icon.generateIcon(baseItem, player,
                    name -> name.replace("%point%", sPoint)
                            .replace("%points%", sPoints)
                            .replace("%current%", sPoints),
                    lore -> {
                        List<String> newLore = new ArrayList<>();
                        for (String s : lore) {
                            if (!s.equals("operation")) {
                                newLore.add(s.replace("%point%", sPoint)
                                        .replace("%points%", sPoints)
                                        .replace("%current%", sPoints));
                                continue;
                            }
                            List<String> operation = already
                                    ? reward.opAlready
                                    : (notReach
                                    ? reward.opNotReach
                                    : reward.opAvailable);
                            for (String line : operation) {
                                newLore.add(line.replace("%point%", sPoint)
                                        .replace("%points%", sPoints)
                                        .replace("%current%", sPoints));
                            }
                        }
                        return newLore;
                    });
            if (customModelData != null) {
                ItemStackUtil.setCustomModelData(item, customModelData);
            }
            return item;
        }
        return null;
    }

    public Gui createGui(Player player) {
        List<Character> keys = new ArrayList<>(rewards.keySet());
        RewardStateDatabase db = plugin.getRewardStateDatabase();
        Map<Character, Boolean> states = db.checkStates(player, id, keys);
        return new Gui(player, states);
    }

    public class Gui implements IGui, InventoryHolder {
        private Player player;
        private Inventory created;
        private Map<Character, Boolean> states;
        private Gui(Player player, Map<Character, Boolean> states) {
            this.player = player;
            this.states = states;
        }

        public boolean hasUsed(Reward reward) {
            return states.getOrDefault(reward.id, false);
        }

        public boolean isSameRewards(Rewards other) {
            return id.equals(other.id);
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @NotNull
        @Override
        public Inventory getInventory() {
            return created;
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
            created = Bukkit.createInventory(this, inventory.length, PAPI.setPlaceholders(player, title));
            updateInventory(created);
            return created;
        }

        @Override
        public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType,
                            int slot, ItemStack currentItem, ItemStack cursor,
                            InventoryView view, InventoryClickEvent event
        ) {
            event.setCancelled(true);
            Character clickId = getClickedId(slot);
            if (clickId != null) {
                Reward reward = rewards.get(clickId);
                if (reward != null && click.equals(ClickType.LEFT)) {
                    PointsDatabase db = plugin.getPointsDatabase();
                    long point = db.getPoints(reward.type, player);
                    long require = reward.point;
                    if (hasUsed(reward)) {
                        Messages.gui__reward__already.tm(player);
                        return;
                    }
                    if (point < require) {
                        Messages.gui__reward__not_reach.tm(player, Pair.of("%type%", reward.type.display));
                        return;
                    }
                    states.put(reward.id, true);
                    RewardStateDatabase db1 = plugin.getRewardStateDatabase();
                    db1.markState(player, id, reward.id);
                    List<Pair<String, Object>> pairs = new ArrayList<>();
                    pairs.add(Pair.of("%point%", require));
                    pairs.add(Pair.of("%points%", point));
                    for (IAction a : reward.rewards) {
                        a.run(player, pairs);
                    }
                    updateInventory(view);
                    return;
                }
                LoadedIcon icon = otherIcons.get(clickId);
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
