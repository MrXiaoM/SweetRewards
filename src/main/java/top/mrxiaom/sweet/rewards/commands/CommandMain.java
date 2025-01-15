package top.mrxiaom.sweet.rewards.commands;
        
import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.rewards.Messages;
import top.mrxiaom.sweet.rewards.SweetRewards;
import top.mrxiaom.sweet.rewards.databases.PointsDatabase;
import top.mrxiaom.sweet.rewards.func.AbstractModule;
import top.mrxiaom.sweet.rewards.func.RewardsManager;
import top.mrxiaom.sweet.rewards.func.entry.PointType;
import top.mrxiaom.sweet.rewards.func.entry.Rewards;

import java.util.*;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter, Listener {
    public CommandMain(SweetRewards plugin) {
        super(plugin);
        registerCommand("sweetrewards", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 4 && "set".equalsIgnoreCase(args[0]) && sender.isOp()) {
            PointsDatabase db = plugin.getPointsDatabase();
            PointType type = db.get(args[1]);
            if (type == null) {
                return Messages.commands__set__not_found.tm(sender);
            }
            Player target = Util.getOnlinePlayer(args[2]).orElse(null);
            if (target == null) {
                return Messages.player__not_found.tm(sender);
            }
            long toSet = Util.parseLong(args[3]).orElse(0L);
            if (toSet < 0) {
                return Messages.commands__set__not_number.tm(sender);
            }
            Long points = db.setPoint(type, target, toSet);
            return (points == null ? Messages.commands__set__fail : Messages.commands__set__success).tm(sender,
                    Pair.of("%player%", target.getName()),
                    Pair.of("%display%", type.display),
                    Pair.of("%id%", type.id),
                    Pair.of("%points%", points == null ? toSet : points.longValue()));
        }
        if (args.length == 4 && "add".equalsIgnoreCase(args[0]) && sender.isOp()) {
            PointsDatabase db = plugin.getPointsDatabase();
            PointType type = db.get(args[1]);
            if (type == null) {
                return Messages.commands__add__not_found.tm(sender);
            }
            Player target = Util.getOnlinePlayer(args[2]).orElse(null);
            if (target == null) {
                return Messages.player__not_found.tm(sender);
            }
            long toAdd = Util.parseLong(args[3]).orElse(0L);
            if (toAdd <= 0) {
                return Messages.commands__add__not_number.tm(sender);
            }
            Long points = db.addPoint(type, target, toAdd);
            return (points == null ? Messages.commands__add__fail : Messages.commands__add__success).tm(sender,
                        Pair.of("%player%", target.getName()),
                        Pair.of("%display%", type.display),
                        Pair.of("%id%", type.id),
                        Pair.of("%added%", toAdd),
                        Pair.of("%points%", points == null ? -1L : points.longValue()));
        }
        if (args.length == 4 && "get".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.rewards.get")) {
            PointsDatabase db = plugin.getPointsDatabase();
            PointType type = db.get(args[1]);
            if (type == null) {
                return Messages.commands__get__not_found.tm(sender);
            }
            Player target;
            boolean other;
            if (args.length == 3) {
                if (!sender.hasPermission("sweet.rewards.get.other")) {
                    return Messages.commands__no_permission.tm(sender);
                }
                target = Util.getOnlinePlayer(args[2]).orElse(null);
                if (target == null) {
                    return Messages.player__not_found.tm(sender);
                }
                other = true;
            } else {
                if (!(sender instanceof Player)) {
                    return Messages.player__only.tm(sender);
                }
                target = (Player) sender;
                other = false;
            }
            long points = db.getPoint(type, target);
            return (other ? Messages.commands__get__success_other : Messages.commands__get__success).tm(sender,
                    Pair.of("%player%", target.getName()),
                    Pair.of("%display%", type.display),
                    Pair.of("%id%", type.id),
                    Pair.of("%points%", points));
        }
        if (args.length >= 2 && "open".equalsIgnoreCase(args[0])) {
            RewardsManager manager = RewardsManager.inst();
            Rewards rewards = manager.get(args[1]);
            if (rewards == null) {
                return Messages.commands__open__not_found.tm(sender);
            }
            Player target;
            if (args.length == 3) {
                if (!sender.hasPermission("sweet.rewards.open-other")) {
                    return Messages.commands__no_permission.tm(sender);
                }
                target = Util.getOnlinePlayer(args[2]).orElse(null);
                if (target == null) {
                    return Messages.player__not_found.tm(sender);
                }
            } else {
                if (!(sender instanceof Player)) {
                    return Messages.player__only.tm(sender);
                }
                target = (Player) sender;
            }
            if (rewards.permission != null && !target.hasPermission(rewards.permission)) {
                return Messages.commands__no_permission.tm(target);
            }
            rewards.createGui(target).open();
            return true;
        }
        if (args.length >= 1 && "reload".equalsIgnoreCase(args[0]) && sender.isOp()) {
            if (args.length == 2 && "database".equalsIgnoreCase(args[1])) {
                plugin.options.database().reloadConfig();
                plugin.options.database().reconnect();
                return Messages.commands__reload_database.tm(sender);
            }
            plugin.reloadConfig();
            return Messages.commands__reload.tm(sender);
        }
        return (sender.isOp() ? Messages.commands__help__admin : Messages.commands__help__normal).tm(sender);
    }

    private static final List<String> emptyList = Lists.newArrayList();
    private static final List<String> listArg0 = Lists.newArrayList(
            "open");
    private static final List<String> listOpArg0 = Lists.newArrayList(
            "open", "add", "set", "reload");
    private static final List<String> listArgs1Reload = Lists.newArrayList("database");
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission("sweet.rewards.get")) list.add("get");
            list.addAll(sender.isOp() ? listOpArg0 : listArg0);
            return startsWith(list, args[0]);
        }
        if (args.length == 2) {
            if ("set".equalsIgnoreCase(args[0]) && sender.isOp()) {
                return startsWith(RewardsManager.inst().keys(), args[1]);
            }
            if ("add".equalsIgnoreCase(args[0]) && sender.isOp()) {
                return startsWith(RewardsManager.inst().keys(), args[1]);
            }
            if ("get".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.rewards.get")) {
                return startsWith(RewardsManager.inst().keys(sender), args[1]);
            }
            if ("open".equalsIgnoreCase(args[0])) {
                return startsWith(RewardsManager.inst().keys(sender), args[1]);
            }
            if ("reload".equalsIgnoreCase(args[1]) && sender.isOp()) {
                return startsWith(listArgs1Reload, args[1]);
            }
        }
        if (args.length == 3) {
            if ("set".equalsIgnoreCase(args[0]) && sender.isOp()) {
                return null;
            }
            if ("add".equalsIgnoreCase(args[0]) && sender.isOp()) {
                return null;
            }
            if ("get".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.rewards.get.other")) {
                return null;
            }
            if ("open".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.rewards.open-other")) {
                return null;
            }
        }
        return emptyList;
    }

    public List<String> startsWith(Collection<String> list, String s) {
        return startsWith(null, list, s);
    }
    public List<String> startsWith(String[] addition, Collection<String> list, String s) {
        String s1 = s.toLowerCase();
        List<String> stringList = new ArrayList<>(list);
        if (addition != null) stringList.addAll(0, Lists.newArrayList(addition));
        stringList.removeIf(it -> !it.toLowerCase().startsWith(s1));
        return stringList;
    }
}
