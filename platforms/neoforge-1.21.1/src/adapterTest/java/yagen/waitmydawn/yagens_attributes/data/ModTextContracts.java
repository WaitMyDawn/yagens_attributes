package yagen.waitmydawn.yagens_attributes.data;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import yagen.waitmydawn.yagens_attributes.client.StatText;
import yagen.waitmydawn.yagens_attributes.client.WeaponTooltip;
import yagen.waitmydawn.yagens_attributes.network.WeaponInspectionData;
import yagen.waitmydawn.yagens_attributes.setup.PanelService;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.PanelPayload;
import yagen.waitmydawn.yagens_attributes.text.ModText;
import java.util.*;

public final class ModTextContracts {
    private static int checks;
    private ModTextContracts() {}
    public static void run() throws Exception {
        var original = Language.getInstance();
        try {
            for (var language : List.of("zh_cn", "en_us")) {
                var data = new HashMap<String, String>();
                try (var input = ModTextContracts.class.getResourceAsStream("/assets/yagens_attributes/lang/" + language + ".json")) {
                    Language.loadFromJson(Objects.requireNonNull(input), data::put);
                }
                Language.inject(new Language() {
                    @Override public String getOrDefault(String key, String fallback) { return data.getOrDefault(key, fallback); }
                    @Override public boolean has(String key) { return data.containsKey(key); }
                    @Override public boolean isDefaultRightToLeft() { return false; }
                    @Override public FormattedCharSequence getVisualOrder(FormattedText text) { return FormattedCharSequence.forward(text.getString(), Style.EMPTY); }
                });
                String damage = language.equals("zh_cn") ? "伤害" : "Damage";
                equal(List.of("+90% " + damage), lines(Stats.DAMAGE, new Bonus(0, .9, List.of(), 0)));
                equal(List.of("x1.2 " + damage), lines(Stats.DAMAGE, new Bonus(0, 0, List.of(1.2), 0)));
                equal(List.of("+3 " + damage), lines(Stats.DAMAGE, new Bonus(3, 0, List.of(), 0)));
                equal(List.of("+3 " + damage), lines(Stats.DAMAGE, new Bonus(0, 0, List.of(), 3)));
                equal(List.of("-15% " + damage), lines(Stats.DAMAGE, new Bonus(0, -.15, List.of(), 0)));
                String heat = language.equals("zh_cn") ? "火焰伤害" : "Heat Damage";
                equal(List.of("+90% " + heat), lines("damage.heat", new Bonus(0, 0, List.of(), 0, 0, .9)));
                equal(List.of("+30% " + heat), lines("damage.heat", new Bonus(0, 0, List.of(), 0, .3)));
                equal(List.of("+10% " + (language.equals("zh_cn") ? "暴击几率" : "Critical Chance")), lines(Stats.CRITICAL_CHANCE, new Bonus(.1, 0, List.of(), 0)));
                equal(List.of("+3 s " + (language.equals("zh_cn") ? "触发时间" : "Status Duration")), lines(Stats.STATUS_DURATION, new Bonus(0, 0, List.of(), 3)));
                equal(List.of("x1.2 × 1.5 " + damage), lines(Stats.DAMAGE, new Bonus(0, 0, List.of(1.2, 1.5), 0)));
                equal(List.of(), lines(Stats.DAMAGE, Bonus.NONE));
                var catalog = BuiltinMods.catalog();
                var info = catalog.card("yagens_attributes:universal_nanocube", 5).info();
                equal(0x55FFFF, ModText.rank(info).getStyle().getColor().getValue());
                equal(null, ModText.rank(catalog.card("yagens_attributes:universal_nanocube", 0).info()).getStyle().getColor());
                equal(false, ModText.maxLevel(ModInfo.DEFAULT));
                equal(false, ModText.metadata(info).getString().contains(data.get(catalog.qualityName(info.rarity()))));
                var weapon = UUID.randomUUID(); var bonuses = Map.of(Stats.DAMAGE, PanelPayload.ModifierView.of(new Bonus(0, .9, List.of(), 0)));
                var collapsed = ModText.tooltip(info, "Prototype", bonuses, false, weapon, false);
                var expanded = ModText.tooltip(info, "Prototype", bonuses, false, weapon, true);
                equal(4, collapsed.size()); equal(6, expanded.size());
                equal(false, collapsed.stream().anyMatch(line -> line.getString().contains(weapon.toString())));
                equal(true, expanded.stream().anyMatch(line -> line.getString().contains(weapon.toString())));
                equal(info.color(), collapsed.get(0).getStyle().getColor().getValue());
                equal(info.color(), collapsed.get(3).getStyle().getColor().getValue());
                var rare = catalog.card("yagens_attributes:nameless_glove", 0).info();
                equal(rare.color(), ModText.bonuses(rare, bonuses).get(0).getStyle().getColor().getValue());
                var profile = new WeaponProfile(.15, 2, .15, 6, Map.of(DamageType.SLASH, 1D), 7, ModInfo.Category.SWORD, Collections.nCopies(8, ModInfo.Polarity.NONE));
                var resolved = PanelService.resolve(profile, List.of(catalog.card("yagens_attributes:universal_nanocube", 5).bonuses(), catalog.card("yagens_attributes:bone_finger_box", 5).bonuses()));
                var inspection = new WeaponInspectionData(resolved, new WeaponProgress(10, 3), 16, false);
                var weaponLines = WeaponTooltip.lines(profile, inspection, false).stream().map(line -> line.getString()).toList();
                equal(true, weaponLines.contains(data.get("stat.yagens_attributes.critical_chance") + ": 33%"));
                equal(true, weaponLines.contains(data.get("gui.yagens_attributes.total_damage") + ": 13.3"));
                equal(false, weaponLines.contains(data.get("stat.yagens_attributes.critical_chance") + ": 15%"));
                equal(true, weaponLines.stream().anyMatch(line -> line.endsWith(": 13.3") && line.contains(data.get("stat.yagens_attributes.damage.slash"))));
                equal(false, weaponLines.stream().anyMatch(line -> line.startsWith(data.get("stat.yagens_attributes.attack_speed") + ":")));
                equal(true, WeaponTooltip.lines(profile, new WeaponInspectionData(PanelService.resolve(profile,
                        List.of(Map.of(Stats.ATTACK_SPEED, new Bonus(0, .3, List.of(), 0)))), WeaponProgress.ZERO, 0, false), false)
                        .stream().anyMatch(line -> line.getString().equals(data.get("stat.yagens_attributes.attack_speed") + ": 1.3x")));
                equal(language.equals("zh_cn") ? "冰冻附加 Prime" : "Cold Addition Prime",
                        data.get("mod.yagens_attributes.primed_cold_addition"));
                equal(language.equals("zh_cn") ? "毒素附加 Prime" : "Toxin Addition Prime",
                        ModText.localizedName(catalog.card("yagens_attributes:primed_toxin_addition", 10).info()).getString());
                equal(2, WeaponTooltip.lines(profile, null, false).size());
                equal(data.get("tooltip.yagens_attributes.syncing"), WeaponTooltip.lines(profile, null, false).get(1).getString());
                equal(2, WeaponTooltip.lines(profile, new WeaponInspectionData(Map.of(), WeaponProgress.ZERO, 0, false), false).size());
                equal(data.get("tooltip.yagens_attributes.preview"), WeaponTooltip.lines(profile, null, true).get(0).getString());
                equal(language.equals("zh_cn") ? "关键延迟" : "Critical Delay", ModText.localizedName(catalog.card("yagens_attributes:critical_delay", 0).info()).getString());
                equal(language.equals("zh_cn") ? "狂暴 Prime" : "Fury Prime", ModText.localizedName(catalog.card("yagens_attributes:primed_fury", 10).info()).getString());
                var galvanized = catalog.card("yagens_attributes:galvanized_gaze_of_pain", 10);
                var conditionalLines = ModText.bonuses(galvanized.info(), Map.of()).stream().map(line -> line.getString()).toList();
                equal(1, conditionalLines.size());
                equal(false, conditionalLines.get(0).contains("gui.yagens_attributes"));
                equal(true, conditionalLines.get(0).contains("+40%"));
                equal(true, conditionalLines.get(0).contains(data.get("stat.yagens_attributes.direct_damage")));
            }
        } finally { Language.inject(original); }
        System.out.println("Passed " + checks + " concise Chinese/English Mod entry presentation checks");
    }
    private static List<String> lines(String stat, Bonus bonus) {
        return StatText.bonuses(Map.of(stat, PanelPayload.ModifierView.of(bonus))).stream().map(line -> line.getString()).toList();
    }
    private static void equal(Object expected, Object actual) { checks++; if (!Objects.equals(expected, actual)) throw new AssertionError(expected + " != " + actual); }
}
