package yagen.waitmydawn.api.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mission.MissionData;
import yagen.waitmydawn.api.mission.MissionType;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.api.registry.ModRegistry;
import yagen.waitmydawn.api.mods.ModSlot;
import yagen.waitmydawn.entity.others.DarkDoppelgangerEntity;
import yagen.waitmydawn.registries.DamageTypeRegistry;
import yagen.waitmydawn.registries.ItemRegistry;

import java.util.List;
import java.util.Objects;

import static yagen.waitmydawn.api.util.ModCompat.ModLevelInItemStack;
import static yagen.waitmydawn.api.util.ModCompat.TRANSFORM_POOL_BY_RARITY;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME)
public class LivingEntityDeathEvent {
    private static final Logger log = LoggerFactory.getLogger(LivingEntityDeathEvent.class);

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
    public static void onPlayerDeathWithMission(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
//        if (player.level().isClientSide) return;
        Level level = player.level();
        if (level.isClientSide) return;

        MinecraftServer server = Objects.requireNonNull(player.getServer());
        MissionData data = MissionData.get(server);
        var active = data.getPlayerActiveTask(player);
        if (active == null) return;
        ResourceLocation taskId = active.getKey();

        data.addDeathCount((ServerLevel) level, level.dimension().location(), taskId);
    }

    @SubscribeEvent
    public static void BountyHunter(LivingExperienceDropEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (!(event.getAttackingPlayer() instanceof Player player)) return;
        if (!(entity instanceof Mob)) return;
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
        event.setDroppedExperience(0);
        int total = (int) (originalExp * (1 + 0.4f * modLevel));
        player.giveExperiencePoints(total);
    }

    /**
     * append for the Mobs who prevents vanilla way of dropping Loots in `LivingDropsEvent`
     */
    @SubscribeEvent
    public static void DarkDoppelgangerEntityDrop(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!ModList.get().isLoaded("darkdoppelganger")) return;
        if (entity.level().isClientSide) return;
        if (entity.getType() == DarkDoppelgangerEntity.DARK_DOPPELGANGER.get()) {
            List<AbstractMod> pool = TRANSFORM_POOL_BY_RARITY.get(ModRarity.WARFRAME);
            if (pool == null || pool.isEmpty()) return;
            AbstractMod newMod = pool.get(entity.level().random.nextInt(pool.size()));
            ItemStack result = new ItemStack(ItemRegistry.MOD.get());
            IModContainer.createModContainer(newMod, 1, result);
            ServerLevel level = (ServerLevel) entity.level();
            Vec3 pos = entity.position();
            level.addFreshEntity(
                    new ItemEntity(level,
                            pos.x, pos.y, pos.z,
                            result)
            );
            level.addFreshEntity(new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(ItemRegistry.TELEPORT.get())));
        }
    }

    @SubscribeEvent
    public static void TeleportItemDrop(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if(entity.getRandom().nextDouble()<0.5) return;
        if(!entity.getType().is(Tags.EntityTypes.BOSSES)) return;

        ServerLevel level = (ServerLevel) entity.level();
        Vec3 pos = entity.position();
        ItemStack itemStack = new ItemStack(ItemRegistry.TELEPORT.get());
        level.addFreshEntity(new ItemEntity(level, pos.x, pos.y, pos.z, itemStack));
    }

    @SubscribeEvent
    public static void HealthTrans(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (entity instanceof Player || !(entity instanceof Mob)) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        int modLevel = ModLevelInItemStack(chest, ModRegistry.HEALTH_TRANS_ARMOR_MOD.get());
        if (modLevel == 0) return;
        List<LivingEntity> nearby = player.level()
                .getEntitiesOfClass(LivingEntity.class,
                        player.getBoundingBox().inflate(64.0));
        for (LivingEntity target : nearby) {
            if (target instanceof Player) target.heal(2 * modLevel);
        }
        player.hurt(player.damageSources().source(DamageTypeRegistry.SLASH_STATUS_DAMAGE_TYPE), modLevel);
    }

    private static ItemStack findTotem(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Items.TOTEM_OF_UNDYING)) return stack;
        }
        return ItemStack.EMPTY;
    }
}
