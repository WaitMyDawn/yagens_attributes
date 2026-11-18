# Yagen's Attributes

Weapon damage composition, tiered critical hits and shared Mod management.

The catalog includes 36 Mods and 236 rank variants, including conditional melee bonuses and three Prime variants with gold outline/sweep animations. Ordinary/Prime counterparts and all three Fury variants are mutually exclusive. Critical Delay conflicts with Bone Finger Box, and additionally with Blood Rush on tridents. Rejected installations flash both conflicting cards red twice. Weapon experience absorption is disabled by default; enable it beside the level display in the weapon panel. Only enabled held weapons below rank 30 share earned XP; worn armor and stowed weapons never participate.

## Installation

Install the release JAR on the server and all clients.

| Minecraft | Loader | Java | Additional dependency |
| --- | --- | --- | --- |
| 1.21.1 | NeoForge 21.1.235 | 21 | — |

## Combat

Weapon profiles supply critical chance, critical multiplier, status chance, status duration in seconds and damage weights. Installed weapon Mods and the current player's bonuses share the same calculation:

`(base + sum(addin)) * (1 + sum(addition)) * product(multiply) + sum(addout)`

Chance `1` means 100%. A 250% critical chance guarantees tier 2 with a 50% chance of tier 3. Critical damage is `1 + tier * (criticalMultiplier - 1)`.
Damage weights split the incoming damage before armor, resistance and protection enchantments. Weapon bonuses, critical damage and the volley share are applied at that stage. Life material applies only to the defended health portion; shields have a separate damage-type table. Vanilla attack cooldowns, enchantments, jumping critical hits and other reduction stages remain active.

Only profiled melee player attacks and projectiles with an explicit source weapon receive weapon combat calculations. Other ordinary player damage receives white numbers; secondary statuses retain their inherited critical display tier.
Supported player-fired projectiles capture their weapon profile and modifier sources at launch, so changing equipment, Mods or player bonuses in flight does not change that volley. The snapshot persists with the projectile. Untracked projectiles that provide an explicit source weapon retain the existing impact-time fallback.

Status rolls produce weighted damage types and launch/hit snapshots through `StatusProcEvent`. All thirteen status types have stacking, damage or control behavior. Hits absorbed entirely by shields can proc. Secondary damage inherits its original modifiers and critical damage without rerolling criticals or statuses. Primary elements combine automatically in installed-slot order, then player bonuses, then innate elements. Duplicate elements merge at their first position; existing combined elements stay independent.

## Ranged attributes

`fire_rate` and `multishot` both default to 1.0 and use the same addin/addition/multiply/addout buckets as other stats. Their effective values are bounded to 0–64. Bundled bow, crossbow and trident profiles, including family defaults, explicitly set both to 1.0. Ranged tooltips, weapon panels and `/ya stats` show the resolved values; fire rate is displayed as a multiplier.

Fire rate advances the existing charge clock: full charge takes the native duration divided by fire rate, rounded up to whole game ticks with a one-tick minimum. Bow launch power, crossbow Quick Charge and trident charge thresholds keep their native rules. Slower values also work; zero prevents charge progress. Each use action has its own fractional clock, with a server-authoritative rate and a sequence-checked reply for client prediction. Other players' local animations are not individually synchronized by this rate packet. Fire rate affects charging, rather than automatically repeating fire or accelerating projectile flight.

Multishot directly determines the entire shot's projectile count, replacing the vanilla Multishot enchantment's count. Native ammunition loading produces one template round. For M≥1, N=floor(M)+Bernoulli(frac(M)); every spawned projectile receives a final damage factor M/N. Thus 1.5 yields one projectile at ×1.5 or two at ×0.75, with equal probability. Positive values below one produce one projectile at ×M; zero emits none and preserves ammunition/loaded ammunition for supported native use paths.

