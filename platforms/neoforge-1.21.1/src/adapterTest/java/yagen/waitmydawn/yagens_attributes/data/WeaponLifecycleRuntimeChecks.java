package yagen.waitmydawn.yagens_attributes.data;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import yagen.waitmydawn.yagens_attributes.api.WeaponLifecycleApi;
import yagen.waitmydawn.yagens_attributes.core.Bonus;
import yagen.waitmydawn.yagens_attributes.core.ModInfo;
import yagen.waitmydawn.yagens_attributes.core.ModLedger;
import yagen.waitmydawn.yagens_attributes.core.Stats;
import yagen.waitmydawn.yagens_attributes.core.WeaponProgress;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import yagen.waitmydawn.yagens_attributes.registries.RangedAttachments;
import yagen.waitmydawn.yagens_attributes.setup.RangedShooting;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

final class WeaponLifecycleRuntimeChecks {
    private static final WeaponProgress TRAINED = new WeaponProgress(6, 0);
    private static final UUID OWNER = UUID.fromString("96e633a3-e089-4f46-889e-457cb3c0256d");
    private static final UUID OFFLINE_OWNER = UUID.fromString("24e75bc1-d72c-4d62-90d7-c58a6b9cf310");
    private static int checks;

    private record Fixture(UUID weapon, UUID firstMod, UUID secondMod, ItemStack stack) {}

    private static final class TestPlayer extends FakePlayer {
        private TestPlayer(ServerLevel level) {
            super(level, new GameProfile(OWNER, "[WeaponLifecycle]"));
        }

        private void removeVanishingEquipment() {
            destroyVanishingCursedItems();
        }
    }

    private WeaponLifecycleRuntimeChecks() {}

    static int run(ServerLevel level) {
        checks = 0;
        var world = WorldState.get(level.getServer());
        var player = new TestPlayer(level);
        player.setPos(0.5, 100, 0.5);
        durability(level, world, player);
        drops(level, world, player);
        tridents(level, world, player);
        creativeAndIntegration(level, world, player);
        clearAndVanish(level, world, player);
        return checks;
    }

    private static void durability(ServerLevel level, WorldState world, FakePlayer player) {
        var fixture = fixture(world, new ItemStack(Items.IRON_SWORD));
        fixture.stack().setDamageValue(fixture.stack().getMaxDamage() - 2);
        fixture.stack().hurtAndBreak(1, level, player, item -> {});
        expectBound(world, fixture);
        fixture.stack().hurtAndBreak(1, level, player, item -> {});
        equal(true, fixture.stack().isEmpty());
        expectRetired(world, fixture);
        var saved = WorldState.load(world.save(new CompoundTag(), level.registryAccess()), level.registryAccess());
        expectRetired(saved, fixture);
    }

    private static void drops(ServerLevel level, WorldState world, FakePlayer player) {
        var burned = fixture(world, new ItemStack(Items.WOODEN_SWORD));
        var dropping = drop(level, burned.stack());
        equal(true, dropping.hurt(level.damageSources().lava(), 10));
        equal(true, dropping.isRemoved());
        expectRetired(world, burned);

        var expired = fixture(world, new ItemStack(Items.IRON_AXE));
        var expiring = drop(level, expired.stack());
        var age = new CompoundTag();
        expiring.addAdditionalSaveData(age);
        age.putShort("Age", (short) (expiring.lifespan - 1));
        expiring.readAdditionalSaveData(age);
        Consumer<ItemExpireEvent> extend = event -> {
            if (event.getEntity() == expiring) event.addExtraLife(15);
        };
        NeoForge.EVENT_BUS.addListener(extend);
        try {
            expiring.tick();
            equal(false, expiring.isRemoved());
            expectBound(world, expired);
        } finally {
            NeoForge.EVENT_BUS.unregister(extend);
        }
        for (int tick = 0; tick < 15; tick++) expiring.tick();
        equal(true, expiring.isRemoved());
        expectRetired(world, expired);

        var delivered = fixture(world, new ItemStack(Items.DIAMOND_AXE));
        var animation = drop(level, delivered.stack());
        animation.makeFakeItem();
        animation.tick();
        equal(true, animation.isRemoved());
        equal(delivered.weapon(), delivered.stack().get(Components.WEAPON_ID.get()));
        expectBound(world, delivered);

        var pickedUp = fixture(world, new ItemStack(Items.GOLDEN_SWORD));
        var picked = drop(level, pickedUp.stack());
        picked.playerTouch(player);
        equal(true, picked.isRemoved());
        equal(true, player.getInventory().contains(stack -> pickedUp.weapon().equals(stack.get(Components.WEAPON_ID.get()))));
        expectBound(world, pickedUp);

        var unloaded = fixture(world, new ItemStack(Items.DIAMOND_SWORD));
        drop(level, unloaded.stack()).remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        expectBound(world, unloaded);

        var voided = fixture(world, new ItemStack(Items.STONE_AXE));
        var falling = drop(level, voided.stack());
        falling.setPos(0.5, level.getMinBuildHeight() - 70, 0.5);
        falling.tick();
        equal(true, falling.isRemoved());
        expectRetired(world, voided);
    }

