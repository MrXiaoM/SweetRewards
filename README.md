# SweetRewards

Minecraft 累计点数奖励插件。

## 简介

你可以用这个插件来做累积充值奖励等等功能，只要插件支持执行控制台命令，就可以做累积点数奖励。

插件可以定义多种点数，你可以用这个插件做好几种累积点数奖励，该如何部署由你说的算。

累积奖励与菜单界面配置高度融合，编辑累积奖励就是在编辑奖励所在的菜单。你可以添加多个菜单来手动分页。

## 命令

根命令 `/sweetrewards`，别名 `/sr` 或 `/rewards`。  
`<>`包裹的是必选参数，`[]`包裹的是可选参数。

| 命令                              | 描述                       | 权限                         |
|---------------------------------|--------------------------|----------------------------|
| `/rewards get <点数类型>`           | 查看自己的点数数量                | `sweet.rewards.get`        |
| `/rewards get <点数类型> <玩家>`      | 查看某人的点数数量                | `sweet.rewards.get.other`  |
| `/rewards set <点数类型> <玩家> <点数>` | 设置玩家的点数                  | OP/控制台                     |
| `/rewards add <点数类型> <玩家> <点数>` | 增加玩家的点数                  | OP/控制台                     |
| `/rewards reset <菜单ID> <玩家>`    | 重置某人的奖励已领取情况             | OP/控制台                     |
| `/rewards reset <菜单ID> --all`   | 重置所有玩家的奖励已领取情况           | OP/控制台                     |
| `/rewards open <菜单ID>`          | 为自己打开菜单                  | 在菜单配置中设定                   |
| `/rewards open <菜单ID> <玩家>`     | 为某人打开菜单，需要目标玩家拥有菜单配置中的权限 | `sweet.rewards.open-other` |
| `/rewards reload database`      | 重新连接数据库                  | OP/控制台                     |
| `/rewards reload`               | 重载配置文件                   | OP/控制台                     |

由于本插件的定位并不是经济或者点券，故点数只增不减，不会设置需要额外做下界检查的 减少点数命令，也不会支持小数。

## PAPI变量

```
%sweetrewards_points_点数类型% 获取玩家的点数数量
%sweetrewards_rank_点数类型_第几名_name% 获取排行榜第几名的玩家名
%sweetrewards_rank_点数类型_第几名_points% 获取排行榜第几名的点数数量
```
