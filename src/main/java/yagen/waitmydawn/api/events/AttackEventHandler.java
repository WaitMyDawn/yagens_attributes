package yagen.waitmydawn.api.events;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.*;
import net.minecraft.world.entity.projectile.*;
import yagen.waitmydawn.item.weapon.LEndersCataclysmItem;
import yagen.waitmydawn.network.DamageNumberPacket;
import yagen.waitmydawn.network.SyncComboPacket;
import yagen.waitmydawn.registries.DataAttachmentRegistry;
import yagen.waitmydawn.registries.MobEffectRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static yagen.waitmydawn.effect.ElectricityStatusEffect.addElectricity;
import static yagen.waitmydawn.effect.GasStatusEffect.addGas;
import static yagen.waitmydawn.effect.HeatStatusEffect.addHeat;
import static yagen.waitmydawn.effect.SlashStatusEffect.addCut;
import static yagen.waitmydawn.effect.ToxinStatusEffect.addToxin;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class AttackEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void playerAttack(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;

        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack held = event.getSource().getWeaponItem();
        if (held == null) held = player.getMainHandItem();

        Map<DamageType, Float> dmgMap = new HashMap<>();

        Entity direct = event.getContainer().getSource().getDirectEntity();
        boolean isArrow = direct instanceof Arrow;
        boolean isThrownTrident = direct instanceof ThrownTrident;
        float baseForProjectile = 10f;
        if (!isArrow && !isThrownTrident)
            dmgMap.put(DamageType.IMPACT, 1f);
        else if (isThrownTrident)
            baseForProjectile = 8f;

        // get damage map
        Map<DamageType, Float> weaponMap = DamageTypeUtils.getDamageTypes(held);
        weaponMap.forEach((k, v) -> dmgMap.merge(k, v, Float::sum));

        float total = dmgMap.values().stream().reduce(0f, Float::sum);
//        System.out.println("TestDamage: total " + total);
        dmgMap.forEach((type, value) ->
                System.out.println(type.name() + ": " + String.format("%.2f", value))
        );

//        player.sendSystemMessage(
//                Component.literal("Compose: " + dmgMap.toString())
//        );

        if (total <= 0) return;

        float actual = event.getNewDamage();
        if (actual <= 0) return;

        // health material
        HealthMaterialType mat = HealthMaterialUtils.getMaterialType(target);
        float adjustedTotal = 0f;

        for (var entry : dmgMap.entrySet()) {
            adjustedTotal = adjustedTotal + actual *
                    entry.getValue() / total
                    * DamageBonusTable.getBonus(mat, entry.getKey());
        }

        double cc = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.CRITICAL_CHANCE.get())).getValue();
        double cd = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.CRITICAL_DAMAGE.get())).getValue();
        double sc = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.STATUS_CHANCE.get())).getValue();
        double lifeSteal = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.LIFE_STEAL.get())).getValue();
        int scCount = (int) sc;
        sc = sc - scCount;

        // cold status
        float coldTocd = 0;
        if (target.hasEffect(MobEffectRegistry.COLD_STATUS)) {
            int amp = target.getEffect(MobEffectRegistry.COLD_STATUS).getAmplifier();
            coldTocd = Math.min(0.8f, coldTocd + amp * 0.1f + 0.3f);
        }

        double criticalLevel = 0;

        // critical
        if (cc > 1) {
            double ccTocd = (int) cc;
            System.out.println("TestDamage: cc " + cc + " ccTocd " + ccTocd);
            cc = cc - ccTocd;
            if (player.getRandom().nextDouble() <= cc) {
                adjustedTotal = adjustedTotal * ((float) (cd + ccTocd + 1) + coldTocd);
                criticalLevel = ccTocd + 1;
            } else {
                adjustedTotal = adjustedTotal * ((float) (cd + ccTocd) + coldTocd);
                criticalLevel = ccTocd;
            }
        } else {
            if (player.getRandom().nextDouble() <= cc) {
                adjustedTotal = adjustedTotal * ((float) cd + coldTocd);
                criticalLevel++;
            }
        }

        // arrow and trident fix
        if (isArrow || isThrownTrident)
            adjustedTotal = adjustedTotal * total / baseForProjectile;

        // viral
        //float factorViral = 1f;
        if (target.hasEffect(MobEffectRegistry.VIRAL_STATUS)) {
            int amp = target.getEffect(MobEffectRegistry.VIRAL_STATUS).getAmplifier();
            float factorViral = 1.5f;
            if (amp > 0)
                factorViral = Math.min(2.5f, factorViral + amp * 0.125f);
//            System.out.println("TestDamage: amp " + amp + " factorV " + factorViral);
            adjustedTotal = adjustedTotal * factorViral;
        }

        event.setNewDamage(adjustedTotal);
        // status
        if (player.getRandom().nextDouble() < sc) {
            scCount++;
        }
        for (; scCount > 0; scCount--) {
            float rand = player.getRandom().nextFloat() * total;
            float acc = 0f;
            for (var entry : dmgMap.entrySet()) {
                acc += entry.getValue();
                if (rand <= acc) {
                    statusEffect(entry.getKey(), player, target, adjustedTotal);
                    break;
                }
            }
        }
        if (lifeSteal > 0)
            player.setHealth(Math.min(player.getHealth() + adjustedTotal * (float) (lifeSteal), player.getMaxHealth()));

        Vec3 pos = target.position().add(0, target.getBbHeight() * 0.7, 0);

        PacketDistributor.sendToPlayersTrackingEntity(target,
                new DamageNumberPacket(pos, adjustedTotal, 0xFFFFFF, criticalLevel));
    }

    // puncture and viral
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void modifierFactorByStatus(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity sourceEntity)) return;
        float factorPuncture = 1f;

        if (sourceEntity.hasEffect(MobEffectRegistry.PUNCTURE_STATUS)) {
            int amp = sourceEntity.getEffect(MobEffectRegistry.PUNCTURE_STATUS).getAmplifier();
            factorPuncture = 0.8f;
            if (amp > 0)
                factorPuncture = Math.max(0.2f, factorPuncture - amp * 0.1f);
//            System.out.println("TestDamage: amp " + amp + " factorP " + factorPuncture);
        }

        event.setNewDamage(Math.max(0, event.getNewDamage() * factorPuncture));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void cancelProjectileInvulnerableTime(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        ItemStack itemStack = event.getSource().getWeaponItem();
        if (itemStack != null)
            if (itemStack.getItem() instanceof ProjectileWeaponItem)
                target.invulnerableTime = 0;
    }

    @SubscribeEvent
    public static void combo(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack weaponStack = event.getSource().getWeaponItem();
        int isValidity = 0;
        if (weaponStack == null) {
            weaponStack = player.getMainHandItem();
            isValidity = 1;
        }
        Item weapon = weaponStack.getItem();

        if (weapon instanceof ProjectileWeaponItem) return;
        if (ModList.get().isLoaded("cataclysm")) if (weapon == LEndersCataclysmItem.MEAT_SHREDDER.get()) return;

        double comboDuration = 20 * player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.COMBO_DURATION.get())).getValue();

        DataAttachmentRegistry.Combo old = player.getData(DataAttachmentRegistry.COMBO.get());
        DataAttachmentRegistry.Combo updated = old.withCount(old.count() + 1 - isValidity)
                .withDuration((int) comboDuration);
        player.setData(DataAttachmentRegistry.COMBO.get(), updated);
        PacketDistributor.sendToPlayer((ServerPlayer) player, new SyncComboPacket(updated));
    }

    public static void forceEffect(LivingEntity target, MobEffectInstance instance) {
        target.activeEffects.put(instance.getEffect(), instance);
        target.addEffect(instance);
    }

    private static void statusEffect(DamageType type, Player attacker, LivingEntity target, float finalDamage) {
        double sd = attacker.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.STATUS_DURATION.get())).getValue();
        switch (type) {
            case PUNCTURE -> {
                if (target.hasEffect(MobEffectRegistry.PUNCTURE_STATUS)) {
                    int amp = target.getEffect(MobEffectRegistry.PUNCTURE_STATUS).getAmplifier();
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.PUNCTURE_STATUS,
                            (int) (20 * sd),
                            Math.min(amp + 1, 6),
                            false,
                            true,
                            true
                    ));
                } else {
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.PUNCTURE_STATUS,
                            (int) (20 * sd),
                            0,
                            false,
                            true,
                            true
                    ));
                }
            }
            case SLASH -> {
                addCut(target, finalDamage * 0.35f, (int) (20 * sd), attacker);
                if (target.hasEffect(MobEffectRegistry.SLASH_STATUS)) {
                    int amp = target.getEffect(MobEffectRegistry.SLASH_STATUS).getAmplifier();
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.SLASH_STATUS,
                            (int) (20 * sd),
                            Math.min(amp + 1, 255),
                            false,
                            true,
                            true
                    ));
                } else {
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.SLASH_STATUS,
                            (int) (20 * sd),
                            0,
                            false,
                            true,
                            true
                    ));
                }
            }
            case IMPACT -> {
                forceEffect(target, new MobEffectInstance(
                        MobEffectRegistry.IMPACT_STATUS,
                        (int) (20 * sd / 8),
                        0,
                        false,
                        true,
                        true
                ));
            }
            case COLD -> {
                if (target.hasEffect(MobEffectRegistry.COLD_STATUS)) {
                    int amp = target.getEffect(MobEffectRegistry.COLD_STATUS).getAmplifier();
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.COLD_STATUS,
                            (int) (20 * sd),
                            Math.min(amp + 1, 5),
                            false,
                            true,
                            true
                    ));
                } else {
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.COLD_STATUS,
                            (int) (20 * sd),
                            0,
                            false,
                            true,
                            true
                    ));
                }
            }
            case TOXIN -> {
                addToxin(target, finalDamage * 0.35f, (int) (20 * sd), attacker);
                if (target.hasEffect(MobEffectRegistry.TOXIN_STATUS)) {
                    int amp = target.getEffect(MobEffectRegistry.TOXIN_STATUS).getAmplifier();
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.TOXIN_STATUS,
                            (int) (20 * sd),
                            Math.min(amp + 1, 255),
                            false,
                            true,
                            true
                    ));
                } else {
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.TOXIN_STATUS,
                            (int) (20 * sd),
                            0,
                            false,
                            true,
                            true
                    ));
                }
            }
            case CORROSIVE -> {
                if (target.hasEffect(MobEffectRegistry.CORROSIVE_STATUS)) {
                    int amp = target.getEffect(MobEffectRegistry.CORROSIVE_STATUS).getAmplifier();
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.CORROSIVE_STATUS,
                            (int) (20 * sd * 5),
                            Math.min(amp + 1, 8),
                            false,
                            true,
                            true
                    ));
                } else {
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.CORROSIVE_STATUS,
                            (int) (20 * sd * 5),
                            0,
                            false,
                            true,
                            true
                    ));
                }
            }
            case ELECTRICITY -> {
                addElectricity(target, finalDamage * 0.35f, (int) (20 * sd), attacker);
                if (target.hasEffect(MobEffectRegistry.ELECTRICITY_STATUS)) {
                    int amp = target.getEffect(MobEffectRegistry.ELECTRICITY_STATUS).getAmplifier();
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.ELECTRICITY_STATUS,
                            (int) (20 * sd),
                            Math.min(amp + 1, 255),
                            false,
                            true,
                            true
                    ));
                } else {
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.ELECTRICITY_STATUS,
                            (int) (20 * sd),
                            0,
                            false,
                            true,
                            true
                    ));
                }
            }
            case GAS -> {
                List<LivingEntity> nearby = target.level()
                        .getEntitiesOfClass(LivingEntity.class,
                                target.getBoundingBox().inflate(3.0));
                for (LivingEntity entity : nearby) {
                    if (entity instanceof Player) continue;
                    addGas(entity, finalDamage * 0.35f, (int) (20 * sd), attacker);
                    if (entity.hasEffect(MobEffectRegistry.GAS_STATUS)) {
                        int amp = entity.getEffect(MobEffectRegistry.GAS_STATUS).getAmplifier();
                        forceEffect(entity, new MobEffectInstance(
                                MobEffectRegistry.GAS_STATUS,
                                (int) (20 * sd),
                                Math.min(amp + 1, 255),
                                false,
                                true,
                                true
                        ));
                    } else {
                        forceEffect(entity, new MobEffectInstance(
                                MobEffectRegistry.GAS_STATUS,
                                (int) (20 * sd),
                                0,
                                false,
                                true,
                                true
                        ));
                    }
                }
            }
            case HEAT -> {
                addHeat(target, finalDamage * 0.35f, (int) (20 * sd));
                if (target.hasEffect(MobEffectRegistry.HEAT_STATUS)) {
                    int amp = target.getEffect(MobEffectRegistry.HEAT_STATUS).getAmplifier();
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.HEAT_STATUS,
                            (int) (20 * sd),
                            Math.min(amp + 1, 3),
                            false,
                            true,
                            true
                    ));
                } else {
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.HEAT_STATUS,
                            (int) (20 * sd),
                            0,
                            false,
                            true,
                            true
                    ));
                }
            }
            case VIRAL -> {
                if (target.hasEffect(MobEffectRegistry.VIRAL_STATUS)) {
                    int amp = target.getEffect(MobEffectRegistry.VIRAL_STATUS).getAmplifier();
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.VIRAL_STATUS,
                            (int) (20 * sd),
                            Math.min(amp + 1, 8),
                            false,
                            true,
                            true
                    ));
                } else {
                    forceEffect(target, new MobEffectInstance(
                            MobEffectRegistry.VIRAL_STATUS,
                            (int) (20 * sd),
                            0,
                            false,
                            true,
                            true
                    ));
                }
            }
            case RADIATION -> {
                forceEffect(target, new MobEffectInstance(
                        MobEffectRegistry.RADIATION_STATUS,
                        (int) (20 * sd),
                        0,
                        false,
                        true,
                        true
                ));
            }
            case BLAST -> {
                forceEffect(target, new MobEffectInstance(
                        MobEffectRegistry.BLAST_STATUS,
                        0,
                        Math.min(Math.round((finalDamage + 3) / 7), 255),
                        false,
                        true,
                        true
                ));
            }
            case MAGNETIC -> {
                forceEffect(target, new MobEffectInstance(
                        MobEffectRegistry.MAGNETIC_STATUS,
                        (int) (20 * sd),
                        0,
                        false,
                        true,
                        true
                ));
                forceEffect(target, new MobEffectInstance(
                        MobEffects.BLINDNESS,
                        (int) (20 * sd),
                        0,
                        false, true, true));
                forceEffect(target, new MobEffectInstance(
                        MobEffects.DARKNESS,
                        (int) (20 * sd),
                        0,
                        false, true, true));
            }
        }
    }
}