    private static void tridents(ServerLevel level, WorldState world, FakePlayer player) {
        var normal = fixture(world, new ItemStack(Items.TRIDENT));
        var thrown = new ThrownTrident(level, player, normal.stack());
        thrown.pickup = AbstractArrow.Pickup.ALLOWED;
        for (int tick = 0; tick < 1200; tick++) thrown.tickDespawn();
        equal(true, thrown.isRemoved());
        expectRetired(world, normal);

        var recovered = fixture(world, new ItemStack(Items.TRIDENT));
        var returning = new ThrownTrident(level, player, recovered.stack());
        returning.pickup = AbstractArrow.Pickup.ALLOWED;
        returning.setNoPhysics(true);
        returning.playerTouch(player);
        equal(true, returning.isRemoved());
        equal(true, player.getInventory().contains(stack -> recovered.weapon().equals(stack.get(Components.WEAPON_ID.get()))));
        expectBound(world, recovered);

        var creative = fixture(world, new ItemStack(Items.TRIDENT));
        var copy = new ThrownTrident(level, player, creative.stack());
        copy.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        copy.kill();
        equal(true, copy.isRemoved());
        expectBound(world, creative);

        var virtual = new ThrownTrident(level, player, creative.stack());
        virtual.pickup = AbstractArrow.Pickup.ALLOWED;
        var shot = Objects.requireNonNull(RangedShooting.snapshot(player, creative.stack()));
        virtual.setData(RangedAttachments.SHOT.get(), shot.projectile(1, true));
        virtual.kill();
        equal(true, virtual.isRemoved());
        expectBound(world, creative);
    }

    private static void creativeAndIntegration(ServerLevel level, WorldState world, FakePlayer player) {
        var original = fixture(world, new ItemStack(Items.DIAMOND_SWORD));
        var stack = original.stack();
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Lifecycle fixture"));
        stack.set(Components.CATALYZED.get(), true);
        stack.set(Components.ABSORB_EXPERIENCE.get(), true);
        stack.set(Components.WEAPON_POLARITIES.get(), WeaponPolarityData.EMPTY.with(0, ModInfo.Polarity.MADURAI));

        player.getAbilities().instabuild = true;
        var menu = ChestMenu.oneRow(1, player.getInventory());
        menu.getSlot(0).set(stack);
        menu.clicked(0, 2, ClickType.CLONE, player);
        var creativeCopy = menu.getCarried();
        player.getAbilities().instabuild = false;
        checkIndependent(world, original, creativeCopy);
        checkIndependent(world, original, WeaponLifecycleApi.copyAsNew(stack));
        equal(Boolean.TRUE, stack.get(Components.CATALYZED.get()));
        var smithingResult = stack.transmuteCopy(Items.NETHERITE_SWORD, 1);
        equal(original.weapon(), smithingResult.get(Components.WEAPON_ID.get()));
        equal(stack.get(Components.WEAPON_POLARITIES.get()), smithingResult.get(Components.WEAPON_POLARITIES.get()));
        expectBound(world, original);

        WeaponLifecycleApi.retireDestroyedWeapon(level, original.weapon());
        WeaponLifecycleApi.retireDestroyedWeapon(level, original.weapon());
        expectRetired(world, original);
    }

