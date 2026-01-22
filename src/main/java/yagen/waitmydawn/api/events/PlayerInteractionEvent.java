package yagen.waitmydawn.api.events;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.*;
import yagen.waitmydawn.config.ServerConfigs;
import yagen.waitmydawn.item.weapon.LEndersCataclysmItem;
import yagen.waitmydawn.network.EnergyPacket;
import yagen.waitmydawn.registries.DataAttachmentRegistry;
import yagen.waitmydawn.registries.MobEffectRegistry;

import java.util.Set;
import java.util.stream.Collectors;

import static yagen.waitmydawn.api.attribute.DefaultItemAttributes.DEFAULTS;
import static yagen.waitmydawn.effect.NourishEffect.*;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class PlayerInteractionEvent {
    @SubscribeEvent
    public static void onPlayerInteractMeatShredder(PlayerInteractEvent.RightClickItem event) {
        if (!(ModList.get().isLoaded("cataclysm"))) return;
        if (!ServerConfigs.BAN_MEAT_SHREDDER_OFFHAND.get()) return;
        Player player = event.getEntity();

        if (event.getHand() == InteractionHand.OFF_HAND) {
            ItemStack itemStack = player.getItemInHand(InteractionHand.OFF_HAND);
            if (itemStack.getItem() == LEndersCataclysmItem.MEAT_SHREDDER.get()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }
        }
    }

    @SubscribeEvent
    public static void onSplashLingering(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide) return;

        Item item = event.getItemStack().getItem();
        Player player = event.getEntity();

        if ((item != Items.SPLASH_POTION && item != Items.LINGERING_POTION) || player.isCreative()) return;

        if (player.getCooldowns().isOnCooldown(item)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        player.getCooldowns().addCooldown(item, ServerConfigs.SPLASH_POTION_COOLDOWN.get());
    }

    @SubscribeEvent
    public static void EatAndDrink(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        ItemStack itemStack = event.getItem();

        if (itemStack.getUseAnimation() != UseAnim.EAT && itemStack.getUseAnimation() != UseAnim.DRINK) return;
        if (player.hasEffect(MobEffectRegistry.NOURISH)) {
            event.setDuration(Math.max(0, event.getDuration() - (int) (16 * getNourishEnhance(player))));
        }
    }


    @SubscribeEvent
    public static void nourishCount(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (!(player.hasEffect(MobEffectRegistry.NOURISH))) return;
        ItemStack itemStack = event.getItem();
        if (!(itemStack.has(DataComponents.FOOD))) return;
        FoodProperties food = itemStack.get(DataComponents.FOOD);
        if (food == null) return;
        addNourishCount(player, Math.min(NOURISH_NEED, getNourishCount(player) + food.nutrition() * food.saturation()));
    }

    @SubscribeEvent
    public static void modifierHandlerAfterCraft(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        if (!(result.getItem() instanceof BowItem)) return;

        CustomData old = result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = old.copyTag();
        if (tag.contains("yagens_attributes_default_applied")) {
            var map = DEFAULTS.get(result.getItem());
            if (map == null) return;

            var existing = result.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

            Set<ResourceLocation> ourKeys = map.keySet().stream()
                    .map(BuiltInRegistries.ATTRIBUTE::getKey)
                    .map(key -> ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "default_" + key.getPath()))
                    .collect(Collectors.toSet());

            final ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            existing.modifiers().stream().filter(e -> !ourKeys.contains(e.modifier().id()))
                    .forEach(e -> builder.add(e.attribute(), e.modifier(), e.slot()));

            result.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
            tag.remove("yagens_attributes_default_applied");
            result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    @SubscribeEvent
    public static void useSpyglass(LivingEntityUseItemEvent.Tick event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.level().isClientSide) return;
        if (event.getItem().getUseAnimation() != UseAnim.SPYGLASS) return;
        if (event.getItem().getItem() != Items.SPYGLASS) return;

        int duration = event.getDuration();

        // duration: 0--1200
        if (duration > 1190) return;

        player.getPersistentData().putBoolean("is_scan", true);
    }

    @SubscribeEvent
    public static void stopSpyglass(LivingEntityUseItemEvent.Stop event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.level().isClientSide) return;
        if (event.getItem().getUseAnimation() != UseAnim.SPYGLASS) return;
        if (event.getItem().getItem() != Items.SPYGLASS) return;

        player.getPersistentData().putBoolean("is_scan", false);
    }

    @SubscribeEvent
    public static void airBrake(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        AttributeInstance airBrake = player.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(YAttributes.AIR_BRAKE.get()));
        AttributeInstance jump = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (airBrake == null || jump == null) return;

        boolean isAirBrake = player.getPersistentData().getBoolean("AirBrake");
        boolean isBulletJump = player.getPersistentData().getBoolean("BulletJump");
        int brakeTime = player.getPersistentData().getInt("AirBrakeTime");

        if (isBulletJump) {
            Vec3 vec3 = player.getLookAngle().normalize().scale(jump.getValue() / jump.getBaseValue() * 1.25);
            player.moveTo(player.getX(), player.getY() + 0.5, player.getZ());
            if (!player.isCreative() && player.getRandom().nextInt(1) == 0)
                player.getFoodData().addExhaustion(2.0f);
            player.setDeltaMovement(player.getDeltaMovement().add(vec3));
            player.getPersistentData().putBoolean("BulletJump", false);
        }

        if (isAirBrake && brakeTime <= (int) airBrake.getValue()) {
            Vec3 vel = player.getDeltaMovement();
            if (vel.y > 0)
                player.setDeltaMovement(vel.x, vel.y + 0.02, vel.z);
            else
                player.setDeltaMovement(vel.x, vel.y * 0.25, vel.z);
            player.getPersistentData().putInt("AirBrakeTime", brakeTime + 1);
        } else if (!isAirBrake) {
            player.getPersistentData().putInt("AirBrakeTime", 0);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || player.tickCount % 5 != 0) return;

        double maxEnergy = player.getAttributeValue(YAttributes.MAX_ENERGY);
        double energy = DataAttachmentRegistry.getEnergy(player);

        if (energy < maxEnergy) {
            double energyRegen = player.getAttributeValue(YAttributes.ENERGY_REGEN);
            energy = energy + energyRegen;
            if (energy > maxEnergy) energy = maxEnergy;
            DataAttachmentRegistry.setEnergy(player, energy);
            PacketDistributor.sendToPlayer((ServerPlayer) player, new EnergyPacket(energy));
        }
    }
}
