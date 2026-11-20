# Player guide

[Back to README](../README.md) · [中文](player-guide.zh-CN.md)

## Find Mods and materials

| What you need | Where to look |
| --- | --- |
| **Mods and Endo** | Start with dungeon and mineshaft chests. Each of those chest tables has separate 80% bonus rolls for a Mod and for 1–4 Endo. |
| **Forma** | Endermen have a 10% chance to drop one. Apply it to a rank 30 weapon to change a slot’s polarity and reset the weapon’s rank. Matching polarities reduce the capacity cost. |
| **Veiled Rivens and Kuva** | Ancient City chests have a 10% chance to contain a Veiled Riven and can also contain Kuva. Ominous trial rewards and the Wither are other sources of Kuva. Unveil a Riven to discover its weapon and stats, then spend Kuva to choose between its existing and rerolled stats. |
| **Gallium and Orokin Catalyst** | Mine Gallium Ore between Y=-16 and 48, most often near Y=16, with a stone pickaxe or better. Smelt the ore or Raw Gallium in a furnace or blast furnace. Craft a Catalyst with four Gallium, two Iron Ingots, and one Amethyst Shard. Hold the Catalyst in your offhand and the weapon in your main hand, then right-click to apply it. |

Deposit physical Endo or Kuva with right-click. Use Endo to upgrade a card or recycle unused cards for Endo in Mod storage. Mods are found as loot.

For the full loot pools and their weights, see the [built-in chest rules](../common/src/main/java/yagen/waitmydawn/yagens_attributes/core/loot/BuiltinContainerLoot.java) and [mob rules](../common/src/main/java/yagen/waitmydawn/yagens_attributes/core/loot/BuiltinMobLoot.java).

## Read a fight

- The weapon panel displays damage by type. Different targets can resist or take extra damage from the same type. Primary elements combine in slot order, so moving a card can change the resulting element.
- Critical chance above 100% can reach higher critical tiers. The damage number changes color by tier, and criticals above red gain exclamation marks.
- Status icons above a target count its active stacks.
- Ranged weak-point hits can activate Argon Scope. Use **F3+B** to see configured weak-point regions alongside hitboxes.
- Melee hits build combo. Some Mods turn the current combo tier into critical chance or status chance. Conditional bonuses, including Galvanized stacks, appear on the weapon buff HUD.

### Status effects

| Damage type | Proc effect |
| --- | --- |
| Impact | Briefly staggers the target. |
| Puncture | Weakens the target’s outgoing attacks and increases its chance of taking a critical hit. |
| Slash | Applies bleeding that bypasses armor and can drain shields. |
| Cold | Slows the target and increases critical damage against it. Enough stacks can freeze susceptible mobs. |
| Heat | Burns the target and gradually strips armor. |
| Electricity | Shocks the target and nearby enemies in pulses. |
| Toxin | Deals damage over time directly to health. |
| Blast | Detonates after a delay, or sooner at ten stacks or when the host dies. |
| Radiation | Confuses affected mobs and can turn them against nearby mobs. |
| Corrosive | Strips both armor and armor toughness. |
| Viral | Amplifies damage to health. |
| Gas | Creates a toxic cloud that can linger after the host dies. |
| Magnetic | Amplifies shield damage and suppresses shield recovery. Breaking the shield triggers Electricity. |

Weapon stats combine Mod bonuses as `(base + sum(addin)) × (1 + sum(addition)) × product(multiply) + sum(addout)`. A shot with 1.5 multishot produces either one projectile at 1.5x damage or two at 0.75x each, with equal chances. Full charge time is the weapon’s regular charge time divided by its fire-rate multiplier, rounded up to a game tick.

The default `linear` armor mode multiplies incoming armor-affected damage by `15 / (15 + armor) × 8 / (8 + toughness)`. Servers can also select `none` or `improved_vanilla` through `armorMode` in the server config.

## Customize the HUD

| Command | Settings |
| --- | --- |
| `/ya client combo` | Combo position and size |
| `/ya client buffs` | Weapon buffs, size and countdown |
| `/ya client status` | World effects, including individual status toggles |
| `/ya client status_count` | Overhead status icons and their layout |

Use `/ya visibility <seeOthers> <shareOwn>` to choose whose damage numbers you see and whether others see yours. `/ya stats` lists the held weapon’s stats and modifiers, and `/ya release <mod>` frees a Mod you own from a weapon you no longer hold.

## Data packs

The bundled JSON files show each data pack format.

- [Weapon profiles](../common/src/generated/resources/data/minecraft/weapon_stats) use `data/<item namespace>/weapon_stats/<item path>.json`.
- [Life materials](../common/src/generated/resources/data/yagens_attributes/health_materials) and [shield multipliers](../common/src/generated/resources/data/yagens_attributes/shield_materials) can be tuned separately.
- [Weak points](../common/src/generated/resources/data/yagens_attributes/weak_points) support templates and separate per-entity files under `data/<entity namespace>/weak_points/entities/`.
- [Mod definitions](../common/src/generated/resources/data/yagens_attributes/mods) specify applicability, capacity, rarity and modifier values.