A volley shares launch-time weapon stats and modifier values. Copies preserve the native projectile's serialized ammunition, enchantment state, critical-arrow flag, position and motion, while receiving distinct entity identities. Damage types and material multipliers are evaluated against each target; critical/status outcomes are still rolled independently for each hit. The M/N factor applies after component bonuses and critical calculation, before native defense, so absolute addin/addout damage is divided too. The panel's component amounts remain the single-projectile baseline before this volley factor. Missed or blocked projectiles do not transfer damage to other projectiles.

Only one native shot's ammunition and durability are consumed. Additional arrows cannot be picked up. Additional tridents cannot return or drop recoverable items, including on owner death. If entity creation or spawn acceptance reduces the actual count, surviving projectiles are normalized to that actual count. A failed stat calculation still retains the volley factor on fallback native damage.

Player ranged hits bypass the damage cooldown check while leaving the target's existing cooldown and prior-damage state intact. Shields, armor, absorption and actual immunity still apply. This supports consecutive hits without making unrelated melee attacks ignore their cooldowns.

Native bow/crossbow firing, crossbow fireworks and thrown tridents are covered, including subclasses using those launch paths. Riptide keeps its native movement behavior. Custom weapons that replace these methods or projectiles with nonstandard serialization need an explicit adapter; `RangedShooting` supplies the shared snapshot/spawn entry points. Combat still requires player-attributed damage.

## Armor mitigation

Server setting `armorMode` in `config/yagens_attributes/yagens_attributes-server.toml` accepts exactly `none`, `improved_vanilla` or `linear` (default). `none` leaves the armor formula to Minecraft or other mods. `improved_vanilla` selects the legacy project's damage-dependent curve. `linear` uses `remaining damage = damage × 15/(15+armor) × 8/(8+toughness)`, with coefficients controlled by `linearArmorScale` and `linearToughnessScale`. The previous `fixedArmor`, `armorScale` and `armorToughnessScale` keys are superseded; explicitly select the new mode when updating an existing config.

Native armor durability, bypass tags, Breach and subsequent protection/resistance stages remain active. Shields retain their armor bypass. The rule applies to living targets regardless of attacker type. The armor Mixin uses priority 2000; competing formula replacements still require integration testing. Custom modes expose `ArmorPenetrationEvent`, applying combined percentage penetration before flat penetration separately to armor and toughness, without modifying the target's attributes. No penetration attributes are registered yet.

With default linear settings and no enchantments, full leather/gold/chainmail/iron/diamond/netherite sets reduce armor-affected damage by 31.82% / 42.31% / 44.44% / 50.00% / 78.57% / 82.86%. Material multipliers, status effects, shields and enchantments are separate.

DOT bases are captured directly before defense, including the hit's original critical multiplier and volley share. A pulse reads current target defenses and never reapplies weapon bonuses or rolls new critical/status chances. Linear mode lets independent layers share one armor calculation; none and improved_vanilla retain their per-layer nonlinear treatment. Heat uses its combined pulse.

## Shields and status effects

Absorption hearts are shields. Natural shields are configured with attributes, for example:

```mcfunction
attribute @s yagens_attributes:max_shield base set 4
attribute @s yagens_attributes:shield_regen base set 2
```

Natural maximum defaults to 0 and recharge to 2 points per second. A positive natural maximum enables recharge after 3 seconds without actual health/shield damage. Recharge stops at that maximum; external absorption sources can fill the combined pool to four times the natural maximum. A natural maximum of 4 therefore allows 16 total shields. Repeated absorption effects add their usual amount. With natural maximum 0, vanilla absorption-source behavior is retained.

Shield hearts are blue and become purple while overloaded. Breaking an eligible shield protects health from that hit's ordinary overflow and briefly displays a lock on the health bar. Eligibility requires a real natural recharge gain and is consumed once; external refills alone cannot rearm it. Protection is 0.25–0.5 seconds, with 20 pre-hit shields giving 0.4 seconds, rounded up to ticks. Toxin bypasses shields and this protection. Eligibility and timers survive logout/unloading. More than 40 shield points use a bounded heart display plus the actual number.

