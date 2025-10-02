package yagen.waitmydawn.api.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModSlot;
import yagen.waitmydawn.network.SyncComboPacket;
import yagen.waitmydawn.registries.DataAttachmentRegistry;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class BowShootEvent {
    private static final ConcurrentHashMap<Player, MultiArrow> MULTI_ARROW = new ConcurrentHashMap<>();

    private record MultiArrow(int arrowCount, float damageFactor, float precision, float power) {
    }

    private static final Set<UUID> CLONES = ConcurrentHashMap.newKeySet();

    @SubscribeEvent
    public static void onProjectileSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof AbstractArrow arr)) return;
        if (CLONES.remove(arr.getUUID())) return;
        if (!(arr.getOwner() instanceof Player player)) return;

        MultiArrow multiArrow = MULTI_ARROW.get(player);
        if (multiArrow == null) return;

        int arrowCount = multiArrow.arrowCount();
        if (arrowCount <= 1) {
            MULTI_ARROW.remove(player);
            return;
        }

        float precision = multiArrow.precision();
        float power = multiArrow.power();

        arr.setBaseDamage(arr.getBaseDamage() * multiArrow.damageFactor());

        float yawOffset = (float) (precision * player.getRandom().nextGaussian());
        float pitchOffset = (float) (precision * player.getRandom().nextGaussian());
        arr.shootFromRotation(player,
                player.getXRot() + pitchOffset,
                player.getYRot() + yawOffset,
                0.0F,
                power * 3.0F,
                1.0F);

        for (int i = 0; i < multiArrow.arrowCount() - 1; i++) {
            AbstractArrow clone = (AbstractArrow) arr.getType().create(event.getLevel());
            if (clone == null) return;

            CompoundTag tag = new CompoundTag();
            arr.save(tag);
            clone.load(tag);

            yawOffset = (float) (precision * player.getRandom().nextGaussian());
            pitchOffset = (float) (precision * player.getRandom().nextGaussian());
            clone.shootFromRotation(player,
                    player.getXRot() + pitchOffset,
                    player.getYRot() + yawOffset,
                    0.0F,
                    power * 3.0F,
                    1.0F);

            clone.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            clone.setUUID(UUID.randomUUID());
            CLONES.add(clone.getUUID());
            event.getLevel().addFreshEntity(clone);
        }

        MULTI_ARROW.remove(player);
    }

    @SubscribeEvent
    public static void MultiShot(ArrowLooseEvent event) {
        ItemStack bow = event.getBow();
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide) return;

        if (!(bow.getItem() instanceof BowItem)) return;

        float multishot = (float) player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.MULTISHOT.get())).getValue();
        if (multishot == 1f) return;
        float precision = (float) player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.SHOOT_SPREAD.get())).getValue(); // 10f
        float power = BowItem.getPowerForTime(event.getCharge());

        if (power < 0.1F) return;

        ItemStack itemstack = player.getProjectile(bow);

        boolean isCreative = player.isCreative();
        boolean isInfinity = //itemstack.getItem() instanceof ArrowItem &&
                bow.getEnchantmentLevel(level.registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(Enchantments.INFINITY)) >= 1;
//        player.sendSystemMessage(Component.literal("isInfinity:" + isInfinity));

        if (itemstack.isEmpty() && !isCreative) {
            if (!isInfinity) return;
        }

        int realCount = 0;
        float multishotCopy = multishot;
        while (multishot >= 1f) {
            multishot--;
            realCount++;
        }
        if (player.getRandom().nextFloat() < multishot) {
            realCount++;
        }
        float damageAverage = multishotCopy / realCount;

        MULTI_ARROW.put(player, new MultiArrow(realCount, damageAverage, precision, power));
    }

    private static final Map<Player, Double> fracFix = new WeakHashMap<>();

    @SubscribeEvent
    public static void FireRate(LivingEntityUseItemEvent.Tick event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Item item = event.getItem().getItem();
        if (!(item instanceof ProjectileWeaponItem)) return;

        double rate = player.getAttribute(YAttributes.FIRE_RATE).getValue();
        if (rate == 1) return;

        int intTick = (int) rate;
        double frac = rate - intTick;

        double fix = fracFix.getOrDefault(player, 0.) + frac;
        int fracTick = (int) fix;
        fix -= fracTick;

        int total = intTick + fracTick - 1;
        event.setDuration(event.getDuration() - total);

        fracFix.put(player, fix);
    }
//    can be used to decline the time to draw bow
//    @SubscribeEvent
//    public static void FireRateTest(LivingEntityUseItemEvent.Start event) {
//        if (!(event.getEntity() instanceof Player player)) return;
//        ItemStack itemStack = event.getItem();
//        Item item = itemStack.getItem();
//        if (!(item instanceof ProjectileWeaponItem)) return;
//        if (itemStack.getUseAnimation() != UseAnim.BOW) return;
//
//        event.setDuration(event.getDuration() - 15);
//    }

    @SubscribeEvent
    public static void shootCombo(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        ItemStack weaponStack = event.getSource().getWeaponItem();
        if (weaponStack == null) return;
        if (!(weaponStack.getItem() instanceof BowItem)) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean isAbility = false;
        if (IModContainer.isModContainer(chest)) {
            var container = IModContainer.get(chest);
            for (ModSlot slot : container.getActiveMods()) {
                if (slot.getMod().getModName().equals("collaborative_proficiency_armor_mod")) {
                    isAbility = true;
                    break;
                }
            }
        }
        if (!isAbility) return;
        if (player.getRandom().nextFloat() > 0.5f) return;
        double comboDuration = 20 * player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.COMBO_DURATION.get())).getValue();
        DataAttachmentRegistry.Combo old = player.getData(DataAttachmentRegistry.COMBO.get());
        DataAttachmentRegistry.Combo updated = old.withCount(old.count() + 1)
                .withDuration((int) comboDuration);
        player.setData(DataAttachmentRegistry.COMBO.get(), updated);
        PacketDistributor.sendToPlayer((ServerPlayer) player, new SyncComboPacket(updated));
    }
}
