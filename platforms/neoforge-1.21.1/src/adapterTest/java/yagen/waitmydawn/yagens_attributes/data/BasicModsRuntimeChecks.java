package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import yagen.waitmydawn.yagens_attributes.api.StatusApi;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.*;
import yagen.waitmydawn.yagens_attributes.registries.*;
import yagen.waitmydawn.yagens_attributes.setup.*;
import java.util.*;

final class BasicModsRuntimeChecks {
    private static int checks;
    static int run(ServerLevel level, JsonObject report) {
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("9f321c1d-8f6c-455e-ae73-14824a284399"), "[BasicModChecks]"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.getInventory().clearContent(); player.getInventory().selected = 0;
        player.setHealth(player.getMaxHealth()); player.setPos(80, 100, 80);
        var world = WorldState.get(player.server);
        for (var entry : world.ledger.owned(player.getUUID())) {
            if (entry.weapon() != null) world.ledger.uninstall(player.getUUID(), entry.id(), entry.weapon(), false);
            world.ledger.withdraw(player.getUUID(), entry.id());
        }
        experience(level, player, world);
        conditional(level, player, world);
        inspectionAndConflicts(player, world);
        report.addProperty("basic_mods_and_weapon_xp_checks", checks);
        return checks;
    }
    private static void experience(ServerLevel level, ServerPlayer player, WorldState state) {
        var sword = new ItemStack(Items.DIAMOND_SWORD); var bow = new ItemStack(Items.BOW);
        player.setItemInHand(InteractionHand.MAIN_HAND, sword); player.setItemInHand(InteractionHand.OFF_HAND, bow);
        UUID main = Components.identify(sword), off = Components.identify(bow);
        player.setExperienceLevels(0); player.experienceProgress = 0; player.totalExperience = 0;
        check(!Components.absorbsExperience(sword)); check(!Components.absorbsExperience(bow));
        player.giveExperiencePoints(10);
        check(player.totalExperience == 10 && state.progress(main).equals(WeaponProgress.ZERO) && state.progress(off).equals(WeaponProgress.ZERO));
        var toggle = new PanelRequest(900, PanelRequest.WEAPON, 0, PanelRequest.TOGGLE_EXPERIENCE, main, null);
        check(toggle.valid()); PanelService.handle(player, toggle); check(Components.absorbsExperience(sword));
        var copy = ItemStack.parseOptional(level.registryAccess(), (CompoundTag) sword.save(level.registryAccess()));
        check(Components.absorbsExperience(copy) && main.equals(copy.get(Components.WEAPON_ID.get())));
        player.giveExperiencePoints(5); check(state.progress(main).equals(new WeaponProgress(0, 5))); check(player.totalExperience == 10);
        bow.set(Components.ABSORB_EXPERIENCE.get(), true); player.giveExperiencePoints(5);
        check(state.progress(main).equals(new WeaponProgress(1, 1))); check(state.progress(off).equals(new WeaponProgress(0, 2)));
        PanelService.handle(player, toggle); check(!Components.absorbsExperience(sword));
        player.giveExperiencePoints(2); check(state.progress(off).equals(new WeaponProgress(0, 4))); check(state.progress(main).equals(new WeaponProgress(1, 1)));
        var stale = new PanelRequest(901, PanelRequest.WEAPON, 0, PanelRequest.TOGGLE_EXPERIENCE, UUID.randomUUID(), null);
        PanelService.handle(player, stale); check(!Components.absorbsExperience(sword));
        check(!new PanelRequest(902, PanelRequest.STORAGE, 0, PanelRequest.TOGGLE_EXPERIENCE, main, null).valid());
        state.progress(off, new WeaponProgress(30, 0));
        var stowed = new ItemStack(Items.DIAMOND_AXE); stowed.set(Components.ABSORB_EXPERIENCE.get(), true); UUID stowedId = Components.identify(stowed); player.getInventory().setItem(8, stowed);
        var armor = new ItemStack(Items.DIAMOND_CHESTPLATE); armor.set(Components.ABSORB_EXPERIENCE.get(), true); player.setItemSlot(EquipmentSlot.CHEST, armor);
        player.giveExperiencePoints(3); check(player.totalExperience == 13); check(state.progress(stowedId).equals(WeaponProgress.ZERO));
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY); player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
    }
    private static UUID install(ServerPlayer player, WorldState state, UUID weapon, String type, int rank, int slot) {
        var card = ModDefinitions.INSTANCE.catalog().card("yagens_attributes:" + type, rank); UUID id = UUID.randomUUID();
        state.ledger.add(new ModLedger.Entry(id, player.getUUID(), null, false, card.bonuses(), card.info(), -1));
        state.ledger.install(player.getUUID(), id, weapon, ModInfo.Category.SWORD, 8, slot, 30, true);
        return id;
    }
    private static Cow target(ServerLevel level) {
        var cow = new Cow(EntityType.COW, level); cow.setPos(82, 100, 80); cow.setNoAi(true);
        Objects.requireNonNull(cow.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(1000); cow.setHealth(1000); level.addFreshEntity(cow); return cow;
    }
    private static void conditional(ServerLevel level, ServerPlayer player, WorldState state) {
        var sword = new ItemStack(Items.DIAMOND_SWORD); player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        UUID weapon = Components.identify(sword); var profile = CombatData.WEAPONS.weapon(sword);
        state.bonus(player.getUUID(), Stats.CRITICAL_CHANCE, new Bonus(-profile.criticalChance(), 0, List.of(), 0));
        state.bonus(player.getUUID(), Stats.STATUS_CHANCE, new Bonus(-profile.statusChance(), 0, List.of(), 0));
        install(player, state, weapon, "universal_nanocube", 5, 0); install(player, state, weapon, "condition_overload", 5, 1);
        UUID frenzy = install(player, state, weapon, "berserker_fury", 5, 2);
        var victim = target(level);
        try {
            check(StatusApi.distinctTypes(victim) == 0);
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200));
            check(StatusApi.distinctTypes(victim) == 0);
            for (int i = 0; i < 3; i++) StatusApi.apply(victim, DamageType.CORROSIVE, player.getUUID(), 1, 120);
            StatusApi.apply(victim, DamageType.IMPACT, player.getUUID(), 1, 120);
            check(StatusApi.distinctTypes(victim) == 2);
            var sources = ConditionalModService.sources(player, sword, ComboService.baseSources(player, sword), victim);
            close(3.5, Stats.resolve(1, Stats.DAMAGE, sources));
            check(victim.hurt(player.damageSources().playerAttack(player), 10));
            close(965, victim.getHealth());
            StatusApi.clearAll(victim); check(StatusApi.distinctTypes(victim) == 0);
            close(1.9, Stats.resolve(1, Stats.DAMAGE, ConditionalModService.sources(player, sword, ComboService.baseSources(player, sword), victim)));
            for (int i = 0; i < 3; i++) {
                var killed = target(level);
                try { check(killed.hurt(player.damageSources().playerAttack(player), 10000)); check(!killed.isAlive()); }
                finally { killed.discard(); }
                close(1 + .35 * Math.min(2, i + 1), Stats.resolve(1, Stats.ATTACK_SPEED, ConditionalModService.sources(player, sword, ComboService.baseSources(player, sword), null)));
            }
            ComboService.tick(new PlayerTickEvent.Post(player));
            var modifier = player.getAttribute(Attributes.ATTACK_SPEED).getModifier(ResourceLocation.parse("yagens_attributes:mod_attack_speed"));
            check(modifier != null); close(.7, modifier.amount());
            var empty = new ItemStack(Items.DIAMOND_SWORD); Components.identify(empty);
            close(1, Stats.resolve(1, Stats.ATTACK_SPEED, ConditionalModService.sources(player, empty, List.of(), null)));
            player.getData(RangedAttachments.KILL_BUFFS).counters.replaceAll((key, counter) -> new ConditionalModifier.Counter(counter.stacks(), level.getServer().overworld().getGameTime()));
            ConditionalModService.tick(new PlayerTickEvent.Post(player)); ComboService.tick(new PlayerTickEvent.Post(player));
            check(player.getAttribute(Attributes.ATTACK_SPEED).getModifier(ResourceLocation.parse("yagens_attributes:mod_attack_speed")) == null);
            state.ledger.uninstall(player.getUUID(), frenzy, weapon, false);
            check(player.getData(RangedAttachments.KILL_BUFFS).counters.isEmpty());
        } finally { victim.discard(); }
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Basic Mod runtime check " + checks); }
    private static void inspectionAndConflicts(ServerPlayer player, WorldState state) {
        var weapon = new ItemStack(Items.DIAMOND_SWORD); UUID id = Components.identify(weapon);
        var profile = CombatData.WEAPONS.weapon(weapon);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        player.getInventory().setItem(8, weapon);
        install(player, state, id, "universal_nanocube", 5, 0);
        install(player, state, id, "cold_addition", 5, 1);
        install(player, state, id, "bone_finger_box", 5, 2);
        install(player, state, id, "primed_fury", 10, 3);
        state.bonus(player.getUUID(), Stats.CRITICAL_CHANCE, new Bonus(0, .4, List.of(1.2), 0));
        state.progress(id, new WeaponProgress(10, 3));
        var inspection = WeaponInspectionService.describe(player, weapon, profile);
        close(profile.criticalChance() * 2.3 * 1.2, inspection.resolved().get(Stats.CRITICAL_CHANCE));
        close(1.55, inspection.resolved().get(Stats.ATTACK_SPEED));
        close(profile.baseDamage() * .9 * 1.9, inspection.resolved().get("damage.cold"));
        close(profile.baseDamage() * 1.9 * 1.9, inspection.resolved().entrySet().stream().filter(entry -> Stats.DAMAGE_COMPONENTS.contains(entry.getKey())).mapToDouble(Map.Entry::getValue).sum());
        check(inspection.progress().equals(new WeaponProgress(10, 3)) && inspection.usedCapacity() == state.ledger.usedCapacity(id, profile.slots()));
        check(weapon.get(Components.WEAPON_ID.get()).equals(id) && !Components.absorbsExperience(weapon));
        for (String type : List.of("fury", "berserker_fury")) {
            try { install(player, state, id, type, 0, 4); throw new AssertionError("Expected Prime Fury conflict"); }
            catch (IllegalArgumentException expected) { check(expected.getMessage().equals("gui.yagens_attributes.mod_conflict")); }
        }
        var trident = new ItemStack(Items.TRIDENT); player.setItemInHand(InteractionHand.MAIN_HAND, trident); UUID tridentId = Components.identify(trident);
        state.progress(tridentId, new WeaponProgress(30, 0));
        var catalog = ModDefinitions.INSTANCE.catalog();
        var delay = catalog.card("yagens_attributes:critical_delay", 0); var rush = catalog.card("yagens_attributes:blood_rush", 0);
        UUID delayId = UUID.randomUUID(), rushId = UUID.randomUUID();
        state.ledger.add(new ModLedger.Entry(delayId, player.getUUID(), null, false, delay.bonuses(), delay.info(), -1));
        state.ledger.add(new ModLedger.Entry(rushId, player.getUUID(), null, false, rush.bonuses(), rush.info(), -1));
        state.ledger.install(player.getUUID(), delayId, tridentId, ModInfo.Category.TRIDENT, 8, 0, 30, true);
        PanelService.handle(player, new PanelRequest(990, PanelRequest.WEAPON, 0, PanelRequest.INSTALL, tridentId, rushId, 1, ModInventory.Query.DEFAULT, "zh_cn"));
        check(state.ledger.entry(rushId).weapon() == null && state.ledger.installed(tridentId).size() == 1);
        state.ledger.uninstall(player.getUUID(), delayId, tridentId, false);
        state.ledger.install(player.getUUID(), rushId, tridentId, ModInfo.Category.TRIDENT, 8, 0, 30, true);
        PanelService.handle(player, new PanelRequest(991, PanelRequest.WEAPON, 0, PanelRequest.INSTALL, tridentId, delayId, 1, ModInventory.Query.DEFAULT, "zh_cn"));
        check(state.ledger.entry(delayId).weapon() == null && state.ledger.installed(tridentId).size() == 1);
    }
    private static void close(double a, double b) { check(Math.abs(a - b) < 1e-4); }
}
