# 玩家指南

[返回 README](../README.zh-CN.md) · [English](player-guide.md)

## 获取 Mod 和材料

| 物品 | 去哪里找 |
| --- | --- |
| **Mod 和内融核心** | 可以先找地牢和废弃矿井的箱子。这两类箱子都有两次独立的 80% 额外抽取，分别产出一张 Mod 和 1–4 个内融核心。 |
| **塑形块** | 末影人有 10% 概率掉落一个。满级武器可以消耗塑形块改变槽位极性，改完后从 0 级重新练起。卡片和槽位的极性相符时，消耗更少容量。 |
| **未知裂罅与赤毒** | 远古城市战利品箱有 10% 概率产出未知裂罅，也有机会找到赤毒。不祥试炼奖励和凋灵也能提供赤毒。揭秘后，裂罅会绑定一种武器并显示词条。花赤毒循环后，可以在原词条和新词条之间选择。 |
| **镓与奥罗金催化剂** | Y=-16～48 可生成镓矿，Y=16 附近较多，用石镐及以上采集。矿石和粗镓都能用熔炉或高炉炼成镓。四个镓、两个铁锭和一枚紫水晶碎片可以合成催化剂。主手持武器、副手持催化剂，右键即可使用。 |

手持内融核心或赤毒右键可存入 Mod 空间。内融核心用于升级 Mod，多余的卡也能在仓库回收成内融核心。Mod 从战利品中获取。

完整掉落池及权重可查看[容器掉落规则](../common/src/main/java/yagen/waitmydawn/yagens_attributes/core/loot/BuiltinContainerLoot.java)和[生物掉落规则](../common/src/main/java/yagen/waitmydawn/yagens_attributes/core/loot/BuiltinMobLoot.java)。

## 看懂战斗反馈

- 武器面板显示各类伤害的实际数值。不同的生命材质和护盾对各类伤害的反应不同。单元素会按槽位顺序组合，移动卡片可能改变合成结果。
- 暴击几率超过 100% 后可以打出更高等级的暴击。伤害数字以颜色区分暴击等级，高阶红暴带感叹号。
- 实体头顶图标显示当前触发层数。
- 远程击中弱点可以激活氩晶瞄具。按 **F3+B** 可以在碰撞箱旁看到已配置的弱点。
- 近战命中累积连击，部分 Mod 会根据连击等级提高暴击几率或触发几率。镀层等条件增益可在武器增益条查看。

### 十三种触发

| 伤害类型 | 触发效果 |
| --- | --- |
| 冲击 | 使目标短暂踉跄。 |
| 穿刺 | 削弱目标的输出，并提高它被暴击的几率。 |
| 切割 | 造成绕过护甲的流血，目标有盾时仍会扣盾。 |
| 冰冻 | 减速并提高目标受到的暴击伤害。叠满可冻结可受控的生物。 |
| 火焰 | 持续燃烧，逐渐削减护甲。 |
| 电击 | 周期性电击目标和附近的敌人。 |
| 毒素 | 绕过护盾，对生命值造成持续伤害。 |
| 爆炸 | 延时引爆。到十层或宿主死亡时提前引爆。 |
| 辐射 | 使受影响的生物陷入混乱，可能转而攻击附近生物。 |
| 腐蚀 | 同时削减护甲和盔甲韧性。 |
| 病毒 | 放大生命伤害。 |
| 毒气 | 产生毒云，宿主死亡后还会短暂残留。 |
| 磁力 | 放大护盾伤害并抑制回复。破盾时触发电击。 |

武器属性按 `(基础值 + 初始加算) × (1 + 乘算区加成之和) × 各独立乘区倍率 + 外围加算` 计算。以 1.5 多重为例，一次射击有相同概率射出一发或两发，分别为单发 1.5 倍伤害或每发 0.75 倍。完整蓄力时间除以射速倍率，向上取整到游戏刻。

默认 `linear` 模式下，受护甲影响的伤害乘以 `15 / (15 + 护甲) × 8 / (8 + 韧性)`。服务端可以通过配置中的 `armorMode` 改用 `none` 或 `improved_vanilla`。

## 自定义显示

| 指令 | 可调内容 |
| --- | --- |
| `/ya client combo` | 连击显示的位置和大小 |
| `/ya client buffs` | 武器增益的位置、大小和读秒 |
| `/ya client status` | 世界内的触发特效，可逐类开关 |
| `/ya client status_count` | 头顶状态图标和排列方式 |

用 `/ya visibility <seeOthers> <shareOwn>` 控制是否看见他人的伤害数字，以及是否向他人显示自己的数字。`/ya stats` 可以查看手持武器的属性和加成，`/ya release <mod>` 可以远程解除自己拥有的 Mod 的占用。

## 数据包

内置 JSON 提供了可直接参考的数据格式。

- [武器属性](../common/src/generated/resources/data/minecraft/weapon_stats)使用 `data/<物品命名空间>/weapon_stats/<物品路径>.json`。
- [生命材质](../common/src/generated/resources/data/yagens_attributes/health_materials)和[护盾倍率](../common/src/generated/resources/data/yagens_attributes/shield_materials)可以分别调整。
- [弱点](../common/src/generated/resources/data/yagens_attributes/weak_points)支持模板和逐实体配置。后者位于 `data/<实体命名空间>/weak_points/entities/`。
- [Mod 定义](../common/src/generated/resources/data/yagens_attributes/mods)记录适用范围、容量、品质和词条数值。