Corrosive removes 26% armor and toughness initially, adding 6% per stack up to 80%. Heat gradually removes up to 50% armor while retaining toughness; the reductions multiply. Viral increases health damage from 2x to 4.25x, while Magnetic applies those multipliers to shields and prevents recharge. Slash bleeding bypasses armor and still spends shields. Electricity shocks nearby targets, Gas leaves a lingering cloud on death, and Blast detonates early at ten stacks or host death without damaging blocks. Cold, Impact, Puncture and Radiation supply their corresponding slow/freeze, stagger, weakening and confusion rules. Detailed coefficients and Minecraft-specific boundaries are maintained in `Spec.md`.

Status layers are independent of MobEffects, milk and `/effect`. The status API exposes authoritative state to integrations; head markers are rendered from independent layer-count snapshots, using each damage icon followed by its true stack count. Client options statusMarkers (true), statusMarkersPerRow (3), statusMarkerHeightGap (0.2 entity heights) and statusMarkerRange (32 blocks) control this display. Rows grow upward from a fixed bottom anchor above the entity, have no background panel, and respect walls, invisibility and hidden GUI. Status visuals use sparse particles and the existing 32×32 icons. Client options `restoreNormalShieldColor = false` and `restoreOvershieldColor = false` independently restore vanilla absorption hearts (normally yellow) when enabled. `showShieldGateLock = true` independently controls the protection lock without changing gameplay. The existing `shieldHearts` switch remains a master switch for custom shield colors only; disabling it restores both colors but does not disable the lock. Large shield-number colors follow the same selection. `statusVisuals` controls status particles. These options are in `config/yagens_attributes/yagens_attributes-client.toml`. Datapacks may append `yagens_attributes:control_immune`, `yagens_attributes:status_immune` and `yagens_attributes:immune_to/<type>` entity-type tags. Only the dragon and wither are control-immune by default. Warframe-specific Parazon finishers, Overguard, cross-owner Heat Inherit and player Radiation interface behavior are not emulated.

## Independent Mod storage

Weapons carry their UUID and intrinsic components for polarity, XP absorption and catalyst use. Mod instances, owners, weapon reservations, sharing permissions, player bonuses and visibility preferences live in world-level saved data. A Mod remains in its owner's independent storage while installed, and can reserve only one weapon. Taking a shared Mod changes ownership centrally, including when its previous owner is offline.

When a weapon breaks or a dropped weapon is destroyed or expires, its installed Mods return to their owners' storage, including offline owners. Dropping, picking up, and trading a weapon preserve its setup. Creative middle-click copies and saved hotbar templates start as independent rank-zero weapons with their default slots. Lost reservations can also be released from the owner's Mod storage.

## Endo

Independent Mod storage also holds a nonnegative integer Endo balance for each player, up to 9,223,372,036,854,775,807. It persists centrally through logout, death and restarts. One physical Endo item equals one stored unit. The item appears under Ingredients in Creative mode and can also be obtained with `/give @s yagens_attributes:endo 64`; no recipes, drops or essence blocks are added.

Right-click while holding Endo to deposit the entire held stack, including in Creative mode. A balance overflow rejects the whole deposit and retains the items. The item artwork reuses the legacy Mod essence icon, exported at 32×32 with every source pixel preserved.

The storage screen shows the Endo icon and balance beside Search. Hover for the full balance and “Click to withdraw”; click to open the quantity dialog. Only positive ASCII whole numbers are accepted. The dialog shows the current balance and inventory-limited maximum, disables invalid submissions and supports Enter to submit or Escape to cancel. The server repeats every check against the current balance and inventory. It fills compatible partial stacks and empty main-inventory slots, using at most 64 items per stack; armor and off-hand slots are excluded. Insufficient funds or space reject the complete withdrawal without debiting or dropping items. Requests never specify another player's account.

## Commands

Hold the relevant weapon in the main hand. Ordinary commands require a player source. Administrative commands require permission level 2.

