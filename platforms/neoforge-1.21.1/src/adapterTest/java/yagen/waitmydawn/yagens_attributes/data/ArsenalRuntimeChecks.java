package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import yagen.waitmydawn.yagens_attributes.api.*;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.*;
import yagen.waitmydawn.yagens_attributes.registries.*;
import yagen.waitmydawn.yagens_attributes.setup.*;
import java.util.*;

final class ArsenalRuntimeChecks {
    private static int checks;
    static int run(ServerLevel level, JsonObject report) {
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("a5b2ac96-aef2-4b6c-bce1-8e0c6ff332d2"), "[ArsenalChecks]"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); player.getInventory().clearContent(); player.getInventory().selected = 0;
        player.setHealth(player.getMaxHealth()); player.setPos(120, 100, 120);
        var state = WorldState.get(player.server);
        for (var entry : state.ledger.owned(player.getUUID())) { state.cycle(entry.id(), null); if (entry.weapon() != null) state.ledger.uninstall(player.getUUID(), entry.id(), entry.weapon(), false); state.ledger.withdraw(player.getUUID(), entry.id()); }
        state.endo(player.getUUID(), 0);
        recycle(player, state); catalyst(level, player, state); ranged(level, player, state); materials(level, report);
        report.addProperty("arsenal_expansion_checks", checks); return checks;
    }
    private static UUID add(ServerPlayer player, WorldState state, String type, int rank) {
        var card = ModDefinitions.INSTANCE.catalog().card("yagens_attributes:" + type, rank); UUID id = UUID.randomUUID();
        state.ledger.add(new ModLedger.Entry(id, player.getUUID(), null, false, card.bonuses(), card.info(), -1)); return id;
    }
    private static void recycle(ServerPlayer player, WorldState state) {
        UUID a = add(player, state, "universal_nanocube", 5), b = add(player, state, "universal_nanocube", 5), c = add(player, state, "universal_nanocube", 5);
        rejects(() -> ModTransactions.recycle(player, state, a, 0, false)); rejects(() -> ModTransactions.recycle(player, state, a, 4, false));
        check(state.endo(player.getUUID()) == 0 && state.ledger.owned(player.getUUID()).size() == 3);
        check(ModTransactions.recycle(player, state, a, 2, false) == 32); check(state.endo(player.getUUID()) == 32);
        var last = state.ledger.owned(player.getUUID()).get(0); UUID weapon = UUID.randomUUID();
        state.ledger.install(player.getUUID(), last.id(), weapon, ModInfo.Category.SWORD, 8, 0, 30, true);
        rejects(() -> ModTransactions.recycle(player, state, last.id(), 1, true)); check(state.endo(player.getUUID()) == 32);
        state.ledger.uninstall(player.getUUID(), last.id(), weapon, false);
        PanelService.handle(player, new PanelRequest(1600, PanelRequest.STORAGE, 0, PanelRequest.RECYCLE_ALL, null, last.id()));
        check(state.endo(player.getUUID()) == 48 && state.ledger.owned(player.getUUID()).isEmpty());
        UUID zero = add(player, state, "universal_nanocube", 0); state.endo(player.getUUID(), Long.MAX_VALUE);
        rejects(() -> ModTransactions.recycle(player, state, zero, 1, true)); check(state.ledger.entry(zero).weapon() == null && state.endo(player.getUUID()) == Long.MAX_VALUE);
        state.endo(player.getUUID(), 0); check(ModTransactions.recycle(player, state, zero, 1, false) == 1);
        UUID galv = add(player, state, "galvanized_chamber", 10); check(ModTransactions.recycle(player, state, galv, 1, false) == 2054);
        var info = ModInfo.DEFAULT; UUID legacy = UUID.randomUUID(); state.ledger.add(new ModLedger.Entry(legacy, player.getUUID(), null, false, Map.of(), info, -1));
        check(ModTransactions.recycle(player, state, legacy, 1, false) == 1);
        check(!new PanelRequest(1601, PanelRequest.WEAPON, 0, PanelRequest.RECYCLE, null, a).valid());
    }
    private static void catalyst(ServerLevel level, ServerPlayer player, WorldState state) {
        var token = new ItemStack(ModItems.OROKIN_CATALYST.get(), 3); player.setItemInHand(InteractionHand.OFF_HAND, token);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE));
        check(CatalystInteractions.apply(player, InteractionHand.MAIN_HAND) == InteractionResult.FAIL && token.getCount() == 3);
        var bow = new ItemStack(Items.BOW); UUID weapon = Components.identify(bow); player.setItemInHand(InteractionHand.MAIN_HAND, bow);
        state.progress(weapon, new WeaponProgress(12, 2)); check(Components.capacity(bow, state.progress(weapon)) == 12);
        UUID mod = add(player, state, "galvanized_chamber", 10);
        PanelService.handle(player, new PanelRequest(1602, PanelRequest.WEAPON, 0, PanelRequest.INSTALL, weapon, mod)); check(state.ledger.entry(mod).weapon() == null);
        var event = new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND); NeoForge.EVENT_BUS.post(event);
        check(event.isCanceled() && bow.getOrDefault(Components.CATALYZED.get(), false)); check(token.getCount() == 2 && Components.capacity(bow, state.progress(weapon)) == 24);
        check(!player.isUsingItem());
        PanelService.handle(player, new PanelRequest(1603, PanelRequest.WEAPON, 0, PanelRequest.INSTALL, weapon, mod)); check(weapon.equals(state.ledger.entry(mod).weapon()));
        check(CatalystInteractions.apply(player, InteractionHand.MAIN_HAND) == InteractionResult.FAIL && token.getCount() == 2);
        var restored = ItemStack.parseOptional(level.registryAccess(), (CompoundTag) bow.save(level.registryAccess())); check(Components.capacity(restored, state.progress(weapon)) == 24);
        state.ledger.uninstall(player.getUUID(), mod, weapon, false);
        state.progress(weapon, new WeaponProgress(30, 0)); player.getInventory().setItem(4, new ItemStack(ModItems.FORMA.get()));
        ModTransactions.polarize(player, state, weapon, 0, ModInfo.Polarity.MADURAI);
        check(state.progress(weapon).level() == 0 && bow.getOrDefault(Components.CATALYZED.get(), false));
        state.progress(weapon, WeaponProgress.ZERO.gain(1080).progress()); check(Components.capacity(bow, state.progress(weapon)) == 60);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
    }
    private static Cow target(ServerLevel level, double health) {
        var cow = new Cow(EntityType.COW, level); cow.setNoAi(true); cow.setPos(122, 100, 120); cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10000); cow.setHealth((float) health); level.addFreshEntity(cow); return cow;
    }
    private static void ranged(ServerLevel level, ServerPlayer player, WorldState state) {
        var bow = new ItemStack(Items.BOW); UUID id = Components.identify(bow); player.setItemInHand(InteractionHand.MAIN_HAND, bow);
        UUID mod = add(player, state, "galvanized_chamber", 10);
        state.ledger.install(player.getUUID(), mod, id, ModInfo.Category.BOW, 8, 0, 30, true);
        var frozen = RangedShooting.snapshot(player, bow); close(1.8, frozen.multishot()); check(id.equals(frozen.weapon()));
        for (int i = 0; i < 6; i++) {
            var cow = target(level, 100); var arrow = new Arrow(EntityType.ARROW, level); arrow.setOwner(player); arrow.setData(RangedAttachments.SHOT.get(), RangedShooting.snapshot(player, bow));
            try { check(cow.hurt(player.damageSources().arrow(arrow, player), 10000)); check(!cow.isAlive()); }
            finally { cow.discard(); arrow.discard(); }
            close(1.8 + .3 * Math.min(5, i + 1), RangedShooting.snapshot(player, bow).multishot());
        }
        close(1.8, frozen.multishot());
        var encoded = ShotData.decode(frozen.encode()); check(id.equals(encoded.weapon())); close(1.8, encoded.multishot());
        var dotBow = new ItemStack(Items.BOW); UUID dotWeapon = Components.identify(dotBow);
        UUID dotMod = add(player, state, "galvanized_chamber", 10);
        state.ledger.install(player.getUUID(), dotMod, dotWeapon, ModInfo.Category.BOW, 8, 0, 30, true);
        var dotProfile = CombatData.WEAPONS.weapon(dotBow);
        state.bonus(player.getUUID(), Stats.CRITICAL_CHANCE, new Bonus(-dotProfile.criticalChance(), 0, List.of(), 0));
        state.bonus(player.getUUID(), Stats.STATUS_CHANCE, new Bonus(-dotProfile.statusChance(), 0, List.of(), 1));
        for (var type : List.of(DamageType.IMPACT, DamageType.PUNCTURE)) state.bonus(player.getUUID(), "damage." + type.key(), new Bonus(0, 0, List.of(0D), 0));
        state.bonus(player.getUUID(), "damage.slash", new Bonus(0, 0, List.of(), 0, 1));
        if (level.getEntity(player.getUUID()) == null) level.addNewPlayer(player);
        var dotTarget = target(level, 13); var dotArrow = new Arrow(EntityType.ARROW, level); dotArrow.setOwner(player);
        dotArrow.setData(RangedAttachments.SHOT.get(), RangedShooting.snapshot(player, dotBow));
        try {
            check(dotTarget.hurt(player.damageSources().arrow(dotArrow, player), 10));
            check(StatusEffects.state(dotTarget).layers.stream().anyMatch(layer -> dotWeapon.equals(layer.weapon())));
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
            for (int tick = 0; tick < 20; tick++) StatusEffects.tick(new EntityTickEvent.Post(dotTarget));
            check(!dotTarget.isAlive()); close(2.1, RangedShooting.snapshot(player, dotBow).multishot());
        } finally { dotTarget.discard(); dotArrow.discard(); }
        for (String key : List.of(Stats.CRITICAL_CHANCE, Stats.STATUS_CHANCE, "damage.impact", "damage.puncture", "damage.slash")) state.bonus(player.getUUID(), key, Bonus.NONE);
        var fusionBow = new ItemStack(Items.BOW); UUID fusionId = Components.identify(fusionBow);
        for (var request : List.of(Map.entry("heat_addition", 0), Map.entry("cold_addition", 1))) {
            UUID entry = add(player, state, request.getKey(), 5); state.ledger.install(player.getUUID(), entry, fusionId, ModInfo.Category.BOW, 8, request.getValue(), 30, true);
        }
        var shot = RangedShooting.snapshot(player, fusionBow); var amounts = Combat.components(10, shot.profile(), shot.sources()); close(18, amounts.get(DamageType.BLAST)); close(0, amounts.get(DamageType.HEAT));
        var loaded = ShotData.decode(shot.encode()); check(new ArrayList<>(shot.sources().get(0).keySet()).equals(new ArrayList<>(loaded.sources().get(0).keySet())));
        state.ledger.uninstall(player.getUUID(), mod, id, false);
    }
    private static void materials(ServerLevel level, JsonObject report) {
        var assigned = new HashSet<String>(); var categories = new JsonObject();
        for (var entry : BuiltinMaterials.life().entrySet()) {
            var names = new JsonArray();
            for (var id : entry.getValue().entities()) {
                var type = BuiltInRegistries.ENTITY_TYPE.getOptional(net.minecraft.resources.ResourceLocation.parse(id)).orElseThrow();
                check(assigned.add(id)); names.add(id);
                if (type == EntityType.PLAYER) continue;
                var entity = type.create(level);
                check(entity instanceof LivingEntity);
                check(CombatData.MATERIALS.material((LivingEntity) entity).equals(entry.getValue().multipliers()));
            }
            categories.add(entry.getKey(), names);
        }
        var unknown = new JsonArray();
        for (var type : BuiltInRegistries.ENTITY_TYPE) if (DefaultAttributes.hasSupplier(type) && !assigned.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString())) unknown.add(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        report.add("health_materials", categories); report.add("unassigned_living_entities", unknown);
        check(ShieldMaterials.INSTANCE.multipliers().equals(BuiltinMaterials.shield()));
        var cow = target(level, 1000);
        try {
            cow.getAttribute(ShieldAttributes.MAX_SHIELD).setBaseValue(100); cow.setAbsorptionAmount(100);
            StatusDamage.hurt(cow, DamageType.GAS, UUID.randomUUID(), 10);
            close(90, cow.getAbsorptionAmount()); close(1000, cow.getHealth());
            cow.setAbsorptionAmount(0); StatusDamage.hurt(cow, DamageType.GAS, UUID.randomUUID(), 10); close(987.5, cow.getHealth());
            var unassigned = new net.minecraft.world.entity.monster.EnderMan(EntityType.ENDERMAN, level);
            check(CombatData.MATERIALS.material(unassigned).isEmpty());
        } finally { cow.discard(); }
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Arsenal runtime check " + checks); }
    private static void close(double a, double b) { check(Math.abs(a - b) < 1e-3); }
    private static void rejects(Runnable action) { try { action.run(); } catch (IllegalArgumentException expected) { checks++; return; } throw new AssertionError("Expected rejection"); }
}
