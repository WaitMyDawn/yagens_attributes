package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import yagen.waitmydawn.yagens_attributes.command.ModCommands;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

public final class AdapterTests {
    private static int checks;

    public static void main(String[] args) throws Exception {
        UUID owner = UUID.randomUUID(), visitor = UUID.randomUUID(), weapon = UUID.randomUUID(), mod = UUID.randomUUID();
        WorldState initial = new WorldState();
        initial.ledger.add(new ModLedger.Entry(mod, owner, null, false,
                Map.of("critical_chance", new Bonus(.1, .2, List.of(1.5, 1.2), .05))));
        initial.ledger.install(owner, mod, weapon);
        initial.ledger.share(owner, mod, true);
        initial.bonus(owner, "damage.cold", new Bonus(2, .3, List.of(), 1));
        initial.visibility(owner, new WorldState.Visibility(false, false));
        initial.progress(weapon, new WeaponProgress(12, 10));
        WorldState loaded = WorldState.load(initial.save(new CompoundTag(), null), null);
        equal(false, loaded.isDirty());
        equal(initial.ledger.all(), loaded.ledger.all());
        equal(initial.bonuses(owner), loaded.bonuses(owner));
        equal(initial.visibility(owner), loaded.visibility(owner));
        equal(initial.progress(weapon), loaded.progress(weapon));
        equal(WorldState.Visibility.DEFAULT, loaded.visibility(visitor));
        // No online player object participates in ownership transfer or subsequent persistence.
        loaded.ledger.uninstall(visitor, mod, weapon, true);
        equal(true, loaded.isDirty());
        WorldState transferred = WorldState.load(loaded.save(new CompoundTag(), null), null);
        equal(0, transferred.ledger.owned(owner).size());
        equal(visitor, transferred.ledger.owned(visitor).get(0).owner());
        equal(null, transferred.ledger.owned(visitor).get(0).weapon());
        equal(0, transferred.ledger.bonuses(weapon).size());
        equal(initial.bonuses(owner), JsonData.decodeBonuses(JsonData.encodeBonuses(initial.bonuses(owner))));
        var oldTag = initial.save(new CompoundTag(), null); oldTag.putInt("schema", 1);
        var migrated = WorldState.load(oldTag, null);
        equal(true, migrated.isDirty()); equal(0, migrated.ledger.installed(weapon).get(0).slot());
        equal(ModInfo.DEFAULT, migrated.ledger.owned(owner).get(0).info());
        migrated.ledger.metadata(mod, new ModInfo("测试名称", ModInfo.Category.MELEE, ModInfo.Rarity.RARE, ModInfo.Polarity.MADURAI));
        var savedAgain = WorldState.load(migrated.save(new CompoundTag(), null), null);
        equal(migrated.ledger.all(), savedAgain.ledger.all());
        var schema2 = initial.save(new CompoundTag(), null); schema2.putInt("schema", 2);
        var migrated2 = WorldState.load(schema2, null);
        equal(WeaponProgress.ZERO, migrated2.progress(weapon)); equal(1, migrated2.ledger.entry(mod).info().capacityCost()); equal(true, migrated2.isDirty());
        rejects(() -> WorldState.load(new CompoundTag(), null));
        rejects(() -> JsonData.decodeBonuses("{\"unknown\":{}}"));
        rejects(() -> JsonData.weapon(JsonParser.parseString("{\"damage_distribution\":{\"slash\":-1}}").getAsJsonObject()));
        rejects(() -> JsonData.weapon(JsonParser.parseString("{\"damage_distribution\":{\"slash\":1},\"typo\":1}").getAsJsonObject()));
        rejects(() -> JsonData.weapon(JsonParser.parseString("{\"damage_distribution\":{\"slash\":1},\"critical_chance\":\"0.5\"}").getAsJsonObject()));
        var dispatcher = new CommandDispatcher<CommandSourceStack>();
        ModCommands.register(dispatcher);
        for (String name : List.of("mods", "stats", "grant", "install", "uninstall", "take", "upgrade", "withdrawmod", "share", "shareweapon", "visibility", "playerbonus", "clearbonus", "modmeta", "modcost", "weaponxp"))
            equal(true, dispatcher.getRoot().getChild("ya").getChild(name) != null);
        defaults(); endo();
        System.out.println("Passed " + checks + " NeoForge adapter contract checks");
        PanelContracts.run();
        LootContracts.run();
        ComboContracts.run();
        ClientUiContracts.run();
        ExperienceContracts.run();
        RivenContracts.run();
        CycleContracts.run();
        PolarityContracts.run();
        RangedContracts.run();
        StatusContracts.run();
        MarkerContracts.run();
        ModTextContracts.run();
        HotbarContracts.run();
        ModContracts.run();
        ArsenalPreview.render();
        MarkerPreview.render();
        ShieldPreview.render();
        StatusIconPreview.render();
    }