| Command | Purpose |
| --- | --- |
| `/ya mods` | List owned Mods and Mods installed on the held weapon |
| `/ya status <targets> <type> <base> <seconds> <stacks>` | Admin: apply a sourced status for debugging; seconds scales the six-second weapon baseline, stacks 1–64 |
| `/ya stats` | Inspect weapon stats, distribution and active modifier sources |
| `/ya install <mod UUID>` | Reserve an owned, unoccupied Mod for the held weapon |
| `/ya uninstall <mod UUID>` | Release a reservation on the held weapon, retaining the original owner |
| `/ya release <mod UUID>` | Remotely release an installed Mod currently owned by the sender; no weapon or operator permission required |
| `/ya take <mod UUID>` | Uninstall and transfer a permitted Mod into independent storage |
| `/ya share <mod UUID> <true/false>` | Allow or forbid other holders to uninstall/take an owned Mod |
| `/ya shareweapon <true/false>` | Set this permission for all owned Mods on the held weapon |
| `/ya visibility <seeOthers> <shareOwn>` | Persist both damage privacy preferences; both default to true |
| `/ya grant <player> <stat> <operation> <value>` | Admin: create a Mod instance in independent storage |
| `/ya playerbonus <player> <stat> <operation> <value>` | Admin: set a player bonus bucket; multiply appends an independent factor |
| `/ya clearbonus <player> <stat>` | Admin: clear all player bonus buckets for a stat |
| `/ya modmeta <mod UUID> <category> <rarity> <polarity> <name...>` | Admin: edit a Mod's explicit classification and card metadata |
| `/ya modcost <mod UUID> <cost>` | Admin: set capacity cost while preserving the Mod's other metadata |
| `/ya weaponxp <amount>` | Admin: add experience to the held weapon without taking player experience |

Stats: `critical_chance`, `critical_damage`, `status_chance`, `status_duration`, `fire_rate`, `multishot`, `damage`, `damage.<type>`.
Operations: `addin`, `addition`, `multiply`, `addout`. `multiply` takes a full factor, such as `1.5`.
Each grant creates one modifier source; multiple Mods may affect the same stat. Player addin/addition/addout commands replace only the specified bucket and preserve other buckets. Clear the stat before rebuilding its multiplier list.

For the diamond sword example, grant/install separate `critical_chance addition 0.2` and `critical_chance multiply 1.5` Mods, then apply player bonuses `addition 0.4` and `multiply 1.2`. `/ya stats` reports `0.432` critical chance.
Specific drop sources/probabilities, polarity capacity discounts and polarity upgrade costs remain undefined.

## Tooltips and panels

Weapon attributes are hidden by default in item tooltips; hold Shift to expand them. Expanded attributes show level/experience, critical chance, critical damage (for example `2x`), status chance, status duration, numeric damage components with their icons and total damage. Composite elements appear first, then single elements, then physical types. The actually rendered tooltip requests a bounded, cached server inspection of its authorized menu slot. Inspection and panel replies use the same installed Mods, player bonuses and active combo/kill modifiers; panel replies seed the tooltip cache immediately. Unsynchronized inventory items show a loading message; standalone template previews are explicitly labeled.

| Default binding | Panel |
| --- | --- |
| Shift+M | Independent Mod storage, with ownership, reservation status and install controls |
| Shift+Y | Main-hand weapon details, base/current stats, damage components and installed Mods |

Bindings can be changed under Controls → Yagen's Attributes. Open them while in game with no other screen open. Panels also provide tabs to switch between the two views.
Mod metadata uses a space-separated applicability row, then Level x / polarity glyph / capacity cost. Quality colors apply to the Mod name and modifier lines; quality names are omitted. Maximum Mod rank text is cyan. Card names are centered without a separate background, and the detail-pane name is centered too. Hold Shift to reveal permissions, the bound weapon and ownership details. Mod levels show no maximum; weapon progression still shows its 30-level range. Each modifier uses one value-first line such as +90% Damage, x1.2 Damage or +3 Damage. Eight source PNGs in art/polarity supply the named symbols; runData exports their tintable alpha silhouettes at 32x32 and provides the neutral dash separately. Source attribution is in art/polarity/Sources.md. Universal Aura/Omni polarity uses the JSON key omni and its own glyph.

