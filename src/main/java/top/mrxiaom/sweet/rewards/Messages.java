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
    commands__open__not_found("&e找不到这个菜单"),
    commands__no_permission("&c你没有执行该命令的权限"),
    player__not_found("&e玩家不在线 (或不存在)"),
    player__only("&e该操作只能由玩家执行"),
    commands__help__normal(),
    commands__help__admin(),


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