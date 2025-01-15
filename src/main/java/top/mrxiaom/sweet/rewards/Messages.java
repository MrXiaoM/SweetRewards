package top.mrxiaom.sweet.rewards;

import top.mrxiaom.pluginbase.func.language.IHolderAccessor;
import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder;

import java.util.List;

import static top.mrxiaom.pluginbase.func.language.LanguageEnumAutoHolder.wrap;

@Language(prefix = "messages.")
public enum Messages implements IHolderAccessor {
    gui__reward__already("&e你已经领取过这个奖励了!"),
    gui__reward__not_reach("&e你没有足够的%type%领取这个奖励"),
    commands__reload("&a配置文件已重载"),
    commands__reload_database("&a已重新连接到数据库"),
    commands__set__not_found("&e找不到这个点数类型"),
    commands__set__not_number("&e请输入正确的点数数值"),
    commands__set__success("&a玩家&e %player% &a的点数&e %display%&r&7(%id%) &a已设置为&e %points%"),
    commands__set__fail("&e玩家&b %player% &a的点数&b %display%&r&7(%id%) &e设置&7(%points%)&e失败，详见控制台日志"),
    commands__add__not_found("&e找不到这个点数类型"),
    commands__add__not_number("&e请输入正确的点数数值"),
    commands__add__success("&a玩家&e %player% &a的点数&e %display%&r&7(%id%) &a增加了&e %added%&a，现在有&e %points%"),
    commands__add__fail("&e玩家&b %player% &a的点数&b %display%&r&7(%id%) &e增加&7(%added%)&e失败，详见控制台日志"),
    commands__get__not_found("&e找不到这个点数类型"),
    commands__get__success("&a你的点数&e %display%&r&7(%id%) 有&e %points%"),
    commands__get__success_other("&a玩家&e %player% 的点数&e %display%&r&7(%id%) 有&e %points%"),
    commands__open__not_found("&e找不到这个菜单"),
    commands__no_permission("&c你没有执行该命令的权限"),
    player__not_found("&e玩家不在线 (或不存在)"),
    player__only("&e该操作只能由玩家执行"),
    commands__help__normal("",
            "&d&lSweetRewards&r &b累计奖励",
            "&f/rewards get <点数类型> &7查看自己的点数数量",
            "&f/rewards open <菜单ID> &7打开菜单",
            ""),
    commands__help__admin("",
            "&d&lSweetRewards&r &b累计奖励",
            "&f/rewards get <点数类型> [玩家] &7查看自己或某人的点数数量",
            "&f/rewards open <菜单ID> [玩家] &7为自己或某人打开菜单",
            "&f/rewards add <点数类型> <玩家> <点数> &7增加玩家的点数",
            "&f/rewards set <点数类型> <玩家> <点数> &7设置玩家的点数",
            "&f/rewards reload database &7重新连接数据库",
            "&f/rewards reload &7重载配置文件",
            ""),


    ;
    Messages(String defaultValue) {
        holder = wrap(this, defaultValue);
    }
    Messages(String... defaultValue) {
        holder = wrap(this, defaultValue);
    }
    Messages(List<String> defaultValue) {
        holder = wrap(this, defaultValue);
    }
    private final LanguageEnumAutoHolder<Messages> holder;
    public LanguageEnumAutoHolder<Messages> holder() {
        return holder;
    }
}