The storage screen uses a 6-by-3 square card grid (18 cards per page) with search, hierarchical category filters and quality/quantity/name ordering. Click the sort control to cycle ordering. Filtering, duplicate grouping and ordering apply to the entire owned inventory before pagination, not only the displayed page. Cards group copies only when metadata and complete modifier definitions match; their quantity and availability counts come from actual ledger entries. Chinese name ordering uses Chinese collation.

Categories: Warframe or Weapon; Weapon contains Melee and Ranged; Melee contains Sword, Axe, Trident and Mace; Ranged contains Bow and Crossbow. Compatibility follows the explicit parent tree, so general Melee Mods work on every melee subtype. Names and item registry spelling never determine compatibility.

The weapon screen uses eight compatible cards per page and places compact numeric stats and level/XP progress on the left, four-by-two slots in the middle, selected Mod details on the right and compatible, unoccupied owned Mods below. Capacity appears once above the slots. The header shows operable weapons from hotbar slots 1–9 in their original relative order; click an icon to switch the held slot and load its server-side panel. Operable items require a synchronized profile, count one and maximum stack size one. Icons use Minecraft's item model/glint renderer without durability/count overlays. Switching clears the previous weapon's panel and gesture state until the server replies. Metadata and modifier lines fit on single lines; expanded details support scrolling. Modifiers with more than eight independent factors show an explicitly abbreviated preview, while combat uses every factor.

Double-click an available card within 300 ms to install it into the selected empty slot or first free slot. Double-click an installed card, right-click its slot, or drag it into the lower Mod area to return it to independent storage. Returning another owner's shared Mod transfers it to the operator; private Mods cannot be manipulated. Drag cards into empty slots to install, or between slots to move; dropping onto an occupied slot atomically swaps them after checking permission for both. On the storage screen, the held-weapon icon is an install drop target. To obtain a physical Mod item, first return it to independent storage, then drag its card onto the inventory icon at the bottom left. The arrow icon upgrades with Endo; the lock toggles sharing. Hover icons for explanations. Escape or dropping outside a valid target cancels dragging; scrolling or hovering slot-page arrows supports weapons with more than eight slots. Server-side ownership, compatibility, capacity and current-weapon checks remain authoritative. Use Refresh for external changes.

Actual Mod damage is calculated in `LivingIncomingDamageEvent` before armor, resistance and protection; it does not rewrite the item or player attack-damage attribute. Vanilla attack-damage tooltips and integrations reading that attribute therefore retain their original values. Writing the same bonuses into that attribute as well would apply them twice; attribute-based integrations require an explicit boundary.

The damage components and total are resolved from the profile's `base_damage`, installed Mods and the current player's bonuses. They are numeric full-charge baseline damage amounts, excluding target mitigation and critical multipliers, rather than percentages or a synthetic 100-damage reference. Actual hits retain Minecraft's cooldown, enchantment, projectile-speed, smash and target-dependent rules; bow/crossbow baselines are representative full-charge arrow values, and trident uses its melee baseline.

Weapon levels range from 0 to 30. At level L, the next level needs `7 + 2L` XP, matching the legacy unpolarized curve; reaching level 30 takes 1080 XP in total. Positive player XP is divided between eligible main/off-hand weapons before reaching the player. Duplicate weapon UUIDs count once, rounding never creates XP, and XP not absorbed by a weapon remains with the player. Armor is not an eligible weapon in this foundation. Level changes update an open panel through a compact progress packet.

Mod capacity equals weapon level. Defined Mods use `base + per_level × level` capacity; legacy custom Mods retain their explicit cost (default 1). Survival installation and upgrades check the resulting total; Creative bypasses only this capacity check, preserving ownership, type and slot restrictions. Polarity symbols do not alter the cost yet. Existing/Creative-installed over-capacity builds are preserved and can still be used/uninstalled. Weapons default to eight slots and support 1–64 configured polarities. Schemas 1–4 migrate to schema 5 without changing Mod UUIDs or ownership; schema 4 Endo balances are preserved and earlier saves begin with zero Endo.
All operations are checked by the server against ownership, sharing permissions and the currently held weapon UUID. An outdated panel cannot operate on a different weapon after a hand change. Panels do not pause the game.

