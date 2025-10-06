package yagen.waitmydawn.api.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModSlot;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class LivingEntityDeathEvent {
    @SubscribeEvent
    public static void onPlayerDeathWithTotem(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        ItemStack totem = findTotem(player);
        if (totem.isEmpty()) return;

        player.setHealth(1.0F);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
        player.level().broadcastEntityEvent(player, (byte) 35);
        totem.shrink(1);

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onDeath(LivingExperienceDropEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (!(event.getAttackingPlayer() instanceof Player player)) return;
        if (entity instanceof Player || !(entity instanceof Mob)) return;
        int modLevel = 0;
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!IModContainer.isModContainer(chest)) return;
        for (ModSlot slot : IModContainer.get(chest).getActiveMods()) {
            if (slot.getMod().getModName().equals("xp_armor_mod")) {
                modLevel = slot.getLevel();
                break;
            }
        }
        if (modLevel == 0) return;
        int originalExp = event.getDroppedExperience();
        if (originalExp <= 0) return;
//        ExperienceOrb.award((ServerLevel) entity.level(), entity.position(), modLevel * itemCount);
        event.setDroppedExperience(0);
        int total = (int) (originalExp * (1 + 0.4f * modLevel));
        while (total > 0) {
            if (total > 100)
                player.giveExperiencePoints(100);
            else
                player.giveExperiencePoints(total);
            total -= 100;
        }
    }

    private static ItemStack findTotem(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Items.TOTEM_OF_UNDYING)) return stack;
        }
        return ItemStack.EMPTY;
    }
}
