package yagen.waitmydawn.api.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.minecraft.world.item.enchantment.Enchantments;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModSlot;

import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class BowShootEvent {
    @SubscribeEvent
    public static void MultiShot(ArrowLooseEvent event) {
        ItemStack bow = event.getBow();

        boolean isScattershot = false;
        if (!IModContainer.isModContainer(bow)) return;
        var container = IModContainer.get(bow);
        for (ModSlot slot : container.getActiveMods()) {
            if (slot.getMod().getModName().equals("scattershot_tool_mod")) {
                isScattershot = true;
                break;
            }
        }
        if (!(bow.getItem() instanceof BowItem)) return;

        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide) return;

        float multishot = (float) player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.MULTISHOT.get())).getValue();
        if (multishot == 1f) return;
        float precision = (float) player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.SHOOT_SPREAD.get())).getValue(); // 10f
        float power = BowItem.getPowerForTime(event.getCharge());

        if (power < 0.1F) return;

        event.setCanceled(true);

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
        if (itemstack.isEmpty()) itemstack = new ItemStack(Items.ARROW);

        ArrowItem arrowItem = itemstack.getItem() instanceof ArrowItem arrow ? arrow : (ArrowItem) Items.ARROW;

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

        if (isScattershot) {
            damageAverage = damageAverage * 0.4f;
//            System.out.println("isScattershot = " + isScattershot);
        }

        for (int i = 0; i < realCount; i++) {
            AbstractArrow arrow = arrowItem.createArrow(level, itemstack, player, bow);
            float yawOffset = (float) (precision * player.getRandom().nextGaussian());
            float pitchOffset = (float) (precision * player.getRandom().nextGaussian());
            arrow.shootFromRotation(player,
                    player.getXRot() + pitchOffset,
                    player.getYRot() + yawOffset,
                    0.0F,
                    power * 3.0F,
                    1.0F);

            arrow.setBaseDamage(arrow.getBaseDamage() * damageAverage);

            if (power == 1.0f) arrow.setCritArrow(true);

            if (i != 0) arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            else if (isCreative || (isInfinity && itemstack.is(Items.ARROW)))
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

            level.addFreshEntity(arrow);
        }

        if (!isCreative && !isInfinity) itemstack.shrink(1);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0F, 1.0F / (level.random.nextFloat() * 0.4F + 1.2F) + power * 0.5F);
        player.awardStat(Stats.ITEM_USED.get(bow.getItem()));
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

}