Damage numbers scatter in a randomized annulus around the target and drift outward/upward. Successive labels use a golden-angle sequence to spread dense bursts instead of repeatedly covering the entity's center. Text renders through the model, follows the interpolated target position and retains its last anchor if the target disappears.
Client and server must use matching protocol version 34.

All subject/status artwork uses 32×32 textures. The first 14 Mod images and fallback Mod image are opaque white placeholders. The 13 existing status icons retain their original designs, transparent backgrounds and minor pixel/shading refinements. Upgrade slots are 128×128 logical units, warehouse cards 124×124 and compatible cards 96×96 on a 1100×600 canvas. Subject-art areas use 64×64, 64×64 and 32×32 respectively; UI scale adapts the complete canvas to the screen. Frames, text and symbols remain separate from subject artwork.

The Yagen's Mods Creative tab includes 89 rank variants across 14 definitions, ordered by ascending quality, then definition ID, then ascending level within each definition. All share the physical item ID `yagens_attributes:mod`; its `yagens_attributes:mod_data` component holds the definition ID and level. Right-click a stack to deposit its real copies into independent storage, open storage with Shift+M, and select a card to upgrade or withdraw it. Withdrawals require an owned, uninstalled Mod and free inventory space. Installation does not consume the account instance; each weapon accepts at most one copy of a definition.

Bundled Mods use Addition by default, including base-damage, elemental and multishot entries; Flame Feather's damage penalty joins the same global Addition bucket. An omitted JSON `operation` defaults to `addition`. Mod levels include zero: each entry scales as `value × (level + 1)`, and independent multiplier entries become `1 + scaled value`. Upgrading from level L costs `rarity.base_endo × 2^L` Endo. The four elemental additions use an additive coefficient relative to total attack damage, so they can introduce an element absent from the weapon. Fire-rate and multishot Mods apply to ranged weapons and tridents. `/ya upgrade <Mod UUID>` and `/ya withdrawmod <Mod UUID>` use the same checks as the panels.

Mods have no crafting recipes. Each definition has a standalone loot table, for example `/loot give @s loot yagens_attributes:mods/multishot`. These templates are not attached to any mob or chest until drop sources and probabilities are defined. A levelled physical item can also be created with `/give @s yagens_attributes:mod[yagens_attributes:mod_data={type:"yagens_attributes:multishot",level:5}]`.

Mod definitions are `data/<namespace>/mods/<path>.json`; qualities and polarities are under `mod_rarities/` and `mod_polarities/`. Entry `description` fields contain English editing guidance, independent of display translations. Data-pack reload recompiles ranks while preserving account UUIDs, ownership and installations. Missing definitions suspend their bonuses while retaining the instance, allowing restoration or withdrawal. Physical models are `assets/<namespace>/models/item/mods/<path>.json`, with the default at `models/item/mod.json`.

## Data packs

Weapon profiles use `data/<item namespace>/weapon_stats/<item path>.json`. Override a bundled profile with the same resource ID and run `/reload`.

```json
{
  "critical_chance": 0.15,
  "critical_damage": 2.0,
  "status_chance": 0.15,
  "status_duration": 6.0,
  "damage_distribution": {"impact": 0.1, "puncture": 0.45, "slash": 0.45},
  "base_damage": 7.0,
  "category": "sword",
  "mod_slots": ["none", "none", "none", "none", "none", "none", "none", "none"]
}
```

Default profiles include all six swords, all six axes, bow, crossbow, trident and mace. Critical/status chances are retained from the legacy profiles. All default critical multipliers are 2 and status durations are 6 seconds; the legacy mace's additive critical bonus is not copied into an absolute multiplier.

