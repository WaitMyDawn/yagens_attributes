package yagen.waitmydawn.yagens_attributes.core;

import yagen.waitmydawn.yagens_attributes.core.ui.*;
import java.util.*;
import static yagen.waitmydawn.yagens_attributes.core.ModFilter.Tag.*;

public final class ModFilterTests {
    private static int checks;
    public static void run() { matching(); edits(); geometry(); bounds(); System.out.println("Passed " + checks + " composable Mod filter, edit and layout checks"); }
    private static ModLedger.Entry entry(String id, int rank, boolean occupied) {
        var card = BuiltinMods.catalog().card("yagens_attributes:" + id, rank);
        return new ModLedger.Entry(UUID.randomUUID(), new UUID(1, 2), occupied ? new UUID(3, 4) : null, false, card.bonuses(), card.info(), occupied ? 0 : -1);
    }
    private static ModFilter filter(ModFilter.Tag... tags) { return new ModFilter(Arrays.stream(tags).map(List::of).toList()); }
    private static void matching() {
        var zero = entry("universal_nanocube", 0, false); var max = entry("universal_nanocube", 5, true);
        var legend = entry("primed_fury", 10, false); var galv = entry("galvanized_chamber", 10, false);
        var legacy = new ModLedger.Entry(UUID.randomUUID(), zero.owner(), null, false, Map.of(), new ModInfo("No recognizable suffix", ModInfo.Category.BOW, ModInfo.Rarity.RIVEN, ModInfo.Polarity.NONE), -1);
        check(ModFilter.NONE.matches(zero)); check(filter(UNRANKED, AVAILABLE, COMMON).matches(zero));
        check(!filter(LEVELED).matches(zero)); check(!filter(MAX_RANK).matches(zero)); check(filter(BELOW_MAX_RANK).matches(zero));
        check(filter(LEVELED, MAX_RANK, OCCUPIED).matches(max)); check(!filter(AVAILABLE).matches(max));
        var alternatives = new ModFilter(List.of(List.of(LEGENDARY, GALVANIZED), List.of(AVAILABLE), List.of(MAX_RANK)));
        check(alternatives.matches(legend)); check(alternatives.matches(galv)); check(!alternatives.matches(max));
        check(filter(RIVEN, UNRANKED).matches(legacy)); check(!filter(MAX_RANK).matches(legacy)); check(!filter(BELOW_MAX_RANK).matches(legacy));
        check(!filter(OCCUPIED, AVAILABLE).matches(max)); check(!filter(OCCUPIED, AVAILABLE).matches(zero));
        check(new ModFilter(List.of(List.of(OCCUPIED, AVAILABLE))).matches(max));
        for (var tag : ModFilter.Tag.values()) for (var other : ModFilter.Tag.values()) for (var sample : List.of(zero, max, legend, galv, legacy)) {
            check(new ModFilter(List.of(List.of(tag), List.of(other))).matches(sample) == (tag.matches(sample) && other.matches(sample)));
            if (tag != other) check(new ModFilter(List.of(List.of(tag, other))).matches(sample) == (tag.matches(sample) || other.matches(sample)));
        }
        var secondFree = new ModLedger.Entry(UUID.randomUUID(), zero.owner(), null, false, zero.bonuses(), zero.info(), -1);
        var sameBound = new ModLedger.Entry(UUID.randomUUID(), zero.owner(), new UUID(3, 4), false, zero.bonuses(), zero.info(), 1);
        var entries = List.of(zero, secondFree, sameBound, legend, max);
        var available = ModInventory.browse(entries, new ModInventory.Query("", ModInfo.Category.ALL, ModInventory.Order.NAME, filter(AVAILABLE, UNRANKED)), null, e -> "irregular.title");
        check(available.size() == 1 && available.get(0).quantity() == 2 && available.get(0).available() == 2);
        var occupied = ModInventory.browse(entries, new ModInventory.Query("", ModInfo.Category.ALL, ModInventory.Order.NAME, filter(OCCUPIED)), null, e -> "unrelated");
        check(occupied.size() == 2 && occupied.stream().mapToInt(ModInventory.Group::quantity).sum() == 2);
        check(ModInventory.browse(entries, new ModInventory.Query("absent phrase", ModInfo.Category.ALL, ModInventory.Order.NAME, filter(AVAILABLE)), null, e -> "other").isEmpty());
    }
    private static void edits() {
        var editor = new ModFilterEditor(ModFilter.NONE);
        check(editor.drop(RARE, -1, -1, -1, 0)); check(editor.drop(LEGENDARY, -1, -1, 0, 1));
        check(editor.drop(AVAILABLE, -1, -1, -1, 1)); check(editor.value().groups().equals(List.of(List.of(RARE, LEGENDARY), List.of(AVAILABLE))));
        var before = editor.value(); check(!editor.drop(RARE, -1, -1, 0, 0)); check(editor.value().equals(before));
        check(editor.split(0, 1)); check(editor.value().groups().equals(List.of(List.of(RARE), List.of(LEGENDARY), List.of(AVAILABLE))));
        check(editor.drop(LEGENDARY, 1, 0, 0, 1)); check(editor.value().equals(before));
        check(editor.drop(AVAILABLE, 1, 0, -1, 0)); check(editor.value().groups().equals(List.of(List.of(AVAILABLE), List.of(RARE, LEGENDARY))));
        check(editor.drop(LEGENDARY, 1, 1, 1, 0)); check(editor.value().groups().get(1).equals(List.of(LEGENDARY, RARE)));
        before = editor.value(); check(!editor.drop(RARE, 0, 0, 1, 1)); check(editor.value().equals(before));
        check(!editor.split(1, 0)); check(!editor.split(1, 2));
        editor.remove(0, 0); check(editor.value().groups().size() == 1);
        editor.clear(); check(editor.value().equals(ModFilter.NONE));
        var full = new ModFilterEditor(new ModFilter(Collections.nCopies(12, List.of(RARE, AVAILABLE))));
        check(!full.drop(RIVEN, -1, -1, -1, 0)); check(!full.split(0, 1)); check(full.value().groups().size() == 12);
    }
    private static void geometry() {
        for (var chip : ModFilterLayout.palette()) {
            check(ModFilterLayout.BANK.contains(chip.bounds().x(), chip.bounds().y()));
            check(ModFilterLayout.BANK.contains(chip.bounds().x() + chip.bounds().width() - 1, chip.bounds().y() + 27));
        }
        var value = new ModFilter(List.of(List.of(ModFilter.Tag.values()), List.of(AVAILABLE)));
        var layout = new ModFilterLayout(value, 0);
        check(layout.chips.size() == 13 && layout.links.size() == 11);
        for (var chip : layout.chips) check(layout.chip(chip.bounds().x() + 60, chip.bounds().y() + 14).equals(chip));
        for (var link : layout.links) check(layout.link(link.bounds().x() + 10, link.bounds().y() + 14).equals(link));
        check(layout.drop(1, 1) == null);
        check(layout.drop(600, layout.groups.get(0).bounds().y() + 30).group() == 0);
        check(layout.drop(1018, layout.groups.get(0).bounds().y() + 30).equals(new ModFilterLayout.Drop(-1, 1)));
        check(layout.drop(400, layout.chips.get(4).bounds().y() - 5).index() == 4);
        check(layout.drop(600, layout.groups.get(1).bounds().y() - 12).equals(new ModFilterLayout.Drop(-1, 1)));
        var many = new ModFilter(Collections.nCopies(12, List.of(AVAILABLE)));
        var overflow = new ModFilterLayout(many, 0); check(overflow.maximumScroll > 0);
        var scrolled = new ModFilterLayout(many, overflow.maximumScroll);
        var last = scrolled.chips.get(11); check(scrolled.chip(last.bounds().x() + 20, last.bounds().y() + 14).equals(last));
    }
    private static void bounds() {
        var tags = new ArrayList<>(List.of(RARE)); var groups = new ArrayList<List<ModFilter.Tag>>(); groups.add(tags);
        var safe = new ModFilter(groups); tags.clear(); groups.clear(); check(safe.groups().equals(List.of(List.of(RARE))));
        rejects(() -> new ModFilter(List.of(List.of()))); rejects(() -> new ModFilter(List.of(List.of(RARE, RARE))));
        rejects(() -> new ModFilter(Collections.nCopies(13, List.of(RARE))));
        rejects(() -> new ModFilter(Collections.nCopies(3, List.of(ModFilter.Tag.values()))));
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Mod filter contract " + checks); }
    private static void rejects(Runnable action) { try { action.run(); } catch (IllegalArgumentException expected) { checks++; return; } throw new AssertionError("Expected invalid filter"); }
}