    private static void endo() {
        UUID owner = UUID.randomUUID(), isolated = UUID.randomUUID();
        var state = new WorldState();
        equal(0L, state.endo(owner)); state.creditEndo(owner, 64); equal(true, state.isDirty());
        state.endo(isolated, Long.MAX_VALUE);
        var loaded = WorldState.load(state.save(new CompoundTag(), null), null);
        equal(64L, loaded.endo(owner)); equal(Long.MAX_VALUE, loaded.endo(isolated)); equal(false, loaded.isDirty());
        rejects(() -> loaded.creditEndo(isolated, 1)); equal(Long.MAX_VALUE, loaded.endo(isolated)); equal(false, loaded.isDirty());
        rejects(() -> loaded.endo(owner, -1)); equal(64L, loaded.endo(owner));
        loaded.endo(owner, 0); equal(true, loaded.isDirty());
        equal(0L, WorldState.load(loaded.save(new CompoundTag(), null), null).endo(owner));
        UUID weapon = UUID.randomUUID(), mod = UUID.randomUUID();
        state.progress(weapon, new WeaponProgress(19, 3));
        state.ledger.add(new ModLedger.Entry(mod, owner, weapon, true, Map.of("damage", new Bonus(0, .5, List.of(), 0)), new ModInfo("Persistent", ModInfo.Category.MELEE, ModInfo.Rarity.RARE, ModInfo.Polarity.MADURAI, 5), 3));
        var previous = state.save(new CompoundTag(), null); previous.putInt("schema", 3);
        var migrated = WorldState.load(previous, null); equal(0L, migrated.endo(owner)); equal(true, migrated.isDirty());
        equal(state.progress(weapon), migrated.progress(weapon)); equal(state.ledger.all(), migrated.ledger.all());
        var invalid = state.save(new CompoundTag(), null);
        invalid.getList("players", net.minecraft.nbt.Tag.TAG_COMPOUND).getCompound(0).putLong("endo", -1);
        rejects(() -> WorldState.load(invalid, null));
        var wrongType = state.save(new CompoundTag(), null);
        wrongType.getList("players", net.minecraft.nbt.Tag.TAG_COMPOUND).getCompound(0).putString("endo", "64");
        rejects(() -> WorldState.load(wrongType, null));
    }

    private abstract static class IrregularTool extends net.minecraft.world.item.SwordItem {
        private IrregularTool() { super(null, new net.minecraft.world.item.Item.Properties()); }
    }

    private static void defaults() {
        equal(Optional.of(WeaponFamilies.Family.SWORD), WeaponFamilies.match(IrregularTool.class));
        equal(Optional.of(WeaponFamilies.Family.AXE), WeaponFamilies.match(net.minecraft.world.item.AxeItem.class));
        equal(Optional.of(WeaponFamilies.Family.TRIDENT), WeaponFamilies.match(net.minecraft.world.item.TridentItem.class));
        equal(Optional.of(WeaponFamilies.Family.BOW), WeaponFamilies.match(net.minecraft.world.item.BowItem.class));
        equal(Optional.of(WeaponFamilies.Family.CROSSBOW), WeaponFamilies.match(net.minecraft.world.item.CrossbowItem.class));
        equal(Optional.of(WeaponFamilies.Family.MACE), WeaponFamilies.match(net.minecraft.world.item.MaceItem.class));
        equal(Optional.empty(), WeaponFamilies.match(net.minecraft.world.item.PickaxeItem.class));
        equal(Optional.empty(), WeaponFamilies.match(String.class));
        var id = net.minecraft.resources.ResourceLocation.parse("test:defaults");
        var document = JsonParser.parseString("{\"schema_version\":1,\"classes\":{\"sword\":{\"damage_distribution\":{\"slash\":1}},\"bow\":{\"damage_distribution\":{\"puncture\":1},\"base_damage\":8}}}");
        var rules = WeaponProfiles.defaults(Map.of(id, document));
        equal(true, rules.get(WeaponFamilies.Family.SWORD).deriveDamage());
        equal(ModInfo.Category.SWORD, rules.get(WeaponFamilies.Family.SWORD).profile().category());
        equal(false, rules.get(WeaponFamilies.Family.BOW).deriveDamage());
        equal(8D, rules.get(WeaponFamilies.Family.BOW).profile().baseDamage());
        rejects(() -> WeaponProfiles.defaults(Map.of(id, document, net.minecraft.resources.ResourceLocation.parse("other:defaults"), document)));
        rejects(() -> WeaponProfiles.defaults(Map.of(id, JsonParser.parseString("{\"schema_version\":2,\"classes\":{}}"))));
        rejects(() -> WeaponProfiles.defaults(Map.of(id, JsonParser.parseString("{\"schema_version\":1,\"classes\":{\"unknown\":{}}}"))));
        rejects(() -> WeaponProfiles.defaults(Map.of(id, JsonParser.parseString("{\"schema_version\":1,\"classes\":{\"sword\":{\"damage_distribution\":{\"slash\":1},\"typo\":1}}}"))));
    }

    private static void equal(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) throw new AssertionError(expected + " != " + actual);
        checks++;
    }

    private static void rejects(Runnable action) {
        try { action.run(); } catch (RuntimeException expected) { checks++; return; }
        throw new AssertionError("Expected validation failure");
    }
}