| Weapon | Critical chance | Status chance | Impact / puncture / slash |
| --- | --- | --- | --- |
| Wooden/stone sword | 10% | 10% | 10 / 45 / 45 |
| Iron sword | 12% | 12% | 10 / 45 / 45 |
| Golden sword | 10% | 30% | 10 / 45 / 45 |
| Diamond sword | 15% | 15% | 10 / 45 / 45 |
| Netherite sword | 18% | 18% | 10 / 45 / 45 |
| Wooden/stone axe | 10% | 10% | 20 / 10 / 70 |
| Iron axe | 12% | 12% | 20 / 10 / 70 |
| Golden axe | 10% | 20% | 20 / 10 / 70 |
| Diamond axe | 15% | 15% | 20 / 10 / 70 |
| Netherite axe | 18% | 18% | 20 / 10 / 70 |
| Bow | 40% | 20% | 10 / 70 / 20 |
| Crossbow | 30% | 25% | 10 / 70 / 20 |
| Trident | 25% | 15% | 20 / 70 / 10 |
| Mace | 28% | 5% | 100 / 0 / 0 |

Class defaults live in `data/yagens_attributes/weapon_defaults/classes.json`. A specific `weapon_stats` profile wins; otherwise real subclasses of `SwordItem`, `AxeItem`, `TridentItem`, `BowItem`, `CrossbowItem` or `MaceItem` inherit their family template. Item names, registry spelling and unrelated tool classes do not establish a match. Sword/axe defaults use 15% critical/status chance; the other families retain the corresponding vanilla defaults above. All templates have eight neutral slots.

Override this defaults file at the same resource ID to change family behavior. The root uses `schema_version: 1` and a `classes` object keyed by `sword`, `axe`, `trident`, `bow`, `crossbow` and `mace`. Each value uses the weapon profile fields; `category` defaults to its family. Duplicate family definitions across different resource IDs reject the reload.

Omitting `base_damage` in a class template derives its panel baseline once per reload from the default ItemStack's main-hand attack-damage modifiers and the vanilla player's unequipped baseline of 1. Bow/crossbow templates explicitly supply 6/7. Weapons with dynamic per-stack attributes or a different projectile damage model need an explicit profile or adapter for an accurate panel baseline; their actual hits keep their native damage pipeline. A failed native attribute inspection skips that fallback item with a warning. Invalid profile/template data retains the previous complete weapon table. The server synchronizes resolved profiles to clients, including third-party items.

Material rules use `data/<namespace>/health_materials/<name>.json`. Eight explicit vanilla life-material profiles ship by default; unclassified entities remain neutral. See [the full assignments and multiplier table](docs/Materials.md). Shields use the separate `data/yagens_attributes/shield_materials/default.json`. Higher priority wins; equal priorities sort by full resource ID. Unlisted damage types have multiplier 1. Matching is by explicit entity IDs or entity-type tags:

```json
{
  "priority": 10,
  "entities": ["minecraft:skeleton"],
  "entity_tags": [],
  "multipliers": {"impact": 1.2, "puncture": 0.8}
}
```

This material JSON is a documentation example, not a bundled balance rule.
Optional profile fields default to `base_damage: 1`, `category: weapon` and eight neutral slots. Custom weapons should supply an explicit baseline and category. Slot/card polarities: `none`, `madurai`, `vazarin`, `naramon`, `zenurik`, `unairu`, `penjaga`, `umbra`, `omni`. Rarities: `common`, `uncommon`, `rare`, `legendary`, `riven`, `galvanized`.
`/ya modmeta` accepts a literal name with spaces; unnamed cards derive their display name from their first stat using bundled English/Chinese translations. A renamed card retains its UUID and bonuses.
Types: `impact`, `puncture`, `slash`, `cold`, `heat`, `electricity`, `toxin`, `blast`, `radiation`, `corrosive`, `viral`, `gas`, `magnetic`.
Weights must be finite and nonnegative with a positive sum. Unknown fields fail the table reload and retain its previous table. Element fusion uses ordered sources after component values resolve; compound elements do not split or recombine.

## Configuration