    private static void clearAndVanish(ServerLevel level, WorldState world, TestPlayer player) {
        var fixture = fixture(world, new ItemStack(Items.WOODEN_AXE));
        player.getInventory().setItem(0, fixture.stack());
        var crafting = new SimpleContainer(4);
        equal(1, player.getInventory().clearOrCountMatchingItems(stack -> stack.is(Items.WOODEN_AXE), 0, crafting));
        expectBound(world, fixture);
        equal(1, player.getInventory().clearOrCountMatchingItems(stack -> stack.is(Items.WOODEN_AXE), 1, crafting));
        expectRetired(world, fixture);

        var cursed = fixture(world, new ItemStack(Items.IRON_SWORD));
        var curse = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.VANISHING_CURSE);
        cursed.stack().enchant(curse, 1);
        player.getInventory().setItem(0, cursed.stack());
        var ordinary = fixture(world, new ItemStack(Items.IRON_SWORD));
        player.getInventory().setItem(1, ordinary.stack());
        player.removeVanishingEquipment();
        equal(true, player.getInventory().getItem(0).isEmpty());
        equal(ordinary.stack(), player.getInventory().getItem(1));
        expectRetired(world, cursed);
        expectBound(world, ordinary);
    }

    private static ItemEntity drop(ServerLevel level, ItemStack stack) {
        return new ItemEntity(level, 0.5, 100, 0.5, stack, 0, 0, 0);
    }

    private static Fixture fixture(WorldState world, ItemStack stack) {
        var weapon = Components.identify(stack);
        var first = UUID.randomUUID();
        var second = UUID.randomUUID();
        world.ledger.add(new ModLedger.Entry(first, OWNER, weapon, true, Map.of(Stats.DAMAGE, new Bonus(1, 0, List.of(), 0))));
        world.ledger.add(new ModLedger.Entry(second, OFFLINE_OWNER, weapon, false, Map.of()));
        world.progress(weapon, TRAINED);
        return new Fixture(weapon, first, second, stack);
    }

    private static void checkIndependent(WorldState world, Fixture original, ItemStack copy) {
        equal(false, copy.isEmpty());
        equal(original.stack().get(DataComponents.CUSTOM_NAME), copy.get(DataComponents.CUSTOM_NAME));
        equal(null, copy.get(Components.WEAPON_ID.get()));
        equal(null, copy.get(Components.CATALYZED.get()));
        equal(null, copy.get(Components.ABSORB_EXPERIENCE.get()));
        equal(null, copy.get(Components.WEAPON_POLARITIES.get()));
        var copyId = Components.identify(copy);
        equal(false, original.weapon().equals(copyId));
        equal(WeaponProgress.ZERO, world.progress(copyId));
        equal(List.of(), world.ledger.installed(copyId));
    }

    private static void expectBound(WorldState world, Fixture fixture) {
        equal(TRAINED, world.progress(fixture.weapon()));
        equal(2, world.ledger.bonuses(fixture.weapon()).size());
        equal(2, world.ledger.bonuses(fixture.weapon(), "minecraft:iron_sword").size());
    }

    private static void expectRetired(WorldState world, Fixture fixture) {
        equal(WeaponProgress.ZERO, world.progress(fixture.weapon()));
        equal(List.of(), world.ledger.installed(fixture.weapon()));
        equal(List.of(), world.ledger.bonuses(fixture.weapon()));
        equal(List.of(), world.ledger.bonuses(fixture.weapon(), "minecraft:iron_sword"));
        var first = world.ledger.entry(fixture.firstMod());
        var second = world.ledger.entry(fixture.secondMod());
        equal(OWNER, first.owner());
        equal(OFFLINE_OWNER, second.owner());
        equal(null, first.weapon());
        equal(null, second.weapon());
        equal(true, first.shared());
        equal(false, second.shared());
        equal(-1, first.slot());
        equal(-1, second.slot());
    }

    private static void equal(Object expected, Object actual) {
        checks++;
        if (!Objects.equals(expected, actual)) throw new AssertionError(expected + " != " + actual);
    }
}
