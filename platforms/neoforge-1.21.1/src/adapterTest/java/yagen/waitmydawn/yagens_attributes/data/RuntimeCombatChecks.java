package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.config.ServerConfigs;
import yagen.waitmydawn.yagens_attributes.api.ArmorPenetrationEvent;
import yagen.waitmydawn.yagens_attributes.api.StatusApi;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.network.PanelRequest;
import yagen.waitmydawn.yagens_attributes.registries.*;
import yagen.waitmydawn.yagens_attributes.setup.*;
import yagen.waitmydawn.yagens_attributes.setup.StatusEffects;
import java.nio.file.*;
import java.util.*;

/** Opt-in integration checks in a disposable server. This source set is excluded from release artifacts. */
@EventBusSubscriber(modid = YagensAttributes.MODID, value = Dist.DEDICATED_SERVER)
public final class RuntimeCombatChecks {
    private static int checks;
    private static final List<Cow> TARGETS = new ArrayList<>();
    private static final Map<LivingEntity, Float> INCOMING_AMOUNTS = new IdentityHashMap<>();
    private static LivingEntity penetrationTarget;
    private static LivingEntity potionBlockedTarget;
    private static int potionChecks;
    private RuntimeCombatChecks() {}
    @SubscribeEvent public static void started(ServerStartedEvent event) {
        if (!Boolean.getBoolean("yagens_attributes.validateCombat")) return;
        var report = new JsonObject();
        report.addProperty("pid", ProcessHandle.current().pid());
        try {
            var suite = System.getProperty("yagens_attributes.validationSuite", "all");
            if (suite.equals("argon-scope")) {
                checks += ArgonScopeRuntimeChecks.run(event.getServer().overworld());
            } else if (suite.equals("weak-points")) {
                checks += WeakPointRuntimeChecks.run(event.getServer().overworld());
                checks += VanillaWeakPointRuntimeChecks.run(event.getServer().overworld());
            } else if (suite.equals("weapon-lifecycle")) {
                checks += WeaponLifecycleRuntimeChecks.run(event.getServer().overworld());
            } else {
                if (!suite.equals("galvanized")) {
                    run(event.getServer().overworld(), report);
                    checks += WeaponLifecycleRuntimeChecks.run(event.getServer().overworld());
                    checks += WeakPointRuntimeChecks.run(event.getServer().overworld());
                    checks += VanillaWeakPointRuntimeChecks.run(event.getServer().overworld());
                    checks += ArgonScopeRuntimeChecks.run(event.getServer().overworld());
                }
                checks += GalvanizedRuntimeChecks.run(event.getServer().overworld(), report);
            }
            report.addProperty("passed", true);
            YagensAttributes.LOGGER.info("Passed {} runtime combat checks", checks);
        } catch (Throwable failure) {
            report.addProperty("passed", false); report.addProperty("failure", failure.toString());
            YagensAttributes.LOGGER.error("Runtime combat checks failed", failure);
        } finally {
            report.addProperty("checks", checks);
            for (var target : TARGETS) target.discard(); TARGETS.clear(); INCOMING_AMOUNTS.clear(); potionBlockedTarget = null;
            try { Files.writeString(Path.of("combat-runtime-report.json"), ModJson.GSON.toJson(report)); }
            catch (java.io.IOException failure) { YagensAttributes.LOGGER.error("Cannot write runtime combat report", failure); }
            event.getServer().halt(false);
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void observe(LivingIncomingDamageEvent event) {
        if (Boolean.getBoolean("yagens_attributes.validateCombat") && TARGETS.contains(event.getEntity()))
            INCOMING_AMOUNTS.put(event.getEntity(), event.getAmount());
    }
    @SubscribeEvent public static void penetration(ArmorPenetrationEvent event) {
        if (event.target() == penetrationTarget) event.add(new ArmorPenetration(5, .25, 2, .25));
    }
    @SubscribeEvent public static void blockPotion(MobEffectEvent.Applicable event) {
        if (event.getEntity() == potionBlockedTarget) {
            potionChecks++;
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }
    private static void run(ServerLevel level, JsonObject report) {
        criticalHitProcs(level, report);
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("cb511ef5-5161-4d06-91bd-361d18dde733"), "[CombatChecks]"));
        player.setPos(0, 100, 0); player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        var state = WorldState.get(level.getServer()); var profile = CombatData.WEAPONS.weapon(player.getMainHandItem());
        state.bonus(player.getUUID(), Stats.CRITICAL_CHANCE, new Bonus(-profile.criticalChance(), 0, List.of(), 0));
        state.bonus(player.getUUID(), Stats.STATUS_CHANCE, new Bonus(-profile.statusChance(), 0, List.of(), 0));
        state.bonus(player.getUUID(), Stats.DAMAGE, new Bonus(0, 1, List.of(), 0));
        var source = level.damageSources().playerAttack(player);
        var direct = target(level); double before = direct.getHealth();
        check(direct.hurt(source, 10)); close(20, INCOMING_AMOUNTS.get(direct)); close(60D / 7, before - direct.getHealth());

        state.bonus(player.getUUID(), Stats.CRITICAL_CHANCE, new Bonus(-profile.criticalChance(), 0, List.of(), 1));
        var critical = target(level); before = critical.getHealth();
        check(critical.hurt(source, 10)); close(40, INCOMING_AMOUNTS.get(critical)); close(120D / 7, before - critical.getHealth());

        state.bonus(player.getUUID(), Stats.STATUS_CHANCE, new Bonus(-profile.statusChance(), 0, List.of(), 1));
        for (var type : List.of(DamageType.IMPACT, DamageType.PUNCTURE, DamageType.SLASH))
            state.bonus(player.getUUID(), "damage." + type.key(), new Bonus(0, 0, List.of(0D), 0));
        state.bonus(player.getUUID(), "damage.heat", new Bonus(0, 0, List.of(), 0, 0, .9));
        var dot = target(level); check(dot.hurt(source, 10));
        var layers = yagen.waitmydawn.yagens_attributes.setup.StatusEffects.state(dot).layers;
        check(layers.size() == 1 && layers.get(0).type() == DamageType.HEAT); close(76, layers.get(0).base()); check(layers.get(0).criticalTier() == 1);
        before = dot.getHealth(); StatusDamage.hurt(dot, DamageType.HEAT, player.getUUID(), layers.get(0).base() * .5);
        close(114D / 7, before - dot.getHealth());
        var batch = target(level); before = batch.getHealth(); StatusDamage.hurtBatch(batch, DamageType.TOXIN, player.getUUID(), List.of(10D, 20D));
        close(90D / 7, before - batch.getHealth());

        var blocked = target(level); check(blocked.hurt(level.damageSources().generic(), 100));
        check(!blocked.hurt(source, 10)); check(!blocked.hasData(RangedAttachments.STATUS) || yagen.waitmydawn.yagens_attributes.setup.StatusEffects.state(blocked).layers.isEmpty());
        blocked.invulnerableTime = 0;
        state.bonus(player.getUUID(), Stats.CRITICAL_CHANCE, new Bonus(-profile.criticalChance(), 0, List.of(), 0));
        check(blocked.hurt(source, 10));
        close(38, yagen.waitmydawn.yagens_attributes.setup.StatusEffects.state(blocked).layers.get(0).base());

        state.bonus(player.getUUID(), Stats.STATUS_CHANCE, new Bonus(-profile.statusChance(), 0, List.of(), 0));
        state.bonus(player.getUUID(), "damage.heat", Bonus.NONE);
        for (var type : List.of(DamageType.IMPACT, DamageType.PUNCTURE, DamageType.SLASH)) state.bonus(player.getUUID(), "damage." + type.key(), Bonus.NONE);
        var shield = target(level); shield.getAttribute(ShieldAttributes.MAX_SHIELD).setBaseValue(20); shield.setAbsorptionAmount(5);
        before = shield.getHealth(); check(shield.hurt(source, 10)); close(0, shield.getAbsorptionAmount());
        double shieldFactor = profile.distribution().entrySet().stream().mapToDouble(entry -> entry.getValue() * ShieldMaterials.INSTANCE.multipliers().getOrDefault(entry.getKey(), 1D)).sum();
        close(60D / 7 * (1 - 5 / (20 * shieldFactor)), before - shield.getHealth());
        var toxin = target(level); toxin.getAttribute(ShieldAttributes.MAX_SHIELD).setBaseValue(20); toxin.setAbsorptionAmount(20);
        for (var type : List.of(DamageType.IMPACT, DamageType.PUNCTURE, DamageType.SLASH)) state.bonus(player.getUUID(), "damage." + type.key(), new Bonus(0, 0, List.of(0D), 0));
        state.bonus(player.getUUID(), "damage.toxin", new Bonus(0, 0, List.of(), 0, 0, .5));
        before = toxin.getHealth(); check(toxin.hurt(source, 10)); close(20, toxin.getAbsorptionAmount()); close(30D / 7, before - toxin.getHealth());
        for (var type : List.of(DamageType.IMPACT, DamageType.PUNCTURE, DamageType.SLASH, DamageType.TOXIN)) state.bonus(player.getUUID(), "damage." + type.key(), Bonus.NONE);

        var bow = new ItemStack(Items.BOW); player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, bow);
        var bowProfile = CombatData.WEAPONS.weapon(bow);
        state.bonus(player.getUUID(), Stats.CRITICAL_CHANCE, new Bonus(-bowProfile.criticalChance(), 0, List.of(), 0));
        state.bonus(player.getUUID(), Stats.STATUS_CHANCE, new Bonus(-bowProfile.statusChance(), 0, List.of(), 0));
        var shot = Objects.requireNonNull(RangedShooting.snapshot(player, bow));
        state.bonus(player.getUUID(), Stats.DAMAGE, new Bonus(0, 9, List.of(), 0));
        var single = target(level); before = single.getHealth(); hitArrow(level, player, bow, shot, single, 1.5); double one = before - single.getHealth(); close(90D / 7, one);
        var multiple = target(level); before = multiple.getHealth();
        hitArrow(level, player, bow, shot, multiple, .75); hitArrow(level, player, bow, shot, multiple, .75);
        close(one, before - multiple.getHealth());
        close(15, INCOMING_AMOUNTS.get(multiple));

        var bypass = target(level); before = bypass.getHealth(); check(bypass.hurt(level.damageSources().fellOutOfWorld(), 10)); close(10, before - bypass.getHealth());
        try {
            ServerConfigs.ARMOR_MODE.set("none");
            close(4, CombatRules.getDamageAfterAbsorb(bypass, 10, source, 20, 0));
            ServerConfigs.ARMOR_MODE.set("improved_vanilla");
            close(5, CombatRules.getDamageAfterAbsorb(bypass, 10, source, 20, 0));
            var nonlinear = target(level); double health = nonlinear.getHealth();
            StatusDamage.hurtBatch(nonlinear, DamageType.TOXIN, player.getUUID(), List.of(10D, 20D));
            close(55D / 3, health - nonlinear.getHealth());
        } finally { ServerConfigs.ARMOR_MODE.set("linear"); }
        penetrationTarget = target(level); penetrationTarget.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(8);
        try {
            close(4, CombatRules.getDamageAfterAbsorb(penetrationTarget, 10, source, 20, 8));
            ServerConfigs.ARMOR_MODE.set("improved_vanilla");
            close(40D / 9, CombatRules.getDamageAfterAbsorb(penetrationTarget, 10, source, 20, 8));
            ServerConfigs.ARMOR_MODE.set("none");
            close(3, CombatRules.getDamageAfterAbsorb(penetrationTarget, 10, source, 20, 8));
            close(20, penetrationTarget.getArmorValue()); close(8, penetrationTarget.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        } finally { ServerConfigs.ARMOR_MODE.set("linear"); penetrationTarget = null; }

        var huge = target(level); huge.getAttribute(Attributes.ARMOR).setBaseValue(2_147_483_647D); huge.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(2_147_483_647D);
        double fraction = ArmorRules.remaining((float) huge.getArmorValue(), (float) huge.getAttributeValue(Attributes.ARMOR_TOUGHNESS), 15, 8);
        float after = CombatRules.getDamageAfterAbsorb(huge, Float.MAX_VALUE, source, (float) huge.getArmorValue(), (float) huge.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        check(after > 0 && Float.isFinite(after)); check(Math.abs(after / (Float.MAX_VALUE * fraction) - 1) < 1e-6);
        var mace = new ItemStack(Items.MACE); player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, mace);
        float plain = CombatRules.getDamageAfterAbsorb(bypass, 10, source, 20, 0);
        mace.enchant(level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.BREACH), 4);
        check(CombatRules.getDamageAfterAbsorb(bypass, 10, source, 20, 0) > plain);
        var weapon = Components.identify(player.getMainHandItem()); UUID firstMod = UUID.randomUUID(), secondMod = UUID.randomUUID(), otherOwner = UUID.randomUUID();
        state.ledger.add(new ModLedger.Entry(firstMod, player.getUUID(), weapon, false, Map.of(), ModInfo.DEFAULT, 0));
        state.ledger.add(new ModLedger.Entry(secondMod, otherOwner, weapon, false, Map.of(), ModInfo.DEFAULT, 1));
        moveRequest(player, weapon, firstMod, 2); check(state.ledger.entry(firstMod).slot() == 2);
        moveRequest(player, weapon, firstMod, 1); check(state.ledger.entry(firstMod).slot() == 2); check(state.ledger.entry(secondMod).slot() == 1);
        state.ledger.share(otherOwner, secondMod, true);
        moveRequest(player, weapon, firstMod, 1); check(state.ledger.entry(firstMod).slot() == 1); check(state.ledger.entry(secondMod).slot() == 2); check(state.ledger.entry(secondMod).owner().equals(otherOwner));
        PanelService.handle(player, new PanelRequest(103, PanelRequest.WEAPON, 0, PanelRequest.TAKE, weapon, secondMod));
        check(state.ledger.entry(secondMod).owner().equals(player.getUUID())); check(state.ledger.entry(secondMod).weapon() == null);
        state.ledger.uninstall(player.getUUID(), firstMod, weapon, false); state.ledger.withdraw(player.getUUID(), firstMod); state.ledger.withdraw(player.getUUID(), secondMod);
        var marked = target(level);
        StatusEffects.apply(marked, DamageType.COLD, player.getUUID(), 0, 120);
        StatusEffects.apply(marked, DamageType.COLD, player.getUUID(), 0, 120);
        StatusEffects.apply(marked, DamageType.PUNCTURE, player.getUUID(), 0, 120);
        var markers = StatusMarkerSync.snapshot(marked);
        check(markers.entityUuid().equals(marked.getUUID())); check(markers.counts().get(DamageType.COLD) == 2);
        check(markers.counts().get(DamageType.PUNCTURE) == 1);
        StatusMarkerSync.tracking(player, marked);
        check(marked.getActiveEffects().isEmpty());
        check(marked.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120)));
        new ItemStack(Items.MILK_BUCKET).finishUsingItem(level, marked);
        check(marked.getActiveEffects().isEmpty());
        check(StatusApi.count(marked, DamageType.COLD) == 2);
        check(StatusApi.count(marked, DamageType.PUNCTURE) == 1);
        check(marked.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120)));
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(), "effect clear " + marked.getUUID());
        check(marked.getActiveEffects().isEmpty());
        check(StatusApi.count(marked, DamageType.COLD) == 2);
        check(StatusApi.count(marked, DamageType.PUNCTURE) == 1);
        check(StatusApi.clear(marked, DamageType.COLD));
        check(!StatusMarkerSync.snapshot(marked).counts().containsKey(DamageType.COLD));
        check(StatusMarkerSync.snapshot(marked).counts().get(DamageType.PUNCTURE) == 1);
        marked.setHealth(0); check(StatusMarkerSync.snapshot(marked).counts().isEmpty());
        statusApi(level, player, report);
        var hotbar = new ArrayList<>(Collections.nCopies(36, ItemStack.EMPTY));
        hotbar.set(0, new ItemStack(Items.STICK)); hotbar.set(1, new ItemStack(Items.DIAMOND_SWORD));
        hotbar.set(2, new ItemStack(Items.MACE)); hotbar.set(4, new ItemStack(Items.BOW)); hotbar.set(6, new ItemStack(Items.DIAMOND_SWORD, 2));
        hotbar.set(8, new ItemStack(Items.TRIDENT)); hotbar.set(9, new ItemStack(Items.DIAMOND_SWORD));
        var known = Set.of(Items.STICK, Items.DIAMOND_SWORD, Items.BOW, Items.TRIDENT);
        check(WeaponEligibility.hotbar(hotbar, stack -> known.contains(stack.getItem()) ? profile : null).equals(List.of(1, 4, 8)));
        hotbar.set(4, ItemStack.EMPTY);
        check(WeaponEligibility.hotbar(hotbar, stack -> known.contains(stack.getItem()) ? profile : null).equals(List.of(1, 8)));
        check(WeaponEligibility.hotbar(hotbar, stack -> null).isEmpty());
        int previousSlot = player.getInventory().selected;
        var previousStack = player.getInventory().getItem(8);
        player.getInventory().setItem(8, new ItemStack(Items.BOW));
        // FakePlayer ignores selection packets; exercise a socket-free native handler while retaining its fake sender.
        var fakeConnection = player.connection;
        net.minecraft.server.network.ServerGamePacketListenerImpl nativeListener;
        try {
            nativeListener = new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,
                    new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND), player,
                    net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(), false));
        } finally { player.connection = fakeConnection; }
        nativeListener.handleSetCarriedItem(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(8));
        check(player.getInventory().selected == 8); check(player.getMainHandItem().is(Items.BOW));
        PanelService.handle(player, new PanelRequest(200, PanelRequest.WEAPON, 0, PanelRequest.REFRESH, null, null));
        check(CombatData.WEAPONS.weapon(player.getMainHandItem()).category() == ModInfo.Category.BOW);
        nativeListener.handleSetCarriedItem(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(previousSlot));
        check(player.getInventory().selected == previousSlot); player.getInventory().setItem(8, previousStack);
        armorSets(report);
        polarization(level, report);
        combo(level, report);
        checks += ExperienceRuntimeChecks.run(level, report);
        checks += RivenRuntimeChecks.run(level, report);
        checks += CycleRuntimeChecks.run(level, report);
        checks += BasicModsRuntimeChecks.run(level, report);
        checks += ArsenalRuntimeChecks.run(level, report);
        checks += RemoteReleaseRuntimeChecks.run(level, report);
        checks += ModFilterRuntimeChecks.run(level, report);
        checks += LootRuntimeChecks.run(level, report);
    }
    private static void combo(ServerLevel level, JsonObject report) {
        int before = checks;
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("e1e9f9fc-3b1e-4db0-a85e-6b1f41f74841"), "[ComboChecks]"));
        player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.getInventory().clearContent(); player.getInventory().selected = 0;
        var sword = new ItemStack(Items.DIAMOND_SWORD); player.getInventory().setItem(0, sword);
        UUID weapon = Components.identify(sword); var state = WorldState.get(level.getServer());
        var catalog = ModDefinitions.INSTANCE.catalog();
        var profile = CombatData.WEAPONS.weapon(sword);
        for (var request : List.of(Map.entry("fury", 5), Map.entry("reach", 3), Map.entry("body_count", 5), Map.entry("blood_rush", 10), Map.entry("weeping_wounds", 5))) {
            var card = catalog.card("yagens_attributes:" + request.getKey(), request.getValue()); UUID mod = UUID.randomUUID();
            state.ledger.add(new ModLedger.Entry(mod, player.getUUID(), null, false, card.bonuses(), card.info(), -1));
            state.ledger.install(player.getUUID(), mod, weapon, profile.category(), 8, -1, 0, true, profile.slots());
        }
        check(ComboService.duration(ComboService.baseSources(player, sword)) == 340);
        double speedBefore = player.getAttributeValue(Attributes.ATTACK_SPEED), rangeBefore = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        ComboService.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        close(speedBefore * 1.3, player.getAttributeValue(Attributes.ATTACK_SPEED));
        close(rangeBefore + 1.5, player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE));
        MeleeAttributes.update(player, List.of(Map.of(Stats.ATTACK_SPEED, new Bonus(0, 0, List.of(Double.MAX_VALUE, Double.MAX_VALUE), 0))));
        close(speedBefore, player.getAttributeValue(Attributes.ATTACK_SPEED)); close(rangeBefore, player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE));
        MeleeAttributes.update(player, ComboService.baseSources(player, sword));
        close(speedBefore * 1.3, player.getAttributeValue(Attributes.ATTACK_SPEED));
        check(ComboService.count(player, weapon) == 0);
        var victim = target(level); var attack = level.damageSources().playerAttack(player);
        check(victim.hurt(attack, 10)); check(ComboService.count(player, weapon) == 1);
        victim.invulnerableTime = 0;
        StatusDamage.hurt(victim, DamageType.HEAT, player.getUUID(), 1);
        check(ComboService.count(player, weapon) == 1);
        for (int i = 1; i < 20; i++) ComboService.hit(player, weapon, 340);
        check(ComboService.count(player, weapon) == 20);
        var sources = ComboService.sources(player, weapon, ComboService.baseSources(player, sword));
        close(profile.criticalChance() * 1.4, Stats.resolve(profile.criticalChance(), Stats.CRITICAL_CHANCE, sources));
        close(profile.statusChance() * 1.4, Stats.resolve(profile.statusChance(), Stats.STATUS_CHANCE, sources));
        var block = new net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent(player,
                new net.neoforged.neoforge.common.damagesource.DamageContainer(level.damageSources().mobAttack(victim), 4), true);
        block.setBlockedDamage(4); ComboService.blocked(block); check(ComboService.count(player, weapon) == 21);
        block.setBlocked(false); ComboService.blocked(block); check(ComboService.count(player, weapon) == 21);
        for (int i = 0; i < 250; i++) ComboService.hit(player, weapon, 340);
        check(ComboService.count(player, weapon) == 220);
        sources = ComboService.sources(player, weapon, ComboService.baseSources(player, sword));
        close(profile.criticalChance() * 5.4, Stats.resolve(profile.criticalChance(), Stats.CRITICAL_CHANCE, sources));
        close(profile.statusChance() * 5.4, Stats.resolve(profile.statusChance(), Stats.STATUS_CHANCE, sources));
        player.getInventory().setItem(0, new ItemStack(Items.BOW));
        ComboService.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        close(speedBefore, player.getAttributeValue(Attributes.ATTACK_SPEED)); close(rangeBefore, player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE));
        check(ComboService.count(player, UUID.randomUUID()) == 0); check(ComboService.count(player, weapon) == 220);
        player.getInventory().setItem(0, sword);
        var combo = player.getData(RangedAttachments.COMBO);
        combo.counters.put(weapon, new Combo.Counter(220, level.getServer().overworld().getGameTime(), 100));
        check(ComboService.count(player, weapon) == 0);
        sources = ComboService.sources(player, weapon, ComboService.baseSources(player, sword));
        close(profile.criticalChance(), Stats.resolve(profile.criticalChance(), Stats.CRITICAL_CHANCE, sources));
        ComboService.hit(player, weapon, 340); check(ComboService.count(player, weapon) == 1);
        var other = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("d412a1b0-d720-4180-93a1-52087c246248"), "[OtherComboChecks]"));
        check(ComboService.count(other, weapon) == 0);
        ComboService.died(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(player, attack));
        check(ComboService.count(player, weapon) == 0);
        check(net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.get(ModCreativeTabs.ITEMS.getId()) == ModCreativeTabs.ITEMS.get());
        report.addProperty("combo_checks", checks - before);
    }
    private static void polarization(ServerLevel level, JsonObject report) {
        int before = checks;
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("b621259d-907a-4e3c-b245-cb942882bf20"), "[PolarityChecks]"));
        player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.getInventory().clearContent(); player.getInventory().selected = 0;
        var held = new ItemStack(Items.DIAMOND_SWORD); player.getInventory().setItem(0, held);
        player.getInventory().setItem(1, new ItemStack(ModItems.FORMA.get(), 3));
        var state = WorldState.get(level.getServer()); UUID weapon = Components.identify(held);
        state.progress(weapon, new WeaponProgress(29, 0));
        rejected(() -> ModTransactions.polarize(player, state, weapon, 0, ModInfo.Polarity.MADURAI));
        check(player.getInventory().getItem(1).getCount() == 3); check(!held.has(Components.WEAPON_POLARITIES.get()));
        state.progress(weapon, new WeaponProgress(30, 0));
        var card = ModDefinitions.INSTANCE.catalog().card("yagens_attributes:nameless_glove", 0);
        UUID foreignOwner = UUID.randomUUID(), mod = UUID.randomUUID();
        state.ledger.add(new ModLedger.Entry(mod, foreignOwner, weapon, false, card.bonuses(), card.info(), 0));
        rejected(() -> ModTransactions.polarize(player, state, weapon, 0, ModInfo.Polarity.MADURAI));
        check(state.progress(weapon).level() == 30); check(state.ledger.entry(mod).weapon().equals(weapon)); check(player.getInventory().getItem(1).getCount() == 3);
        state.ledger.share(foreignOwner, mod, true);
        rejected(() -> ModTransactions.polarize(player, state, UUID.randomUUID(), 0, ModInfo.Polarity.MADURAI));
        rejected(() -> ModTransactions.polarize(player, state, weapon, 64, ModInfo.Polarity.MADURAI));
        rejected(() -> ModTransactions.polarize(player, state, weapon, 0, ModInfo.Polarity.UMBRA));
        ModTransactions.polarize(player, state, weapon, 0, ModInfo.Polarity.MADURAI);
        check(player.getInventory().getItem(1).getCount() == 2); check(state.progress(weapon).equals(WeaponProgress.ZERO));
        check(state.ledger.entry(mod).owner().equals(foreignOwner)); check(state.ledger.entry(mod).weapon() == null);
        check(state.ledger.installed(weapon).isEmpty());
        check(held.get(Components.WEAPON_POLARITIES.get()).overrides().equals(List.of(new WeaponPolarityData.Slot(0, ModInfo.Polarity.MADURAI))));
        var restored = ItemStack.parseOptional(level.registryAccess(), (net.minecraft.nbt.CompoundTag) held.save(level.registryAccess()));
        check(Objects.equals(held.get(Components.WEAPON_POLARITIES.get()), restored.get(Components.WEAPON_POLARITIES.get())));
        check(Objects.equals(weapon, restored.get(Components.WEAPON_ID.get())));
        player.getInventory().setItem(0, restored); state.progress(weapon, new WeaponProgress(30, 0));
        rejected(() -> ModTransactions.polarize(player, state, weapon, 0, ModInfo.Polarity.MADURAI));
        check(player.getInventory().getItem(1).getCount() == 2); check(state.progress(weapon).level() == 30);
        ModTransactions.polarize(player, state, weapon, 0, ModInfo.Polarity.VAZARIN);
        check(player.getInventory().getItem(1).getCount() == 1); check(state.progress(weapon).equals(WeaponProgress.ZERO));
        check(restored.get(Components.WEAPON_POLARITIES.get()).overrides().equals(List.of(new WeaponPolarityData.Slot(0, ModInfo.Polarity.VAZARIN))));
        state.progress(weapon, state.progress(weapon).gain(7).progress()); check(state.progress(weapon).equals(new WeaponProgress(1, 0)));
        state.progress(weapon, new WeaponProgress(30, 0)); player.getInventory().setItem(1, ItemStack.EMPTY);
        rejected(() -> ModTransactions.polarize(player, state, weapon, 1, ModInfo.Polarity.NARAMON));
        check(state.progress(weapon).level() == 30); check(restored.get(Components.WEAPON_POLARITIES.get()).overrides().size() == 1);
        check(new ItemStack(ModItems.FORMA.get()).getRarity() == Rarity.RARE); check(new ItemStack(ModItems.KUVA.get()).getRarity() == Rarity.EPIC);
        int creativeBefore = checks;
        player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.CREATIVE);
        var progress = new WeaponProgress(3, 2); state.progress(weapon, progress); state.endo(player.getUUID(), 0);
        var profile = WeaponPolarityData.profile(restored, CombatData.WEAPONS.weapon(restored), state.ledger);
        state.ledger.install(foreignOwner, mod, weapon, profile.category(), profile.slots().size(), 0, 0, true, profile.slots());
        state.ledger.share(foreignOwner, mod, false);
        rejected(() -> ModTransactions.polarize(player, state, weapon, 1, ModInfo.Polarity.NARAMON));
        check(state.progress(weapon).equals(progress)); check(state.ledger.entry(mod).weapon().equals(weapon));
        state.ledger.share(foreignOwner, mod, true);
        ModTransactions.polarize(player, state, weapon, 1, ModInfo.Polarity.NARAMON);
        check(player.getInventory().getItem(1).isEmpty()); check(state.progress(weapon).equals(progress));
        check(state.ledger.entry(mod).weapon().equals(weapon)); check(state.ledger.entry(mod).slot() == 0);
        check(state.ledger.entry(mod).owner().equals(foreignOwner));
        check(WeaponPolarityData.profile(restored, CombatData.WEAPONS.weapon(restored), state.ledger).slots().get(1) == ModInfo.Polarity.NARAMON);
        ModTransactions.polarize(player, state, weapon, 0, ModInfo.Polarity.MADURAI);
        check(state.progress(weapon).equals(progress)); check(state.ledger.installed(weapon).size() == 1);
        rejected(() -> ModTransactions.polarize(player, state, weapon, 0, ModInfo.Polarity.UMBRA));
        ModTransactions.upgrade(player, state, mod, weapon);
        check(state.ledger.entry(mod).info().level() == 1); check(state.endo(player.getUUID()) == 0);
        state.endo(player.getUUID(), 200);
        ModTransactions.upgrade(player, state, mod, weapon);
        check(state.ledger.entry(mod).info().level() == 2); check(state.endo(player.getUUID()) == 200);
        for (int i = 2; i < card.info().details().maxLevel(); i++) ModTransactions.upgrade(player, state, mod, weapon);
        check(state.ledger.entry(mod).info().level() == card.info().details().maxLevel());
        rejected(() -> ModTransactions.upgrade(player, state, mod, weapon)); check(state.endo(player.getUUID()) == 200);
        state.ledger.share(foreignOwner, mod, false);
        rejected(() -> ModTransactions.polarize(player, state, weapon, 1, ModInfo.Polarity.VAZARIN));
        check(state.progress(weapon).equals(progress)); check(state.ledger.entry(mod).weapon().equals(weapon));
        var fresh = new ItemStack(Items.IRON_SWORD); player.getInventory().setItem(0, fresh);
        check(!fresh.has(Components.WEAPON_ID.get()));
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), "ya polarize 1 naramon");
        check(fresh.has(Components.WEAPON_ID.get()));
        check(fresh.get(Components.WEAPON_POLARITIES.get()).overrides().equals(List.of(new WeaponPolarityData.Slot(0, ModInfo.Polarity.NARAMON))));
        check(state.progress(fresh.get(Components.WEAPON_ID.get())).equals(WeaponProgress.ZERO));
        player.getInventory().setItem(0, restored);
        player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        UUID own = UUID.randomUUID();
        state.ledger.add(new ModLedger.Entry(own, player.getUUID(), null, false, card.bonuses(), card.info(), -1));
        state.endo(player.getUUID(), 0);
        rejected(() -> ModTransactions.upgrade(player, state, own, weapon)); check(state.ledger.entry(own).info().level() == 0);
        long price = card.info().details().upgradeCost(); state.endo(player.getUUID(), price);
        ModTransactions.upgrade(player, state, own, weapon);
        check(state.ledger.entry(own).info().level() == 1); check(state.endo(player.getUUID()) == 0);
        report.addProperty("creative_mod_checks", checks - creativeBefore);
        report.addProperty("polarization_checks", checks - before);
    }
    private static void rejected(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException expected) { check(true); return; }
        throw new AssertionError("Expected runtime transaction rejection");
    }
    private static void moveRequest(ServerPlayer player, UUID weapon, UUID mod, int slot) {
        PanelService.handle(player, new PanelRequest(100 + slot, PanelRequest.WEAPON, 0, PanelRequest.MOVE, weapon, mod, slot, ModInventory.Query.DEFAULT, "en_us"));
    }
    private static Cow target(ServerLevel level) {
        return target(level, Objects.requireNonNull(EntityType.COW.create(level)));
    }
    private static Cow target(ServerLevel level, Cow cow) {
        cow.setNoAi(true); cow.setNoGravity(true); cow.setPos(0, 100, 0);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); cow.setHealth(1000);
        cow.getAttribute(Attributes.ARMOR).setBaseValue(20); cow.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0);
        check(level.addFreshEntity(cow)); TARGETS.add(cow); return cow;
    }
    private static void statusApi(ServerLevel level, ServerPlayer owner, JsonObject report) {
        int before = checks;
        check(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.keySet().stream()
                .noneMatch(id -> id.getNamespace().equals(YagensAttributes.MODID)));
        var immune = target(level, new Cow(EntityType.COW, level) {
            @Override public boolean canBeAffected(MobEffectInstance effect) { return false; }
        });
        check(!immune.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120)));
        potionBlockedTarget = target(level);
        check(!potionBlockedTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120)));
        int beforePotionChecks = potionChecks;
        for (var entity : List.of(immune, potionBlockedTarget)) {
            for (var type : DamageType.values()) {
                check(StatusApi.apply(entity, type, owner.getUUID(), 0, 120));
                check(StatusApi.count(entity, type) == 1);
                check(StatusApi.durationTicks(entity, type) == StatusRules.duration(type, 120));
            }
            check(entity.getActiveEffects().isEmpty());
            var snapshot = StatusApi.layers(entity);
            check(snapshot.size() == DamageType.values().length);
            check(snapshot.stream().allMatch(layer -> layer.owner().equals(owner.getUUID())));
            try { snapshot.clear(); throw new AssertionError("Mutable status API snapshot"); }
            catch (UnsupportedOperationException expected) { check(true); }
            close(14.8, entity.getAttributeValue(Attributes.ARMOR));
            check(StatusApi.clear(entity, DamageType.CORROSIVE));
            close(20, entity.getAttributeValue(Attributes.ARMOR));
            check(snapshot.size() == DamageType.values().length);
            check(StatusApi.clearAll(entity));
            check(StatusApi.layers(entity).isEmpty());
            check(StatusMarkerSync.snapshot(entity).counts().isEmpty());
            check(!StatusApi.clearAll(entity));
        }
        check(potionChecks == beforePotionChecks);
        potionBlockedTarget = null;
        StatusEffects.state(immune).heatStrip = .5;
        check(StatusApi.clear(immune, DamageType.HEAT));
        close(0, StatusEffects.state(immune).heatStrip);
        StatusEffects.state(immune).frozen = 20;
        check(StatusApi.clearAll(immune)); check(!StatusEffects.frozen(immune));
        boolean rejected = false;
        try { StatusApi.apply(immune, DamageType.SLASH, owner.getUUID(), 1, 120, 101); }
        catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected); check(StatusApi.layers(immune).isEmpty());
        report.addProperty("independent_status_checks", checks - before);
    }
    private static void criticalHitProcs(ServerLevel level, JsonObject report) {
        int start = checks;
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("1bd609d8-3c2b-4a65-8aa4-664e95a4f81d"), "[HitProcChecks]"));
        var bow = new ItemStack(Items.BOW); player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, bow);
        var profile = CombatData.WEAPONS.weapon(bow); var world = WorldState.get(level.getServer()); UUID weapon = Components.identify(bow);
        var hunter = ModDefinitions.INSTANCE.catalog().card("yagens_attributes:hunter_munitions", 5);
        UUID hunterId = UUID.randomUUID(), obsoleteId = UUID.randomUUID();
        world.ledger.add(new ModLedger.Entry(hunterId, player.getUUID(), null, false, hunter.bonuses(), hunter.info(), -1));
        world.ledger.install(player.getUUID(), hunterId, weapon, ModInfo.Category.BOW, 8, 0, 30, true);
        var bone = ModDefinitions.INSTANCE.catalog().card("yagens_attributes:bone_finger_box", 5);
        world.ledger.add(new ModLedger.Entry(obsoleteId, player.getUUID(), weapon, false, bone.bonuses(), bone.info(), 1));
        var scoped = Objects.requireNonNull(RangedShooting.snapshot(player, bow));
        close(profile.criticalChance(), Stats.resolve(profile.criticalChance(), Stats.CRITICAL_CHANCE, scoped.sources()));
        world.bonus(player.getUUID(), Stats.CRITICAL_CHANCE, new Bonus(-profile.criticalChance(), 0, List.of(), 1));
        world.bonus(player.getUUID(), Stats.STATUS_CHANCE, new Bonus(-profile.statusChance(), 0, List.of(), 0));
        world.bonus(player.getUUID(), Stats.CRITICAL_SLASH_CHANCE, new Bonus(0, 0, List.of(), .7));
        var shot = Objects.requireNonNull(RangedShooting.snapshot(player, bow));
        shot = ShotData.decode(shot.encode());
        var target = target(level); potionBlockedTarget = target; target.setAbsorptionAmount(4);
        hitArrow(level, player, bow, shot, target, .75);
        check(StatusApi.count(target, DamageType.SLASH) == 1); check(target.getActiveEffects().isEmpty());
        var layer = StatusEffects.state(target).layers.get(0);
        close(15, layer.base()); check(layer.weapon().equals(weapon)); check(layer.criticalTier() == 1);
        StatusDamage.hurt(target, DamageType.SLASH, player.getUUID(), 1);
        check(StatusApi.count(target, DamageType.SLASH) == 1);
        potionBlockedTarget = null;
        for (var id : List.of(hunterId, obsoleteId)) { world.ledger.uninstall(player.getUUID(), id, weapon, false); world.ledger.withdraw(player.getUUID(), id); }
        for (var stat : List.of(Stats.CRITICAL_CHANCE, Stats.STATUS_CHANCE, Stats.CRITICAL_SLASH_CHANCE)) world.bonus(player.getUUID(), stat, Bonus.NONE);
        report.addProperty("critical_hit_proc_checks", checks - start);
    }
    private static void hitArrow(ServerLevel level, ServerPlayer player, ItemStack bow, ShotData shot, Cow target, double share) {
        var arrow = new Arrow(level, player, new ItemStack(Items.ARROW), bow);
        arrow.setData(RangedAttachments.SHOT, shot.projectile(share, true));
        check(target.hurt(level.damageSources().arrow(arrow, player), 10));
    }
    private static void armorSets(JsonObject report) {
        Item[][] sets = {
                {Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS},
                {Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS},
                {Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS},
                {Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS},
                {Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS},
                {Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS}};
        String[] names = {"leather", "gold", "chainmail", "iron", "diamond", "netherite"};
        int[] expectedArmor = {7, 11, 12, 15, 20, 20}, expectedToughness = {0, 0, 0, 0, 8, 12};
        var rows = new JsonArray();
        for (int i = 0; i < sets.length; i++) {
            int armor = 0; double toughness = 0;
            for (var item : sets[i]) { var piece = (ArmorItem) item; armor += piece.getDefense(); toughness += piece.getToughness(); }
            close(expectedArmor[i], armor); close(expectedToughness[i], toughness);
            var row = new JsonObject(); row.addProperty("set", names[i]); row.addProperty("armor", armor); row.addProperty("toughness", toughness);
            row.addProperty("reduction", 1 - ArmorRules.remaining(armor, toughness, 15, 8)); rows.add(row);
        }
        report.add("armor_sets", rows);
    }
    private static void close(double expected, double actual) { check(Math.abs(expected - actual) < 1e-4); }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Runtime combat check failed at " + checks); }
}