The client TOML controls floating numbers, viewing other players' numbers, maximum live numbers (default 128) and lifetime (default 30 ticks).
The server TOML controls combat calculation, visibility range (default 64 blocks) and shield recharge delay (default 60 ticks). Privacy is enforced on the server before packets are sent; client preferences can further hide other players' numbers.
Critical tiers are capped at 100, procs at 64 per hit, durations at 3600 seconds and installed Mods at 64 per weapon.

## License

See [LICENSE.md](LICENSE.md).

For source builds, see [DEVELOPMENT.md](DEVELOPMENT.md).

## Arsenal additions

Lethal Torrent grants +60% fire rate and multishot at rank 5. Galvanized Chamber grants +80% multishot at rank 10 and +30% per ranged kill for 20 seconds, up to five stacks; it conflicts with Split Chamber (the renamed existing `multishot` definition). Quickening grants +40% attack speed and +20% chance of an additional combo count at rank 3. All use Addition. Launched projectiles and their DOT layers preserve the source weapon identity for kill rewards after switching equipment.

In Mod storage, toggle the cycle-symbol recycling tool, click a card and confirm a quantity, or drag a card onto the tool to recycle every eligible uninstalled copy. Endo is credited directly. Each card yields `2^(q-1) + floor(q*(2^rank-1)/2)`, where q is its quality's base Endo value; rounding occurs before multiplying by quantity. Installed cards and pending Riven choices are excluded; invalid quantities or balance overflow preserve all cards.

Hold an Orokin Catalyst in the offhand and right-click a supported mainhand weapon to permanently double rank-derived Mod capacity. Repeated use and inapplicable items are rejected. The item component survives polarization; rank 0 still has zero capacity and rank 30 has 60. No recipes or automatic loot-table injections are added. Lethal Torrent, Quickening and the Catalyst use white placeholders; Galvanized Chamber shares Split Chamber artwork.

In Mod storage, occupied copies appear as individual cards while free copies remain grouped. Right-click or double-click an owned occupied card to release its reservation remotely, or drag it to the broken-chain icon. Selecting it and pressing Delete also works. Ownership, rank and rolls remain intact; release never creates physical items. Shared holder permissions do not grant remote access, and a previous owner cannot release a Mod after ownership transfer. Future weapon calculations use the updated ledger; already launched projectiles retain their snapshots. No world scan or automatic lost-item cleanup is performed.

The storage toolbar's Filters button opens a tag builder. Drag tags from the left into the right pane: separate groups are AND conditions, joined tags within one group are OR alternatives. Hover a connection to preview its split, then click to split it into independent groups. Drag back left or right-click to remove a tag. Available tags cover all six qualities, occupied/unoccupied, rank zero/above zero, and max/below-max rank. The server filters individual copies before grouping and pagination, so counts cover the complete result.

Apply commits the draft; Cancel/Escape preserves the previous filter. Tab/arrows select tags, Enter adds a group, Shift+Enter joins the selected group, Delete removes a tag, Backspace splits a connection, Ctrl+Backspace clears the draft and Ctrl+Enter applies. Filters combine with search/category/sort and remain active for the current panel session. Empty filters impose no restriction. Unknown maximum ranks match neither max-rank tag. No recipes or textures are added.

## Loot authoring

Confirmed acquisition rules are maintained in Java and generated by runData: 40 mob/container/trial bindings plus the existing independent Enderman 10% ×1 Forma reward. [Loot sources](docs/Loot.md) documents exact pools, eligibility, counts and Mod quality/rank parameters. The editor is not required to build or use those drops.

The development editor source tree, local controller and Gradle hook have been removed from this mod. The reusable implementation and lifecycle controller live in the sibling [Yagen-Loot-Workbench skill](../Yagen-Loot-Workbench/skills/yagen-loot-workbench/SKILL.md). Attach it through that skill only when authoring requires the graphical editor. The reference default key is **`.`**, registered on the NeoForge Mod bus, with `/lootworkbench` as a local fallback.

Instance `.editor/loot-workbench/` drafts and backups remain available. Removal does not clear them or modify formal loot. The independent skill includes graphical source discovery, direct editing, supported probability previews, copy/batch operations, import recovery and Java/runData writeback contracts; other projects require an explicit host adapter.
